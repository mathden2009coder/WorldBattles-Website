package com.worldbattles.arenas;
import net.minecraft.commands.Commands;
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
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.nio.file.*;
import java.util.*;
@Mod(WorldBattlesArenas.ID)
public class WorldBattlesArenas {
 public static final String ID="worldbattlesarenas";
 private static final Logger LOG=LoggerFactory.getLogger("WorldBattles Arenas");
 private static final String TAG="wb_arena_active", ZTAG="wb_arena_zombie", LOCK="wb_arena_recover";
 private static final ResourceLocation STRUCTURE_ID=new ResourceLocation("minecraft","zombies");
 private static final BlockPos ORIGIN=new BlockPos(-142,-62,-12), SIZE=new BlockPos(65,10,101);
 private static final int[][] SPAWNS={
 {-96,-60,24},{-92,-60,46},{-113,-60,55},{-101,-60,49},{-112,-60,78},
 {-96,-60,67},{-92,-60,82},{-125,-60,77},{-121,-60,62},{-132,-60,42},
 {-127,-60,34},{-119,-60,53},{-135,-60,67},{-125,-60,65},{-135,-60,26},
 {-118,-60,20},{-123,-60,7}};
 private static final SimpleChannel CHANNEL=NetworkRegistry.newSimpleChannel(new ResourceLocation(ID,"main"),()->"2",v->v.equals("2"),v->v.equals("2"));
 public record Show(String[] statuses){}
 public record Choose(int index){}
 private enum Phase {AVAILABLE,WAITING,COUNTDOWN,ACTIVE,CLEANING}
 private static final class Arena{
  final int index,dx;
  final LinkedHashSet<UUID> players=new LinkedHashSet<>();
  final Set<UUID> dead=new HashSet<>(),mobs=new HashSet<>();
  final ServerBossEvent bar;
  Phase phase=Phase.AVAILABLE;
  long deadline=0,restoreAt=0,lastSpawn=0;
  int wave=0,total=0,kills=0,spawned=0,cursor=0;
  boolean generated;
  Arena(int index){this.index=index;dx=index*300;generated=index==0;
   bar=new ServerBossEvent(Component.literal("ZOMBIES "+(index+1)),BossEvent.BossBarColor.RED,BossEvent.BossBarOverlay.PROGRESS);
  }
  String status(){return switch(phase){
   case AVAILABLE->generated?"DISPONIBLE 0/10":"NON GÉNÉRÉE";
   case WAITING->"ATTENTE "+players.size()+"/10 · "+Math.max(0,(deadline-tick+19)/20)+" s";
   case COUNTDOWN->"DÉPART "+players.size()+"/10";
   case ACTIVE->"EN COURS · "+kills+"/"+total;
   case CLEANING->"RESTAURATION";
  };}
  void announce(String header,String sub,int duration){
   for(UUID id:players){ServerPlayer p=find(id);if(p!=null)title(p,header,sub,duration);}
  }
  void updateBar(){bar.setName(Component.literal("ZOMBIES "+(index+1)+" · "+kills+" / "+total+" · Vague "+wave+"/3"));
   bar.setProgress(total==0?0f:Math.max(0f,Math.min(1f,(float)kills/total)));
  }
  int[] sizes(){int a=total/5,b=total*3/10;return new int[]{a,b,total-a-b};}
  int quota(){return sizes()[wave-1];}
  int alive(){int active=0;Iterator<UUID> it=mobs.iterator();
   while(it.hasNext()){Entity entity=server.overworld().getEntity(it.next());if(entity==null||!entity.isAlive())it.remove();else active++;}
   return active;
  }
  void join(ServerPlayer p){
   if(!generated||phase==Phase.CLEANING){message(p,"Arène non disponible.");return;}
   if(p.level().dimension()!=Level.OVERWORLD){message(p,"Rejoins le lobby d'abord.");return;}
   if(findArena(p.getUUID())!=null){message(p,"Tu es déjà dans une arène.");return;}
   if(phase!=Phase.AVAILABLE&&phase!=Phase.WAITING){message(p,"Partie déjà en cours.");return;}
   if(players.size()>=10){message(p,"Cette arène est complète.");return;}
   if(phase==Phase.AVAILABLE){phase=Phase.WAITING;deadline=tick+2400;}
   clearOverworld(p);players.add(p.getUUID());p.addTag(TAG);p.setInvulnerable(true);p.setGameMode(GameType.ADVENTURE);
   p.teleportTo(server.overworld(),-95.5+dx,-60,-7.5,0,0);
   announce("§6ZOMBIES "+(index+1),"§f"+players.size()+"/10 joueurs · Départ dans "+Math.max(0,(deadline-tick+19)/20)+" s",45);
  }
  void leave(ServerPlayer p){
   if(!players.remove(p.getUUID())){message(p,"Tu n'es pas dans cette arène.");return;}
   dead.remove(p.getUUID());lobby(p,this);
   if(players.isEmpty()||(phase==Phase.ACTIVE&&dead.containsAll(players)))finish(false);
  }
  void beginCountdown(){
   if(players.isEmpty()){finish(false);return;}
   phase=Phase.COUNTDOWN;deadline=tick+100;
   for(UUID id:players){ServerPlayer p=find(id);if(p!=null){
    // Y=-56 corrects the two-block underground spawn reported after V0.3.1.
    p.teleportTo(server.overworld(),-90.5+dx,-56,-3.5,30,4);p.setInvulnerable(false);
   }}
   announce("§6PRÉPAREZ-VOUS","§fRécupérez vos armes !",40);
  }
  void nextWave(){wave++;spawned=0;lastSpawn=tick-20;updateBar();
   announce("§cVAGUE "+wave+" / 3","§f"+quota()+" zombies à éliminer",70);
  }
  void start(){
   if(players.isEmpty()){finish(false);return;}
   phase=Phase.ACTIVE;total=100+50*(players.size()-1);kills=0;wave=0;dead.clear();
   for(UUID id:players){ServerPlayer p=find(id);if(p!=null)bar.addPlayer(p);}
   nextWave();
  }
  void spawn(){
   ServerLevel level=server.overworld();
   for(int tries=0;tries<SPAWNS.length;tries++){
    int[] pt=SPAWNS[cursor++%SPAWNS.length];
    BlockPos pos=new BlockPos(pt[0]+dx,pt[1],pt[2]);
    if(!level.getBlockState(pos).getCollisionShape(level,pos).isEmpty()
       ||!level.getBlockState(pos.above()).getCollisionShape(level,pos.above()).isEmpty())continue;
    Zombie z=EntityType.ZOMBIE.create(level);if(z==null)return;
    z.moveTo(pt[0]+dx+.5,pt[1],pt[2]+.5,level.random.nextFloat()*360,0);
    z.addTag(ZTAG);z.addTag("wb_arena_"+index);z.setPersistenceRequired();
    if(level.addFreshEntity(z)){mobs.add(z.getUUID());spawned++;}return;
   }
  }
  void tick(){
   if(phase==Phase.CLEANING){if(tick>=restoreAt)completeReset();return;}
   if(phase==Phase.WAITING){
    if(tick>=deadline)beginCountdown();
    else if(tick%20==0){
     int seconds=(int)Math.max(0,(deadline-tick+19)/20);
     for(UUID id:players){ServerPlayer p=find(id);if(p==null)continue;
      if(p.distanceToSqr(-95.5+dx,-60,-7.5)>9)p.teleportTo(server.overworld(),-95.5+dx,-60,-7.5,0,0);
      p.displayClientMessage(Component.literal("§6Zombies "+(index+1)+" §f· Attente §e"+(seconds/60)+":"+String.format("%02d",seconds%60)+" §7· "+players.size()+"/10"),true);
     }
    }
   }
   if(phase==Phase.COUNTDOWN){
    if(tick%20==0){int seconds=(int)Math.max(0,(deadline-tick+19)/20);
     if(seconds>=1&&seconds<=5)announce("§e"+seconds,"§fDébut de la vague 1",22);}
    if(tick>=deadline)start();
   }
   if(phase!=Phase.ACTIVE)return;
   int alive=alive();
   int completed=0;int[] quotas=sizes();for(int i=0;i<wave-1;i++)completed+=quotas[i];
   if(kills-completed>=quota()&&alive==0){if(wave==3)finish(true);else nextWave();return;}
   if(alive<18&&spawned<quota()&&tick-lastSpawn>=12){spawn();lastSpawn=tick;}
   if(tick%20==0)for(UUID id:dead){ServerPlayer p=find(id);if(p!=null){
    p.setInvulnerable(true);
    if(p.distanceToSqr(-95.5+dx,-60,-7.5)>16)p.teleportTo(server.overworld(),-95.5+dx,-60,-7.5,0,0);
   }}
  }
  void finish(boolean victory){
   if(phase==Phase.CLEANING)return;
   phase=Phase.CLEANING;
   for(UUID id:new ArrayList<>(players)){ServerPlayer p=find(id);if(p!=null){
    lobby(p,this);
    title(p,victory?"§aVICTOIRE !":"§cDÉFAITE",victory?"§fVous avez éliminé "+kills+" zombies !":"§fRetour au lobby",100);
   }}
   for(UUID id:mobs){Entity mob=server.overworld().getEntity(id);
    if(mob!=null&&mob.getTags().contains(ZTAG))mob.discard();}
   bar.removeAllPlayers();players.clear();dead.clear();mobs.clear();
   wave=0;kills=0;total=0;restoreAt=tick+20;
  }
  void completeReset(){
   restoreAt=Long.MAX_VALUE; // no restoration loops on failure
   if(restore(this)){phase=Phase.AVAILABLE;LOG.info("Zombies arena {} restored",index+1);}
   else LOG.error("Zombies arena {} locked: structure restore failed",index+1);
  }
 }
 private static final Arena[] ARENAS={new Arena(0),new Arena(1),new Arena(2)};
 private static MinecraftServer server;
 private static long tick=0;
 public WorldBattlesArenas(){
  CHANNEL.registerMessage(0,Show.class,(m,b)->{for(String s:m.statuses())b.writeUtf(s);},
    b->new Show(new String[]{b.readUtf(),b.readUtf(),b.readUtf()}),
    (m,c)->{c.get().enqueueWork(()->DistExecutor.unsafeRunWhenOn(Dist.CLIENT,()->()->ArenasClient.show(m.statuses())));c.get().setPacketHandled(true);},Optional.of(NetworkDirection.PLAY_TO_CLIENT));
  CHANNEL.registerMessage(1,Choose.class,(m,b)->b.writeInt(m.index()),b->new Choose(b.readInt()),
    (m,c)->{ServerPlayer p=c.get().getSender();c.get().enqueueWork(()->{if(p!=null)choose(p,m.index());});c.get().setPacketHandled(true);},Optional.of(NetworkDirection.PLAY_TO_SERVER));
  MinecraftForge.EVENT_BUS.register(this);
 }
 public static void select(int index){CHANNEL.sendToServer(new Choose(index));}
 private static ServerPlayer find(UUID id){return server==null?null:server.getPlayerList().getPlayer(id);}
 private static Arena findArena(UUID id){for(Arena a:ARENAS)if(a.players.contains(id))return a;return null;}
 private static void message(ServerPlayer p,String msg){p.sendSystemMessage(Component.literal("§6[Zombies] §f"+msg));}
 private static void title(ServerPlayer p,String header,String sub,int stay){
  p.connection.send(new ClientboundSetTitlesAnimationPacket(5,stay,10));
  p.connection.send(new ClientboundSetTitleTextPacket(Component.literal(header)));
  p.connection.send(new ClientboundSetSubtitleTextPacket(Component.literal(sub)));
 }
 private static void clearOverworld(ServerPlayer p){
  if(p.level().dimension()!=Level.OVERWORLD)return;
  p.getInventory().clearContent();p.containerMenu.broadcastChanges();
 }
 private static void lobby(ServerPlayer p,Arena arena){
  if(arena!=null)arena.bar.removePlayer(p);
  p.setInvulnerable(false);
  clearOverworld(p);p.removeTag(TAG);p.removeTag(LOCK);
  p.teleportTo(server.overworld(),23.5,-58,18.5,0,0);p.setGameMode(GameType.ADVENTURE);
 }
 private static void choose(ServerPlayer p,int index){
  if(index==-1){Arena arena=findArena(p.getUUID());if(arena!=null)arena.leave(p);return;}
  if(index<0||index>=ARENAS.length)return;
  ARENAS[index].join(p);
 }
 private static void show(ServerPlayer p){
  String[] statuses=new String[3];for(int i=0;i<3;i++)statuses[i]=ARENAS[i].status();
  CHANNEL.send(PacketDistributor.PLAYER.with(()->p),new Show(statuses));
 }
 private static Optional<StructureTemplate> template(){
  if(server==null)return Optional.empty();
  Optional<StructureTemplate> t=server.overworld().getStructureManager().get(STRUCTURE_ID);
  if(t.isEmpty()||!t.get().getSize().equals(SIZE)){
   LOG.error("Missing/invalid template minecraft:zombies, expected 65 x 10 x 101");return Optional.empty();}
  return t;
 }
 private static boolean restore(Arena a){
  Optional<StructureTemplate> t=template();if(t.isEmpty())return false;
  try{
   ServerLevel w=server.overworld();BlockPos origin=ORIGIN.offset(a.dx,0,0);
   w.getChunkAt(origin);w.getChunkAt(origin.offset(SIZE.getX()-1,0,SIZE.getZ()-1));
   StructurePlaceSettings settings=new StructurePlaceSettings().setMirror(Mirror.NONE).setRotation(Rotation.NONE).setIgnoreEntities(true);
   return t.get().placeInWorld(w,origin,origin,settings,w.random,2);
  }catch(Exception ex){LOG.error("Failed to restore Zombies {}",a.index+1,ex);return false;}
 }
 private static Path savedMarker(){
  return server.getWorldPath(LevelResource.ROOT).resolve("worldbattles-arenas-zombies-generated.properties");
 }
 private static void loadGenerated(){
  try{
   Properties props=new Properties();Path path=savedMarker();
   if(Files.exists(path)){try(var input=Files.newInputStream(path)){props.load(input);}}
   for(int i=1;i<3;i++)ARENAS[i].generated=Boolean.parseBoolean(props.getProperty("arena"+(i+1),"false"));
  }catch(Exception ex){LOG.error("Cannot read arenas generation flags",ex);}
 }
 private static void saveGenerated(){
  try{
   Properties props=new Properties();
   for(int i=1;i<3;i++)props.setProperty("arena"+(i+1),Boolean.toString(ARENAS[i].generated));
   try(var output=Files.newOutputStream(savedMarker())){props.store(output,"WorldBattles Arenas generated copies");}
  }catch(Exception ex){LOG.error("Cannot save arenas generation flags",ex);}
 }
 private static int generate(){
  if(template().isEmpty())return 0;
  int made=0;
  for(int i=1;i<3;i++){
   Arena a=ARENAS[i];if(a.generated)continue;
   if(a.phase!=Phase.AVAILABLE)continue;
   if(restore(a)){a.generated=true;made++;saveGenerated();
    LOG.info("Generated Zombies arena {} at X offset {}",i+1,a.dx);
   }else{LOG.error("Arena {} copy failed; generation stopped",i+1);break;}
  }
  return made;
 }
 @SubscribeEvent public void commands(RegisterCommandsEvent e){
  e.getDispatcher().register(Commands.literal("wbarenas")
   .then(Commands.literal("open").then(Commands.literal("zombies")
    .executes(c->{if(c.getSource().getEntity() instanceof ServerPlayer p){show(p);return 1;}return 0;})
    .then(Commands.argument("joueur",EntityArgument.player()).requires(s->s.hasPermission(2))
     .executes(c->{show(EntityArgument.getPlayer(c,"joueur"));return 1;}))))
   .then(Commands.literal("leave").executes(c->{if(c.getSource().getEntity() instanceof ServerPlayer p){Arena a=findArena(p.getUUID());if(a!=null)a.leave(p);return 1;}return 0;}))
   .then(Commands.literal("status").executes(c->{c.getSource().sendSuccess(()->Component.literal(
    "Zombies: "+ARENAS[0].status()+" | "+ARENAS[1].status()+" | "+ARENAS[2].status()),false);return 1;}))
   .then(Commands.literal("admin").requires(s->s.hasPermission(2))
    .then(Commands.literal("generate").executes(c->{int count=generate();
     c.getSource().sendSuccess(()->Component.literal("Arènes Zombies générées : "+count+" (déjà existantes ignorées)."),true);return count;}))
    .then(Commands.literal("reset").executes(c->{for(Arena a:ARENAS)if(a.generated)a.finish(false);return 1;}))
    .then(Commands.literal("start").executes(c->{int count=0;for(Arena a:ARENAS)if(a.phase==Phase.WAITING){a.beginCountdown();count++;}return count;}))));
 }
 @SubscribeEvent public void tick(TickEvent.ServerTickEvent e){
  if(e.phase!=TickEvent.Phase.END)return;
  if(server!=e.getServer()){server=e.getServer();loadGenerated();}
  tick++;for(Arena a:ARENAS)a.tick();
 }
 @SubscribeEvent public void deaths(LivingDeathEvent e){
  if(e.getEntity() instanceof Zombie z&&z.getTags().contains(ZTAG)){
   for(Arena a:ARENAS)if(a.mobs.remove(z.getUUID())){a.kills++;a.updateBar();break;}
   return;
  }
  if(e.getEntity() instanceof ServerPlayer p){
   Arena a=findArena(p.getUUID());
   if(a!=null&&a.phase==Phase.ACTIVE&&!a.dead.contains(p.getUUID())){
    e.setCanceled(true);p.setHealth(1f);a.dead.add(p.getUUID());p.setInvulnerable(true);
    p.teleportTo(server.overworld(),-95.5+a.dx,-60,-7.5,0,0);
    title(p,"§cÉLIMINÉ","§fAttends la fin de la partie",80);
    if(a.dead.containsAll(a.players))a.finish(false);
   }
  }
 }
 @SubscribeEvent public void logout(PlayerEvent.PlayerLoggedOutEvent e){
  if(e.getEntity() instanceof ServerPlayer p){
   Arena a=findArena(p.getUUID());if(a==null)return;
   a.players.remove(p.getUUID());a.dead.remove(p.getUUID());a.bar.removePlayer(p);
   p.removeTag(TAG);p.addTag(LOCK);p.setInvulnerable(false);
   if(a.players.isEmpty()||(a.phase==Phase.ACTIVE&&a.dead.containsAll(a.players)))a.finish(false);
  }
 }
 @SubscribeEvent public void login(PlayerEvent.PlayerLoggedInEvent e){
  if(e.getEntity() instanceof ServerPlayer p&&(p.getTags().contains(TAG)||p.getTags().contains(LOCK)))lobby(p,null);
 }
}
