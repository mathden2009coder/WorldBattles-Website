package com.worldbattles.arenas;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.minecraft.commands.Commands;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetSubtitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitlesAnimationPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.BossEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.nio.file.*;
import java.util.*;

public final class SandWarsMode {
 private static final Logger LOG=LoggerFactory.getLogger("WorldBattles Sand Wars");
 private static final String ACTIVE="wb_arena_active",RECOVER="wb_arena_recover";
 private static final ResourceLocation MAP_ID=new ResourceLocation("minecraft","arena1");
 private static final ResourceLocation ROOM_ID=new ResourceLocation("minecraft","swsalleattente");
 private static final BlockPos MAP_ORIGIN=new BlockPos(34,-59,-5),MAP_SIZE=new BlockPos(73,20,96);
 private static final BlockPos ROOM_ORIGIN=new BlockPos(113,-59,38),ROOM_SIZE=new BlockPos(11,7,12);
 private static final SimpleChannel NET=NetworkRegistry.newSimpleChannel(new ResourceLocation("worldbattlesarenas","sandwars"),
  ()->"1",v->v.equals("1"),v->v.equals("1"));
 public record View(String[] statuses){}
 public record Choice(int index){}
 private enum Phase{READY,WAITING,COUNTDOWN,ACTIVE,RESTORING,LOCKED}
 private enum Team{ORANGE,NORMAL}
 private static MinecraftServer server;
 private static long ticks;
 private static final class Match {
  final int index,dz,verticalOffset;
  final Map<UUID,Team> teams=new LinkedHashMap<>();
  final Set<UUID> eliminated=new HashSet<>();
  final ServerBossEvent bar;
  Phase phase=Phase.READY;
  boolean generated, aligned;
  long deadline,restoreAt;
  int round=0,orangeWins=0,normalWins=0;
  Match(int index){
   this.index=index;dz=index*300;verticalOffset=index==0?0:-2;generated=index==0;aligned=index==0;
   bar=new ServerBossEvent(Component.literal("SAND WARS"),BossEvent.BossBarColor.YELLOW,BossEvent.BossBarOverlay.PROGRESS);
  }
  String status(){
   if(!generated)return "NON GENEREE";
   return switch(phase){
    case READY->"DISPONIBLE 0/10";
    case WAITING->"ATTENTE "+teams.size()+"/10 · "+Math.max(0,(deadline-ticks+19)/20)+" s";
    case COUNTDOWN->"DEPART IMMINENT";
    case ACTIVE->"MANCHE "+round+" · "+orangeWins+"-"+normalWins;
    case RESTORING->"RESTAURATION";
    case LOCKED->"MAINTENANCE";
   };
  }
  int count(Team team){int n=0;for(Team t:teams.values())if(t==team)n++;return n;}
  int alive(Team team){int n=0;for(var entry:teams.entrySet())
   if(entry.getValue()==team&&!eliminated.contains(entry.getKey())&&find(entry.getKey())!=null)n++;
   return n;
  }
  void announce(String heading,String subtitle,int stay){
   for(UUID id:teams.keySet()){ServerPlayer p=find(id);if(p!=null)title(p,heading,subtitle,stay);}
  }
  void updateBar(){
   bar.setName(Component.literal("SAND WARS · Orange "+orangeWins+"/3 | Sable "+normalWins+"/3 | Manche "+round));
   bar.setProgress(Math.max(orangeWins,normalWins)/3f);
  }
  void waitRoom(ServerPlayer p){p.setInvulnerable(true);p.teleportTo(server.overworld(),118.5,-59+verticalOffset,43.5+dz,0,0);}
  void teamSpawn(ServerPlayer p,Team team){
   if(team==Team.ORANGE)p.teleportTo(server.overworld(),102.5,-56+verticalOffset,0.5+dz,0,0);
   else p.teleportTo(server.overworld(),37.5,-53+verticalOffset,87.5+dz,180,0);
  }
  void join(ServerPlayer p){
   if(!generated||phase==Phase.RESTORING||phase==Phase.LOCKED){info(p,"Arène indisponible.");return;}
   if(phase!=Phase.READY&&phase!=Phase.WAITING){info(p,"Cette partie a déjà commencé.");return;}
   if(p.level().dimension()!=Level.OVERWORLD){info(p,"Rejoins le lobby.");return;}
   if(teams.size()>=10){info(p,"Arène complète.");return;}
   if(p.getTags().contains(ACTIVE)){info(p,"Tu participes déjà à un mini-jeu.");return;}
   if(phase==Phase.READY){phase=Phase.WAITING;deadline=ticks+2400;}
   Team team=count(Team.ORANGE)<=count(Team.NORMAL)?Team.ORANGE:Team.NORMAL;
   clear(p);teams.put(p.getUUID(),team);p.addTag(ACTIVE);p.setGameMode(GameType.ADVENTURE);
   waitRoom(p);
   announce("§6SAND WARS","§f"+teams.size()+"/10 joueurs · Départ dans "+Math.max(0,(deadline-ticks+19)/20)+" s",50);
  }
  void leave(ServerPlayer p){
   if(teams.remove(p.getUUID())==null)return;
   eliminated.remove(p.getUUID());lobby(p,this);
   if(teams.isEmpty()){if(phase==Phase.WAITING)phase=Phase.READY;else finish(null);}
   else if(phase==Phase.ACTIVE)checkRoundEnd();
  }
  void beginRound(){
   if(teams.isEmpty()){finish(null);return;}
   round++;phase=Phase.COUNTDOWN;deadline=ticks+100;eliminated.clear();updateBar();
   for(var entry:teams.entrySet()){ServerPlayer p=find(entry.getKey());if(p==null)continue;
    teamSpawn(p,entry.getValue());p.setHealth(p.getMaxHealth());p.removeAllEffects();p.clearFire();
    p.setInvulnerable(true);bar.addPlayer(p);
   }
   announce("§eMANCHE "+round,"§fDépart dans 5 secondes",45);
  }
  void checkRoundEnd(){
   if(phase!=Phase.ACTIVE)return;
   int orange=alive(Team.ORANGE),normal=alive(Team.NORMAL);
   // Do not award a point to a team unless BOTH teams had a participant.
   if(count(Team.ORANGE)==0||count(Team.NORMAL)==0){if(teams.isEmpty())finish(null);return;}
   if(orange>0&&normal>0)return;
   if(orange==0&&normal==0){beginRestore(null);return;}
   endRound(orange>0?Team.ORANGE:Team.NORMAL);
  }
  void endRound(Team winner){
   if(winner==Team.ORANGE)orangeWins++;else normalWins++;
   updateBar();
   announce(winner==Team.ORANGE?"§6SABLE ORANGE GAGNE !":"§eSABLE ORDINAIRE GAGNE !",
    "§fManches "+orangeWins+" - "+normalWins,75);
   if(orangeWins>=3||normalWins>=3){finish(winner);return;}
   beginRestore(winner);
  }
  void beginRestore(Team winner){
   phase=Phase.RESTORING;restoreAt=ticks+60;
   for(UUID id:teams.keySet()){ServerPlayer p=find(id);if(p!=null)waitRoom(p);}
   announce("§6RESTAURATION","§fProchaine manche dans quelques instants",65);
  }
  void finish(Team winner){
   if(phase==Phase.RESTORING||phase==Phase.LOCKED)return;
   for(UUID id:new ArrayList<>(teams.keySet())){ServerPlayer p=find(id);if(p!=null){
    lobby(p,this);
    title(p,winner==null?"§ePARTIE TERMINÉE":winner==Team.ORANGE?"§6VICTOIRE ORANGE !":"§eVICTOIRE SABLE !",
      "§fRetour au lobby",100);
   }}
   teams.clear();eliminated.clear();bar.removeAllPlayers();round=0;orangeWins=0;normalWins=0;
   phase=Phase.RESTORING;restoreAt=ticks+60;
  }
  void eliminate(ServerPlayer victim){
   if(phase!=Phase.ACTIVE||eliminated.contains(victim.getUUID()))return;
   eliminated.add(victim.getUUID());
   victim.setHealth(victim.getMaxHealth());victim.removeAllEffects();victim.clearFire();
   waitRoom(victim);
   title(victim,"§cÉLIMINÉ","§fAttends la prochaine manche",70);
   checkRoundEnd();
  }
  void step(){
   if(phase==Phase.RESTORING&&ticks>=restoreAt){
    restoreAt=Long.MAX_VALUE;
    if(!restoreMap(this)){phase=Phase.LOCKED;LOG.error("Sand Wars arena {} locked after restore failure",index+1);return;}
    if(teams.isEmpty()){phase=Phase.READY;return;}
    beginRound();
   }
   if(phase==Phase.WAITING){
    if(ticks>=deadline)beginRound();
    else if(ticks%20==0){int seconds=(int)Math.max(0,(deadline-ticks+19)/20);
     for(UUID id:teams.keySet()){ServerPlayer p=find(id);if(p==null)continue;
      p.setInvulnerable(true);
      if(p.distanceToSqr(118.5,-59+verticalOffset,43.5+dz)>9)waitRoom(p);
      p.displayClientMessage(Component.literal("§6Sand Wars §f· "+seconds/60+":"+String.format("%02d",seconds%60)+" · "+teams.size()+"/10"),true);
     }
    }
   }
   if(phase==Phase.COUNTDOWN){
    if(ticks%20==0){int sec=(int)((deadline-ticks+19)/20);
     if(sec>=1&&sec<=5)announce("§e"+sec,"§fDébut de la manche "+round,22);
    }
    if(ticks>=deadline){
     phase=Phase.ACTIVE;
     for(UUID id:teams.keySet()){ServerPlayer p=find(id);if(p!=null)p.setInvulnerable(false);}
     announce("§cCOMBAT !","§fUne seule vie par manche",45);
     checkRoundEnd();
    }
   }
   if(phase==Phase.ACTIVE&&ticks%20==0)for(UUID id:eliminated){
    ServerPlayer p=find(id);if(p==null)continue;
    p.setInvulnerable(true);
    if(p.distanceToSqr(118.5,-59,43.5+dz)>16)waitRoom(p);
   }
  }
 }
 private static final Match[] MATCHES={new Match(0),new Match(1),new Match(2)};
 private static ServerPlayer find(UUID id){return server==null?null:server.getPlayerList().getPlayer(id);}
 private static Match findMatch(UUID id){for(Match m:MATCHES)if(m.teams.containsKey(id))return m;return null;}
 private static void info(ServerPlayer p,String msg){p.sendSystemMessage(Component.literal("§6[Sand Wars] §f"+msg));}
 private static void title(ServerPlayer p,String heading,String sub,int duration){
  p.connection.send(new ClientboundSetTitlesAnimationPacket(5,duration,10));
  p.connection.send(new ClientboundSetTitleTextPacket(Component.literal(heading)));
  p.connection.send(new ClientboundSetSubtitleTextPacket(Component.literal(sub)));
 }
 private static void clear(ServerPlayer p){
  if(p.level().dimension()==Level.OVERWORLD){p.getInventory().clearContent();p.containerMenu.broadcastChanges();}
 }
 private static void lobby(ServerPlayer p,Match m){
  if(m!=null)m.bar.removePlayer(p);
  p.setInvulnerable(false);clear(p);p.removeTag(ACTIVE);p.removeTag(RECOVER);
  p.teleportTo(server.overworld(),23.5,-58,18.5,0,0);p.setGameMode(GameType.ADVENTURE);
 }
 private static Optional<StructureTemplate> template(ResourceLocation id,BlockPos size){
  Optional<StructureTemplate> result=server.overworld().getStructureManager().get(id);
  if(result.isEmpty()||!result.get().getSize().equals(size)){
   LOG.error("Missing/incorrect structure {}, expected {}",id,size);return Optional.empty();}
  return result;
 }
 private static boolean paste(StructureTemplate t,BlockPos origin){
  ServerLevel level=server.overworld();
  int xmax=origin.getX()+t.getSize().getX()-1,zmax=origin.getZ()+t.getSize().getZ()-1;
  for(int x=origin.getX()>>4;x<=(xmax>>4);x++)for(int z=origin.getZ()>>4;z<=(zmax>>4);z++)level.getChunk(x,z);
  return t.placeInWorld(level,origin,origin,new StructurePlaceSettings()
   .setMirror(Mirror.NONE).setRotation(Rotation.NONE).setIgnoreEntities(true),level.random,2);
 }
 private static boolean restoreMap(Match match){
  Optional<StructureTemplate> map=template(MAP_ID,MAP_SIZE);if(map.isEmpty())return false;
  try{return paste(map.get(),MAP_ORIGIN.offset(0,match.verticalOffset,match.dz));}
  catch(Exception e){LOG.error("Sand Wars arena {} restoration failed",match.index+1,e);return false;}
 }
 private static boolean copyRoom(Match match){
  Optional<StructureTemplate> room=template(ROOM_ID,ROOM_SIZE);if(room.isEmpty())return false;
  try{return paste(room.get(),ROOM_ORIGIN.offset(0,match.verticalOffset,match.dz));}
  catch(Exception e){LOG.error("Sand Wars waiting room {} creation failed",match.index+1,e);return false;}
 }
 // One-time correction for already-generated copies only. An operator must run this
 // after a full world backup. It does not affect the original arena.
 private static boolean noPlayersInOldCopy(Match match){
  ServerLevel world=server.overworld();
  for(ServerPlayer p:server.getPlayerList().getPlayers()){
   if(p.level()!=world)continue;
   BlockPos point=p.blockPosition();
   boolean nearMap=point.getX()>=MAP_ORIGIN.getX()-3&&point.getX()<MAP_ORIGIN.getX()+MAP_SIZE.getX()+3
     &&point.getZ()>=MAP_ORIGIN.getZ()+match.dz-3&&point.getZ()<MAP_ORIGIN.getZ()+match.dz+MAP_SIZE.getZ()+3;
   boolean nearRoom=point.getX()>=ROOM_ORIGIN.getX()-3&&point.getX()<ROOM_ORIGIN.getX()+ROOM_SIZE.getX()+3
     &&point.getZ()>=ROOM_ORIGIN.getZ()+match.dz-3&&point.getZ()<ROOM_ORIGIN.getZ()+match.dz+ROOM_SIZE.getZ()+3;
   if(nearMap||nearRoom)return false;
  }
  return true;
 }
 private static void clearOldUpperLayers(BlockPos originalOrigin,BlockPos dimensions){
  ServerLevel world=server.overworld();
  // Only the two obsolete top layers above the newly-lowered copy.
  // This does not clear any blocks within the freshly positioned structure.
  BlockPos.MutableBlockPos pos=new BlockPos.MutableBlockPos();
  for(int y=originalOrigin.getY()+dimensions.getY()-2;y<originalOrigin.getY()+dimensions.getY();y++){
   for(int x=originalOrigin.getX();x<originalOrigin.getX()+dimensions.getX();x++){
    for(int z=originalOrigin.getZ();z<originalOrigin.getZ()+dimensions.getZ();z++){
     pos.set(x,y,z);
     if(!world.getBlockState(pos).isAir())world.setBlock(pos,Blocks.AIR.defaultBlockState(),2);
    }
   }
  }
 }
 private static int realign(CommandSourceStack source){
  if(template(MAP_ID,MAP_SIZE).isEmpty()||template(ROOM_ID,ROOM_SIZE).isEmpty()){
   source.sendFailure(Component.literal("Sand Wars: structures manquantes ou de mauvaise taille."));return 0;
  }
  for(int i=1;i<3;i++){
   Match m=MATCHES[i];
   if(!m.generated||m.phase!=Phase.READY||!noPlayersInOldCopy(m)){
    source.sendFailure(Component.literal("Sand Wars "+(i+1)+": copie non générée, partie active ou joueur dans la zone. Rien n'a été déplacé."));
    return 0;
   }
  }
  int moved=0;
  for(int i=1;i<3;i++){
   Match m=MATCHES[i];
   if(m.aligned)continue;
   if(!restoreMap(m)||!copyRoom(m)){
    source.sendFailure(Component.literal("Echec du réalignement Sand Wars "+(i+1)+". Vérifie les logs et la sauvegarde."));
    return moved;
   }
   // Remove only the two topmost layers of the former +2-block-high copy.
   clearOldUpperLayers(MAP_ORIGIN.offset(0,0,m.dz),MAP_SIZE);
   clearOldUpperLayers(ROOM_ORIGIN.offset(0,0,m.dz),ROOM_SIZE);
   m.aligned=true;saveFlags();
   moved++;
  }
  final int result=moved;
  source.sendSuccess(()->Component.literal("Sand Wars: "+result+" copies réalignées 2 blocs plus bas."),true);
  return moved;
 }
 private static Path flagFile(){return server.getWorldPath(LevelResource.ROOT).resolve("sandwars-mode-generated.properties");}
 private static void loadFlags(){
  try{Properties properties=new Properties();if(Files.exists(flagFile()))try(var input=Files.newInputStream(flagFile())){properties.load(input);}
   for(int i=1;i<3;i++){
    MATCHES[i].generated=Boolean.parseBoolean(properties.getProperty("arena"+(i+1),"false"));
    MATCHES[i].aligned=Boolean.parseBoolean(properties.getProperty("aligned"+(i+1),"false"));
   }
  }catch(Exception e){LOG.error("Cannot read Sand Wars generation flags",e);}
 }
 private static void saveFlags(){
  try{Properties properties=new Properties();
   for(int i=1;i<3;i++){
    properties.setProperty("arena"+(i+1),Boolean.toString(MATCHES[i].generated));
    properties.setProperty("aligned"+(i+1),Boolean.toString(MATCHES[i].aligned));
   }
   try(var output=Files.newOutputStream(flagFile())){properties.store(output,"Sand Wars generated copies");}
  }catch(Exception e){LOG.error("Cannot write Sand Wars generation flags",e);}
 }
 private static int generate(CommandSourceStack source){
  if(template(MAP_ID,MAP_SIZE).isEmpty()||template(ROOM_ID,ROOM_SIZE).isEmpty()){
   source.sendFailure(Component.literal("Structure arena1 ou swsalleattente absente ou de mauvaise taille."));return 0;
  }
  int created=0;
  for(int i=1;i<3;i++){Match match=MATCHES[i];
   if(match.generated||match.phase!=Phase.READY)continue;
   if(!restoreMap(match)||!copyRoom(match))break;
   match.generated=true;match.aligned=true;created++;saveFlags();
  }
  int result=created;
  source.sendSuccess(()->Component.literal("Sand Wars : "+result+" copie(s) générées sur l'axe Z."),true);
  return created;
 }
 private static void open(ServerPlayer p){
  NET.send(PacketDistributor.PLAYER.with(()->p),new View(new String[]{MATCHES[0].status(),MATCHES[1].status(),MATCHES[2].status()}));
 }
 public static void choose(int index){NET.sendToServer(new Choice(index));}
 private static void select(ServerPlayer p,int index){
  if(index==-1){Match m=findMatch(p.getUUID());if(m!=null)m.leave(p);return;}
  if(index>=0&&index<3)MATCHES[index].join(p);
 }
 public SandWarsMode(){
  NET.registerMessage(0,View.class,(m,b)->{for(String status:m.statuses())b.writeUtf(status);},
   b->new View(new String[]{b.readUtf(),b.readUtf(),b.readUtf()}),
   (m,c)->{c.get().enqueueWork(()->DistExecutor.unsafeRunWhenOn(Dist.CLIENT,()->()->SandWarsClient.show(m.statuses())));c.get().setPacketHandled(true);},
   Optional.of(NetworkDirection.PLAY_TO_CLIENT));
  NET.registerMessage(1,Choice.class,(m,b)->b.writeInt(m.index()),b->new Choice(b.readInt()),
   (m,c)->{ServerPlayer p=c.get().getSender();c.get().enqueueWork(()->{if(p!=null)select(p,m.index());});c.get().setPacketHandled(true);},
   Optional.of(NetworkDirection.PLAY_TO_SERVER));
  MinecraftForge.EVENT_BUS.register(this);
 }
 @SubscribeEvent public void register(RegisterCommandsEvent event){
  event.getDispatcher().register(Commands.literal("wbarenas")
   .then(Commands.literal("open").then(Commands.literal("sandwars")
    .executes(c->{if(c.getSource().getEntity() instanceof ServerPlayer p){open(p);return 1;}return 0;})
    .then(Commands.argument("joueur",EntityArgument.player()).requires(s->s.hasPermission(2))
      .executes(c->{open(EntityArgument.getPlayer(c,"joueur"));return 1;}))))
   .then(Commands.literal("swadmin").requires(s->s.hasPermission(2))
    .then(Commands.literal("generate").executes(c->generate(c.getSource())))
    .then(Commands.literal("realign").executes(c->realign(c.getSource())))
    .then(Commands.literal("start").executes(c->{int n=0;for(Match m:MATCHES)if(m.phase==Phase.WAITING){m.beginRound();n++;}return n;}))
    .then(Commands.literal("status").executes(c->{for(Match m:MATCHES){
     String state="Sand Wars "+(m.index+1)+": "+m.status()+" manches "+m.orangeWins+"-"+m.normalWins;
     c.getSource().sendSuccess(()->Component.literal(state),false);
    }return 1;}))
    .then(Commands.literal("testwin")
     .then(Commands.argument("arena",IntegerArgumentType.integer(1,3))
      .then(Commands.literal("orange").executes(c->testWin(c.getSource(),IntegerArgumentType.getInteger(c,"arena"),Team.ORANGE)))
      .then(Commands.literal("normal").executes(c->testWin(c.getSource(),IntegerArgumentType.getInteger(c,"arena"),Team.NORMAL)))))
    .then(Commands.literal("reset").executes(c->{int n=0;for(Match m:MATCHES)if(m.generated&&m.phase!=Phase.RESTORING&&m.phase!=Phase.LOCKED){m.finish(null);n++;}return n;}))));
 }
 private static int testWin(CommandSourceStack source,int arena,Team winner){
  Match match=MATCHES[arena-1];
  if(match.phase!=Phase.ACTIVE){source.sendFailure(Component.literal("Sand Wars : l'arène n'est pas en combat."));return 0;}
  match.endRound(winner);
  source.sendSuccess(()->Component.literal("TEST : manche simulée pour "+winner+" dans Sand Wars "+arena),false);
  return 1;
 }
 @SubscribeEvent public void tick(TickEvent.ServerTickEvent event){
  if(event.phase!=TickEvent.Phase.END)return;
  if(server!=event.getServer()){server=event.getServer();loadFlags();}
  ticks++;for(Match m:MATCHES)m.step();
 }
 @SubscribeEvent public void attacked(LivingAttackEvent event){
  if(!(event.getEntity() instanceof ServerPlayer victim))return;
  Match m=findMatch(victim.getUUID());if(m==null)return;
  if(m.phase!=Phase.ACTIVE||m.eliminated.contains(victim.getUUID())){event.setCanceled(true);return;}
  Entity attacker=event.getSource().getEntity();
  if(attacker instanceof ServerPlayer p&&m.teams.get(p.getUUID())==m.teams.get(victim.getUUID()))event.setCanceled(true);
 }
 @SubscribeEvent public void death(LivingDeathEvent event){
  if(!(event.getEntity() instanceof ServerPlayer p))return;
  Match m=findMatch(p.getUUID());if(m==null||m.phase!=Phase.ACTIVE||m.eliminated.contains(p.getUUID()))return;
  event.setCanceled(true);m.eliminate(p);
 }
 @SubscribeEvent public void logout(PlayerEvent.PlayerLoggedOutEvent event){
  if(!(event.getEntity() instanceof ServerPlayer p))return;
  Match m=findMatch(p.getUUID());if(m==null)return;
  m.teams.remove(p.getUUID());m.eliminated.remove(p.getUUID());m.bar.removePlayer(p);
  p.removeTag(ACTIVE);p.addTag(RECOVER);p.setInvulnerable(false);
  if(m.teams.isEmpty())m.finish(null);
  else if(m.phase==Phase.ACTIVE)m.checkRoundEnd();
 }
 // Zombie mode already handles recovery from wb_arena_recover.
}
