package com.worldbattles.factions;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.worldbattles.shop.ShopData;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.regex.Pattern;

@Mod(FactionsMod.ID)
public final class FactionsMod {
 public static final String ID="worldbattlesfactions";
 private static final Logger LOG=LoggerFactory.getLogger("WorldBattles Factions");
 private static final ResourceLocation FREELANDS=new ResourceLocation("secondworld","overworld");
 private static final long CREATE_COST=100_000L;
 private static final Pattern NAME=Pattern.compile("[A-Za-z0-9_]{3,16}");
 private static final SimpleChannel CHANNEL=NetworkRegistry.newSimpleChannel(new ResourceLocation(ID,"main"),()->"1",v->v.equals("1"),v->v.equals("1"));
 public record Snapshot(CompoundTag data){}
 public record Action(String operation,String argument,String extra){}
 private record Invitation(String faction,long expiry){}
 private static final Map<UUID,Invitation> INVITES=new HashMap<>();
 private static final Map<UUID,Long> HOME_COOLDOWN=new HashMap<>();
 private static final Map<UUID,Long> COMBAT_UNTIL=new HashMap<>();
 public FactionsMod(){
  CHANNEL.registerMessage(0,Snapshot.class,(m,b)->b.writeNbt(m.data()),b->new Snapshot(b.readNbt()),
   (m,ctx)->{ctx.get().enqueueWork(()->DistExecutor.unsafeRunWhenOn(Dist.CLIENT,()->()->FactionsClient.receive(m.data())));ctx.get().setPacketHandled(true);},
   Optional.of(NetworkDirection.PLAY_TO_CLIENT));
  CHANNEL.registerMessage(1,Action.class,(m,b)->{b.writeUtf(m.operation(),30);b.writeUtf(m.argument(),256);b.writeUtf(m.extra(),64);},
   b->new Action(b.readUtf(30),b.readUtf(256),b.readUtf(64)),
   (m,ctx)->{ServerPlayer player=ctx.get().getSender();ctx.get().enqueueWork(()->{if(player!=null)perform(player,m);});ctx.get().setPacketHandled(true);},
   Optional.of(NetworkDirection.PLAY_TO_SERVER));
  MinecraftForge.EVENT_BUS.register(this);
 }
 public static void send(String operation,String argument,String extra){
  CHANNEL.sendToServer(new Action(operation,argument,extra));
 }
 public static boolean allowed(ServerPlayer player){return player.level().dimension().location().equals(FREELANDS);}
 private static void feedback(ServerPlayer player,String message){
  player.sendSystemMessage(Component.literal("§6[Factions] §f"+message));
 }
 private static void notifyFaction(MinecraftServer server,FactionData.Faction faction,String message){
  for(UUID id:faction.members.keySet()){
   ServerPlayer p=server.getPlayerList().getPlayer(id);
   if(p!=null){feedback(p,message);if(allowed(p))snapshot(p,"");}
  }
 }
 private static long positive(String str){
  try {long n=Long.parseLong(str.trim());return n>=1&&n<=ShopData.MAX_BALANCE?n:0;}
  catch(NumberFormatException e){return 0;}
 }
 private static ServerPlayer online(MinecraftServer server,String name){return server.getPlayerList().getPlayerByName(name);}
 private static void open(ServerPlayer player){
  if(!allowed(player)){feedback(player,"Les factions sont accessibles uniquement dans les Freelands.");return;}
  snapshot(player,"");
 }
 @SubscribeEvent public void commands(RegisterCommandsEvent event){
  event.getDispatcher().register(Commands.literal("factions")
   .executes(c->{open(c.getSource().getPlayerOrException());return 1;})
   .then(Commands.literal("chat")
    .then(Commands.argument("message",StringArgumentType.greedyString())
     .executes(c->{ServerPlayer p=c.getSource().getPlayerOrException();
      perform(p,new Action("chat",StringArgumentType.getString(c,"message"),""));return 1;}))));
  event.getDispatcher().register(Commands.literal("f")
   .executes(c->{open(c.getSource().getPlayerOrException());return 1;})
   .then(Commands.literal("chat")
    .then(Commands.argument("message",StringArgumentType.greedyString())
     .executes(c->{ServerPlayer p=c.getSource().getPlayerOrException();
      perform(p,new Action("chat",StringArgumentType.getString(c,"message"),""));return 1;}))));
 }
 @SubscribeEvent public void login(PlayerEvent.PlayerLoggedInEvent event){
  if(!(event.getEntity() instanceof ServerPlayer p))return;
  FactionData data=FactionData.get(p.getServer());FactionData.Faction f=data.of(p.getUUID());
  if(f!=null&&!p.getGameProfile().getName().equals(f.members.get(p.getUUID()))){
   f.members.put(p.getUUID(),p.getGameProfile().getName());data.setDirty();
  }
 }
 @SubscribeEvent public void attack(LivingAttackEvent event){
  Entity attacker=event.getSource().getEntity();
  if(!(attacker instanceof ServerPlayer p)||!allowed(p))return;
  if(!(event.getEntity() instanceof ServerPlayer victim)||!allowed(victim))return;
  FactionData data=FactionData.get(p.getServer());
  FactionData.Faction attackerFaction=data.of(p.getUUID());
  if(attackerFaction!=null&&attackerFaction==data.of(victim.getUUID())&&!attackerFaction.friendlyFire){
   event.setCanceled(true);return;
  }
  long until=p.serverLevel().getServer().getTickCount()+300L;
  COMBAT_UNTIL.put(p.getUUID(),until);COMBAT_UNTIL.put(victim.getUUID(),until);
 }
 private static void perform(ServerPlayer p,Action a){
  if(!allowed(p)){feedback(p,"Factions désactivées ici : rejoins les Freelands.");return;}
  String op=a.operation(),arg=a.argument().trim(),extra=a.extra().trim();
  // Strip Minecraft formatting/control characters from user-supplied text.
  if(op.equals("chat")||op.equals("setdesc"))arg=arg.replaceAll("§[0-9a-fk-orA-FK-OR]", "").replaceAll("[\\p{Cntrl}]", " ").trim();
  MinecraftServer server=p.getServer();
  FactionData data=FactionData.get(server);
  FactionData.Faction f=data.of(p.getUUID());
  UUID id=p.getUUID();
  ShopData shop=ShopData.get(server);
  String outcome="";
  switch(op){
   case "refresh"->{snapshot(p,"");return;}
   case "create"->{
    if(f!=null){outcome="Tu as déjà une faction.";break;}
    if(!NAME.matcher(arg).matches()){outcome="Nom : 3 à 16 caractères, lettres/chiffres/_.";break;}
    if(!data.nameFree(arg)){outcome="Ce nom de faction est déjà pris.";break;}
    if(!Arrays.asList(FactionData.ICONS).contains(extra)){outcome="Choisis un emblème valide.";break;}
    if(shop.balance(id)<CREATE_COST){outcome="Il te faut 100 000 $ dans la banque de WorldBattles Shop.";break;}
    if(!shop.adminSet(id,p.getGameProfile().getName(),shop.balance(id)-CREATE_COST).ok()){
     outcome="Transaction refusée par la banque.";break;
    }
    FactionData.Faction created=new FactionData.Faction(arg,id);
    created.icon=extra;created.log(p.getGameProfile().getName()+" a fondé la faction (-100 000 $).");
    created.members.put(id,p.getGameProfile().getName());data.register(created);f=created;
    LOG.info("FACTION_CREATE player={} name={} cost=100000",id,arg);
    outcome="Faction "+arg+" créée ! Coût : 100 000 $.";
   }
   case "invite"->{
    if(f==null||!f.officer(id)){outcome="Réservé au chef et aux officiers.";break;}
    ServerPlayer target=online(server,arg);
    if(target==null||!allowed(target)){outcome="Ce joueur doit être connecté dans les Freelands.";break;}
    if(data.of(target.getUUID())!=null){outcome="Ce joueur a déjà une faction.";break;}
    if(f.members.size()>=FactionData.MAX_MEMBERS){outcome="Faction complète (30 membres).";break;}
    INVITES.put(target.getUUID(),new Invitation(f.name.toLowerCase(Locale.ROOT),System.currentTimeMillis()+300_000));
    feedback(target,"Invitation à rejoindre §e"+f.name+"§f ! Ouvre /factions (expire dans 5 min).");
    snapshot(target,"Invitation reçue de "+f.name);
    outcome="Invitation envoyée à "+target.getGameProfile().getName()+".";
   }
   case "accept"->{
    if(f!=null){outcome="Tu appartiens déjà à une faction.";break;}
    Invitation invite=INVITES.get(id);
    if(invite==null||invite.expiry()<System.currentTimeMillis()){INVITES.remove(id);outcome="Aucune invitation valide.";break;}
    FactionData.Faction dest=data.factions.get(invite.faction());
    if(dest==null||dest.members.size()>=FactionData.MAX_MEMBERS){INVITES.remove(id);outcome="Faction indisponible ou complète.";break;}
    INVITES.remove(id);data.add(dest,id,p.getGameProfile().getName());
    dest.log(p.getGameProfile().getName()+" a rejoint la faction.");
    notifyFaction(server,dest,p.getGameProfile().getName()+" a rejoint "+dest.name+".");
    outcome="Tu as rejoint "+dest.name+" !";
   }
   case "decline"->{INVITES.remove(id);outcome="Invitation refusée.";}
   case "leave"->{
    if(f==null){outcome="Tu n'as pas de faction.";break;}
    if(f.owner(id)){outcome="Transfère le rôle de chef ou dissous la faction avant de quitter.";break;}
    String previous=f.name;data.remove(f,id);f.log(p.getGameProfile().getName()+" a quitté.");outcome="Tu as quitté "+previous+".";
   }
   case "deposit"->{
    if(f==null){outcome="Tu n'as pas de faction.";break;}
    long amount=positive(arg);
    if(amount==0){outcome="Montant invalide.";break;}
    if(shop.balance(id)<amount){outcome="Solde insuffisant.";break;}
    if(amount>FactionData.MAX_TREASURY-f.treasury){outcome="Plafond de trésorerie atteint.";break;}
    if(!shop.adminSet(id,p.getGameProfile().getName(),shop.balance(id)-amount).ok()){outcome="Transaction refusée.";break;}
    f.treasury+=amount;f.log(p.getGameProfile().getName()+" a déposé "+amount+" $.");data.setDirty();
    outcome="Dépôt de "+amount+" $ effectué. Nouveau rang : "+f.rank()+".";
    LOG.info("FACTION_DEPOSIT player={} faction={} amount={}",id,f.name,amount);
   }
   case "withdraw"->{
    if(f==null||!f.owner(id)){outcome="Seul le chef peut retirer de la trésorerie.";break;}
    long amount=positive(arg);
    if(amount==0||f.treasury<amount){outcome="Montant invalide ou fonds insuffisants.";break;}
    if(shop.balance(id)>ShopData.MAX_BALANCE-amount){outcome="Ton compte personnel dépasse le plafond.";break;}
    if(!shop.adminSet(id,p.getGameProfile().getName(),shop.balance(id)+amount).ok()){outcome="Transaction refusée.";break;}
    f.treasury-=amount;f.log(p.getGameProfile().getName()+" a retiré "+amount+" $.");data.setDirty();
    outcome="Retrait de "+amount+" $ effectué.";
    LOG.info("FACTION_WITHDRAW player={} faction={} amount={}",id,f.name,amount);
   }
   case "promote","demote","kick","transfer"->{
    if(f==null||!f.owner(id)){outcome="Seul le chef peut gérer les grades et les expulsions.";break;}
    UUID targetId=null;
    for(var m:f.members.entrySet())if(m.getValue().equalsIgnoreCase(arg)){targetId=m.getKey();break;}
    if(targetId==null){outcome="Membre introuvable.";break;}
    if(targetId.equals(id)){outcome="Impossible de modifier ton propre rôle ainsi.";break;}
    String target=f.members.get(targetId);
    switch(op){
     case "promote"->{f.officers.add(targetId);outcome=target+" est maintenant officier.";}
     case "demote"->{f.officers.remove(targetId);outcome=target+" est maintenant membre.";}
     case "kick"->{data.remove(f,targetId);outcome=target+" a été exclu.";}
     default->{f.founder=targetId;f.officers.add(id);f.officers.remove(targetId);outcome=target+" est maintenant chef.";}
    }
    f.log(outcome);data.setDirty();
    ServerPlayer affected=server.getPlayerList().getPlayer(targetId);
    if(affected!=null){feedback(affected,outcome);if(allowed(affected))snapshot(affected,"");}
   }
   case "setdesc"->{
    if(f==null||!f.officer(id)){outcome="Réservé aux officiers.";break;}
    if(arg.length()>100||arg.isBlank()){outcome="Description entre 1 et 100 caractères.";break;}
    f.description=arg;f.log("Description modifiée.");data.setDirty();outcome="Description mise à jour.";
   }
   case "emblem"->{
    if(f==null||!f.owner(id)){outcome="Réservé au chef.";break;}
    if(!Arrays.asList(FactionData.ICONS).contains(arg)){outcome="Emblème inconnu.";break;}
    f.icon=arg;f.log("Emblème modifié.");data.setDirty();outcome="Emblème mis à jour.";
   }
   case "friendlyfire"->{
    if(f==null||!f.owner(id)){outcome="Réservé au chef.";break;}
    f.friendlyFire=!f.friendlyFire;data.setDirty();
    outcome="Dégâts entre membres : "+(f.friendlyFire?"ACTIVÉS":"DÉSACTIVÉS")+".";
   }
   case "sethome"->{
    if(f==null||!f.officer(id)){outcome="Réservé aux officiers.";break;}
    BlockPos pos=p.blockPosition();
    f.homeX=pos.getX();f.homeY=pos.getY();f.homeZ=pos.getZ();f.hasHome=true;data.setDirty();
    outcome="Point de ralliement de la faction enregistré.";
   }
   case "home"->{
    if(f==null||!f.hasHome){outcome="Aucun point de ralliement défini.";break;}
    long tick=server.getTickCount();
    if(COMBAT_UNTIL.getOrDefault(id,0L)>tick){outcome="Téléportation indisponible pendant un combat (15 s).";break;}
    if(HOME_COOLDOWN.getOrDefault(id,0L)>tick){outcome="Attends 60 secondes entre deux téléportations.";break;}
    if(!server.overworld().isClientSide()){
     var world=server.getLevel(net.minecraft.resources.ResourceKey.create(
       net.minecraft.core.registries.Registries.DIMENSION,FREELANDS));
     if(world==null){outcome="Dimension Freelands indisponible.";break;}
     world.getChunkAt(new BlockPos(f.homeX,f.homeY,f.homeZ));
     if(!world.getBlockState(new BlockPos(f.homeX,f.homeY,f.homeZ)).getCollisionShape(world,new BlockPos(f.homeX,f.homeY,f.homeZ)).isEmpty()
      ||!world.getBlockState(new BlockPos(f.homeX,f.homeY+1,f.homeZ)).getCollisionShape(world,new BlockPos(f.homeX,f.homeY+1,f.homeZ)).isEmpty()){
      outcome="Point de ralliement obstrué. Demande au chef de le déplacer.";break;
     }
     p.teleportTo(world,f.homeX+0.5,f.homeY,f.homeZ+0.5,p.getYRot(),p.getXRot());
     HOME_COOLDOWN.put(id,tick+1200);outcome="Téléportation au point de ralliement.";
    }
   }
   case "chat"->{
    if(f==null){outcome="Tu n'as pas de faction.";break;}
    if(arg.isBlank()||arg.length()>180){outcome="Message entre 1 et 180 caractères.";break;}
    for(UUID member:f.members.keySet()){
     ServerPlayer to=server.getPlayerList().getPlayer(member);
     if(to!=null)to.sendSystemMessage(Component.literal("§8[§6"+f.name+"§8] §e"+p.getGameProfile().getName()+"§7 : "+arg));
    }
    return;
   }
   case "disband"->{
    if(f==null||!f.owner(id)){outcome="Seul le chef peut dissoudre la faction.";break;}
    if(!arg.equals(f.name)){outcome="Pour confirmer, saisis le nom exact de ta faction.";break;}
    if(f.treasury>0&&shop.balance(id)>ShopData.MAX_BALANCE-f.treasury){
     outcome="Impossible de rembourser la trésorerie : plafond de solde personnel.";break;
    }
    if(f.treasury>0&&!shop.adminSet(id,p.getGameProfile().getName(),shop.balance(id)+f.treasury).ok()){
     outcome="Remboursement impossible.";break;
    }
    String name=f.name;long balance=f.treasury;
    notifyFaction(server,f,"La faction "+name+" a été dissoute par son chef.");
    data.dissolve(f);outcome="Faction dissoute. "+balance+" $ remboursés au chef.";
    LOG.info("FACTION_DISBAND by={} name={} refunded={}",id,name,balance);
   }
   default->{outcome="Action inconnue.";break;}
  }
  feedback(p,outcome);
  snapshot(p,outcome);
 }
 private static void snapshot(ServerPlayer player,String notice){
  if(!allowed(player))return;
  MinecraftServer server=player.getServer();FactionData data=FactionData.get(server);
  FactionData.Faction f=data.of(player.getUUID());
  CompoundTag root=new CompoundTag();
  root.putString("Notice",notice);root.putLong("Balance",ShopData.get(server).balance(player.getUUID()));
  root.putLong("Cost",CREATE_COST);root.putString("Player",player.getGameProfile().getName());
  Invitation invite=INVITES.get(player.getUUID());
  root.putString("Invite",invite!=null&&invite.expiry()>System.currentTimeMillis()?
   Optional.ofNullable(data.factions.get(invite.faction())).map(x->x.name).orElse(""):"");
  if(f!=null){
   root.putBoolean("InFaction",true);root.putString("Name",f.name);root.putString("Desc",f.description);
   root.putString("Icon",f.icon);root.putString("Rank",f.rank());root.putString("Role",f.role(player.getUUID()));
   root.putLong("Treasury",f.treasury);root.putBoolean("FriendlyFire",f.friendlyFire);
   root.putBoolean("Home",f.hasHome);
   ListTag members=new ListTag();
   for(var entry:f.members.entrySet()){
    CompoundTag member=new CompoundTag();member.putString("Name",entry.getValue());
    member.putString("Role",f.role(entry.getKey()));
    member.putBoolean("Online",server.getPlayerList().getPlayer(entry.getKey())!=null);
    members.add(member);
   }
   root.put("Members",members);
   ListTag history=new ListTag();for(String line:f.history){
    CompoundTag t=new CompoundTag();t.putString("Text",line);history.add(t);
   }root.put("History",history);
  }
  ListTag board=new ListTag();int count=0;
  for(FactionData.Faction top:data.leaderboard()){
   if(count++>=15)break;
   CompoundTag entry=new CompoundTag();entry.putString("Name",top.name);
   entry.putString("Icon",top.icon);entry.putString("Rank",top.rank());
   entry.putLong("Treasury",top.treasury);entry.putInt("Members",top.members.size());board.add(entry);
  }
  root.put("Leaderboard",board);
  CHANNEL.send(PacketDistributor.PLAYER.with(()->player),new Snapshot(root));
 }
}
