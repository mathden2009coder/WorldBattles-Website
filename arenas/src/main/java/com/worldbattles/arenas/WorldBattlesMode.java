package com.worldbattles.arenas;
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
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
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
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.nio.file.*;
import java.util.*;
public final class WorldBattlesMode {
 private static final Logger LOG=LoggerFactory.getLogger("WorldBattles WorldBattles");
 private static final String ACTIVE="wb_arena_active",RECOVER="wb_arena_recover";
 private static final ResourceLocation MAP_ID=new ResourceLocation("minecraft","arena2");
 private static final ResourceLocation ROOM_ID=new ResourceLocation("minecraft","wbsalleattente");
 private static final BlockPos MAP_ORIGIN=new BlockPos(-110,-59,-130), MAP_SIZE=new BlockPos(144,25,96);
 private static final BlockPos ROOM_ORIGIN=new BlockPos(-37,-59,-23),ROOM_SIZE=new BlockPos(13,13,15);
 private static final SimpleChannel NET=NetworkRegistry.newSimpleChannel(new ResourceLocation("worldbattlesarenas","worldbattles"),()->"1",v->v.equals("1"),v->v.equals("1"));
 public record View(String[] statuses){}
 public record Choice(int index){}
 private enum Phase{READY,WAITING,COUNTDOWN,ACTIVE,RESTORING,LOCKED}
 private enum Team{NETHER,OVERWORLD}
 private static final class Match{
  final int index,dx;
  final Map<UUID,Team> teams=new LinkedHashMap<>();
  final ServerBossEvent score;
  Phase phase=Phase.READY;
  boolean generated;
  long deadline,restoreAt;
  int round=0,netherWins=0,overworldWins=0,netherKills=0,overworldKills=0;
  Match(int index){
   this.index=index;dx=index*300;generated=index==0;
   score=new ServerBossEvent(Component.literal("WORLD BATTLES"),BossEvent.BossBarColor.BLUE,BossEvent.BossBarOverlay.PROGRESS);
  }
  String status(){
   if(!generated)return "NON GENEREE";
   return switch(phase){
    case READY->"DISPONIBLE 0/10";
    case WAITING->"ATTENTE "+teams.size()+"/10 · "+Math.max(0,(deadline-ticks+19)/20)+"s";
    case COUNTDOWN->"DEPART IMMINENT";
    case ACTIVE->"MANCHE "+round+" · "+netherKills+"-"+overworldKills;
    case RESTORING->"RESTAURATION";
    case LOCKED->"MAINTENANCE";
   };
  }
  int count(Team t){int count=0;for(Team v:teams.values())if(v==t)count++;return count;}
  void titles(String title,String sub,int stay){
   for(UUID id:teams.keySet()){ServerPlayer p=find(id);if(p!=null)title(p,title,sub,stay);}
  }
  void updateBar(){
   score.setName(Component.literal("WORLD BATTLES · Nether "+netherKills+"/25   Overworld "+overworldKills+"/25   |   "+netherWins+"-"+overworldWins));
   score.setProgress(Math.max(netherKills,overworldKills)/25f);
  }
  void join(ServerPlayer p){
   if(!generated||phase==Phase.LOCKED||phase==Phase.RESTORING){info(p,"Cette arène est indisponible.");return;}
   if(phase!=Phase.READY&&phase!=Phase.WAITING){info(p,"Partie déjà commencée.");return;}
   if(p.level().dimension()!=Level.OVERWORLD){info(p,"Rejoins le lobby.");return;}
   if(teams.size()>=10){info(p,"Arène pleine.");return;}
   if(p.getTags().contains(ACTIVE)||getMatch(p.getUUID())!=null){info(p,"Tu participes déjà à un mini-jeu.");return;}
   if(phase==Phase.READY){phase=Phase.WAITING;deadline=ticks+2400;}
   Team team=count(Team.NETHER)<=count(Team.OVERWORLD)?Team.NETHER:Team.OVERWORLD;
   clear(p);teams.put(p.getUUID(),team);p.addTag(ACTIVE);
   p.setGameMode(GameType.ADVENTURE);p.setInvulnerable(true);
   p.teleportTo(server.overworld(),-30.5+dx,-57,-13.5,0,0);
   titles("§6WORLD BATTLES","§f"+teams.size()+"/10 joueurs · "+Math.max(0,(deadline-ticks+19)/20)+" secondes",50);
  }
  void leave(ServerPlayer p){
   if(teams.remove(p.getUUID())==null)return;
   lobby(p,this);
   if(teams.isEmpty()){if(phase==Phase.WAITING)phase=Phase.READY;else if(phase!=Phase.READY)finish(null);}
  }
  void prepRound(){
   if(teams.isEmpty()){finish(null);return;}
   round++;phase=Phase.COUNTDOWN;deadline=ticks+100;netherKills=0;overworldKills=0;updateBar();
   for(var e:teams.entrySet()){ServerPlayer p=find(e.getKey());if(p==null)continue;
    teleportTeam(p,e.getValue());p.setInvulnerable(true);score.addPlayer(p);}
   titles("§eMANCHE "+round,"§fDébut dans 5 secondes",40);
  }
  void teleportTeam(ServerPlayer p,Team team){
   if(team==Team.NETHER)p.teleportTo(server.overworld(),27.5+dx,-49,-56.5,0,0);
   else p.teleportTo(server.overworld(),-91.5+dx,-49,-56.5,0,0);
  }
  void endRound(Team winner){
   if(winner==Team.NETHER)netherWins++;else overworldWins++;
   titles(winner==Team.NETHER?"§cNETHER GAGNE LA MANCHE":"§aOVERWORLD GAGNE LA MANCHE",
     "§fManches : "+netherWins+" - "+overworldWins,75);
   if(netherWins>=2||overworldWins>=2){finish(winner);return;}
   beginRestore();
  }
  void beginRestore(){
   phase=Phase.RESTORING;restoreAt=ticks+60;
   for(UUID id:teams.keySet()){ServerPlayer p=find(id);if(p!=null){
    p.setInvulnerable(true);p.teleportTo(server.overworld(),-30.5+dx,-57,-13.5,0,0);
   }}
   titles("§6PREPARATION","§fRestauration avant la prochaine manche",75);
  }
  void finish(Team winner){
   if(phase==Phase.RESTORING||phase==Phase.LOCKED)return;
   for(UUID id:new ArrayList<>(teams.keySet())){ServerPlayer p=find(id);if(p==null)continue;
    lobby(p,this);
    title(p,winner==null?"§ePARTIE TERMINEE":winner==Team.NETHER?"§cVICTOIRE NETHER !":"§aVICTOIRE OVERWORLD !",
      "§fRetour au lobby",100);
   }
   teams.clear();score.removeAllPlayers();round=0;netherWins=0;overworldWins=0;netherKills=0;overworldKills=0;
   phase=Phase.RESTORING;restoreAt=ticks+60;
  }
  void step(){
   if(phase==Phase.RESTORING&&ticks>=restoreAt){
    restoreAt=Long.MAX_VALUE;
    if(!restoreMap(this)){phase=Phase.LOCKED;LOG.error("World Battles arena {} locked after restoration failure",index+1);return;}
    if(teams.isEmpty()){phase=Phase.READY;return;}
    prepRound();
   }
   if(phase==Phase.WAITING){
    if(ticks>=deadline)prepRound();
    else if(ticks%20==0){int remaining=(int)Math.max(0,(deadline-ticks+19)/20);
     for(UUID id:teams.keySet()){ServerPlayer p=find(id);if(p!=null){
      p.setInvulnerable(true);
      if(p.distanceToSqr(-30.5+dx,-57,-13.5)>9)p.teleportTo(server.overworld(),-30.5+dx,-57,-13.5,0,0);
      p.displayClientMessage(Component.literal("§6World Battles §f· "+remaining/60+":"+String.format("%02d",remaining%60)+" · "+teams.size()+"/10"),true);
     }}
    }
   }
   if(phase==Phase.COUNTDOWN){
    if(ticks%20==0){int left=(int)((deadline-ticks+19)/20);if(left>0&&left<=5)titles("§e"+left,"§fDébut de la manche "+round,21);}
    if(ticks>=deadline){phase=Phase.ACTIVE;for(UUID id:teams.keySet()){ServerPlayer p=find(id);if(p!=null)p.setInvulnerable(false);}
     titles("§cCOMBAT !","§f25 éliminations pour gagner",45);}
   }
  }
  void eliminate(ServerPlayer victim,ServerPlayer killer){
   Team victimTeam=teams.get(victim.getUUID());
   if(victimTeam==null||phase!=Phase.ACTIVE)return;
   Team killerTeam=killer==null?null:teams.get(killer.getUUID());
   if(killerTeam!=null&&killerTeam!=victimTeam){
    if(killerTeam==Team.NETHER)netherKills++;else overworldKills++;
    updateBar();
   }
   victim.setHealth(victim.getMaxHealth());
   victim.clearFire();victim.removeAllEffects();
   teleportTeam(victim,victimTeam);
   title(victim,"§cELIMINE !","§fRetour dans ta base",40);
   if(netherKills>=25||overworldKills>=25)endRound(netherKills>=25?Team.NETHER:Team.OVERWORLD);
  }
 }
 private static MinecraftServer server;
 private static long ticks;
 private static final Match[] MATCHES={new Match(0),new Match(1),new Match(2)};
 private static ServerPlayer find(UUID id){return server==null?null:server.getPlayerList().getPlayer(id);}
 private static Match getMatch(UUID id){for(Match m:MATCHES)if(m.teams.containsKey(id))return m;return null;}
 private static void info(ServerPlayer p,String message){p.sendSystemMessage(Component.literal("§6[World Battles] §f"+message));}
 private static void title(ServerPlayer p,String heading,String subtitle,int duration){
  p.connection.send(new ClientboundSetTitlesAnimationPacket(5,duration,10));
  p.connection.send(new ClientboundSetTitleTextPacket(Component.literal(heading)));
  p.connection.send(new ClientboundSetSubtitleTextPacket(Component.literal(subtitle)));
 }
 private static void clear(ServerPlayer p){
  if(p.level().dimension()==Level.OVERWORLD){p.getInventory().clearContent();p.containerMenu.broadcastChanges();}
 }
 private static void lobby(ServerPlayer p,Match m){
  if(m!=null)m.score.removePlayer(p);
  p.setInvulnerable(false);clear(p);p.removeTag(ACTIVE);p.removeTag(RECOVER);
  p.teleportTo(server.overworld(),23.5,-58,18.5,0,0);p.setGameMode(GameType.ADVENTURE);
 }
 private static Optional<StructureTemplate> getTemplate(ResourceLocation id,BlockPos expected){
  Optional<StructureTemplate> template=server.overworld().getStructureManager().get(id);
  if(template.isEmpty()||!template.get().getSize().equals(expected)){
   LOG.error("Missing/invalid World Battles structure {}: expected {}",id,expected);return Optional.empty();
  }return template;
 }
 private static boolean place(StructureTemplate template,BlockPos origin){
  ServerLevel level=server.overworld();
  // Preload every affected horizontal chunk, including the large arena2 template.
  int xmax=origin.getX()+template.getSize().getX()-1,zmax=origin.getZ()+template.getSize().getZ()-1;
  for(int x=origin.getX()>>4;x<=xmax>>4;x++)for(int z=origin.getZ()>>4;z<=zmax>>4;z++)level.getChunk(x,z);
  return template.placeInWorld(level,origin,origin,new StructurePlaceSettings().setMirror(Mirror.NONE)
    .setRotation(Rotation.NONE).setIgnoreEntities(true),level.random,2);
 }
 private static boolean restoreMap(Match m){
  Optional<StructureTemplate> map=getTemplate(MAP_ID,MAP_SIZE);
  if(map.isEmpty())return false;
  try{return place(map.get(),MAP_ORIGIN.offset(m.dx,0,0));}
  catch(Exception e){LOG.error("World Battles map restore failed",e);return false;}
 }
 private static boolean generateRoom(Match m){
  Optional<StructureTemplate> room=getTemplate(ROOM_ID,ROOM_SIZE);
  if(room.isEmpty())return false;
  try{return place(room.get(),ROOM_ORIGIN.offset(m.dx,0,0));}
  catch(Exception e){LOG.error("World Battles waiting-room generation failed",e);return false;}
 }
 private static Path flagFile(){return server.getWorldPath(LevelResource.ROOT).resolve("worldbattles-mode-generated.properties");}
 private static void loadFlags(){
  try{Properties props=new Properties();if(Files.exists(flagFile()))try(var input=Files.newInputStream(flagFile())){props.load(input);}
   for(int i=1;i<3;i++)MATCHES[i].generated=Boolean.parseBoolean(props.getProperty("arena"+(i+1),"false"));
  }catch(Exception e){LOG.error("Failed to load World Battles generation state",e);}
 }
 private static void saveFlags(){
  try{Properties props=new Properties();for(int i=1;i<3;i++)props.setProperty("arena"+(i+1),Boolean.toString(MATCHES[i].generated));
   try(var output=Files.newOutputStream(flagFile())){props.store(output,"World Battles copied arenas");}
  }catch(Exception e){LOG.error("Failed to persist World Battles generation state",e);}
 }
 private static int generate(CommandSourceStack source){
  if(getTemplate(MAP_ID,MAP_SIZE).isEmpty()||getTemplate(ROOM_ID,ROOM_SIZE).isEmpty())return 0;
  int generated=0;
  for(int i=1;i<3;i++){Match m=MATCHES[i];
   if(m.generated||m.phase!=Phase.READY)continue;
   if(!restoreMap(m)||!generateRoom(m))break;
   m.generated=true;generated++;saveFlags();
  }
  final int count=generated;source.sendSuccess(()->Component.literal("World Battles: "+count+" copie(s) créées. Vérifie les bâtiments."),true);
  return generated;
 }
 private static void open(ServerPlayer p){
  NET.send(PacketDistributor.PLAYER.with(()->p),new View(new String[]{MATCHES[0].status(),MATCHES[1].status(),MATCHES[2].status()}));
 }
 public static void choose(int index){NET.sendToServer(new Choice(index));}
 private static void select(ServerPlayer p,int index){
  if(index==-1){Match m=getMatch(p.getUUID());if(m!=null)m.leave(p);return;}
  if(index>=0&&index<3)MATCHES[index].join(p);
 }
 public WorldBattlesMode(){
  NET.registerMessage(0,View.class,(m,b)->{for(String status:m.statuses())b.writeUtf(status);},
   b->new View(new String[]{b.readUtf(),b.readUtf(),b.readUtf()}),
   (m,c)->{c.get().enqueueWork(()->DistExecutor.unsafeRunWhenOn(Dist.CLIENT,()->()->WorldBattlesClient.show(m.statuses())));c.get().setPacketHandled(true);},
   Optional.of(NetworkDirection.PLAY_TO_CLIENT));
  NET.registerMessage(1,Choice.class,(m,b)->b.writeInt(m.index()),b->new Choice(b.readInt()),
   (m,c)->{ServerPlayer p=c.get().getSender();c.get().enqueueWork(()->{if(p!=null)select(p,m.index());});c.get().setPacketHandled(true);},
   Optional.of(NetworkDirection.PLAY_TO_SERVER));
  MinecraftForge.EVENT_BUS.register(this);
 }
 @SubscribeEvent public void register(RegisterCommandsEvent event){
  event.getDispatcher().register(Commands.literal("wbarenas")
   .then(Commands.literal("open").then(Commands.literal("worldbattles")
    .executes(c->{if(c.getSource().getEntity() instanceof ServerPlayer p){open(p);return 1;}return 0;})
    .then(Commands.argument("joueur",EntityArgument.player()).requires(c->c.hasPermission(2))
     .executes(c->{open(EntityArgument.getPlayer(c,"joueur"));return 1;}))))
   .then(Commands.literal("wbadmin").requires(c->c.hasPermission(2))
    .then(Commands.literal("generate").executes(c->generate(c.getSource())))
    .then(Commands.literal("start").executes(c->{int n=0;for(Match m:MATCHES)if(m.phase==Phase.WAITING){m.prepRound();n++;}return n;}))
    .then(Commands.literal("status").executes(c->{for(Match m:MATCHES){String s="World Battles "+(m.index+1)+": "+m.status();
      c.getSource().sendSuccess(()->Component.literal(s),false);}return 1;}))
    .then(Commands.literal("reset").executes(c->{for(Match m:MATCHES)if(m.generated&&m.phase!=Phase.RESTORING)m.finish(null);return 1;}))));
 }
 @SubscribeEvent public void tick(TickEvent.ServerTickEvent event){
  if(event.phase!=TickEvent.Phase.END)return;
  if(server!=event.getServer()){server=event.getServer();loadFlags();}
  ticks++;for(Match m:MATCHES)m.step();
 }
 @SubscribeEvent public void attack(LivingAttackEvent event){
  if(!(event.getEntity() instanceof ServerPlayer victim))return;
  Match m=getMatch(victim.getUUID());if(m==null)return;
  if(m.phase!=Phase.ACTIVE){event.setCanceled(true);return;}
  Entity attacker=event.getSource().getEntity();
  if(attacker instanceof ServerPlayer p){
   Team a=m.teams.get(p.getUUID()),b=m.teams.get(victim.getUUID());
   if(a!=null&&a==b)event.setCanceled(true);
  }
 }
 @SubscribeEvent public void death(LivingDeathEvent event){
  if(!(event.getEntity() instanceof ServerPlayer victim))return;
  Match m=getMatch(victim.getUUID());if(m==null||m.phase!=Phase.ACTIVE)return;
  event.setCanceled(true);
  Entity attacker=event.getSource().getEntity();
  m.eliminate(victim,attacker instanceof ServerPlayer p?p:null);
 }
 @SubscribeEvent public void logout(PlayerEvent.PlayerLoggedOutEvent event){
  if(!(event.getEntity() instanceof ServerPlayer p))return;
  Match m=getMatch(p.getUUID());if(m==null)return;
  m.teams.remove(p.getUUID());m.score.removePlayer(p);
  p.removeTag(ACTIVE);p.addTag(RECOVER);p.setInvulnerable(false);
  if(m.teams.isEmpty())m.finish(null);
 }
 @SubscribeEvent public void login(PlayerEvent.PlayerLoggedInEvent event){
  if(!(event.getEntity() instanceof ServerPlayer p))return;
  // Do not interfere with the Zombies mode recovery system.
  if(p.getTags().contains(RECOVER)&&getMatch(p.getUUID())==null){lobby(p,null);}
 }
}
