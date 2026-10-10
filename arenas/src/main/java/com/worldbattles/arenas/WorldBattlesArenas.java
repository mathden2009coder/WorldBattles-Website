package com.worldbattles.arenas;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
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
import java.util.*;
@Mod(WorldBattlesArenas.ID)
public class WorldBattlesArenas {
 public static final String ID="worldbattlesarenas";
 private static final String TAG="wb_arena_active", ZTAG="wb_arena_zombie", LOCK="wb_arena_recover";
 private static final int[][] SPAWNS={
 {-96,-60,24},{-92,-60,46},{-113,-60,55},{-101,-60,49},{-112,-60,78},
 {-96,-60,67},{-92,-60,82},{-125,-60,77},{-121,-60,62},{-132,-60,42},
 {-127,-60,34},{-119,-60,53},{-135,-60,67},{-125,-60,65},{-135,-60,26},
 {-118,-60,20},{-123,-60,7}};
 private static final SimpleChannel CHANNEL=NetworkRegistry.newSimpleChannel(new ResourceLocation(ID,"main"),()->"1",v->v.equals("1"),v->v.equals("1"));
 public record Show(String status){}
 public record Choose(boolean join){}
 private static final LinkedHashSet<UUID> players=new LinkedHashSet<>();
 private static final Set<UUID> dead=new HashSet<>(), mobs=new HashSet<>();
 private enum Phase {AVAILABLE,WAITING,COUNTDOWN,ACTIVE,CLEANING}
 private static Phase phase=Phase.AVAILABLE;
 private static MinecraftServer server;
 private static long tick=0,deadline=0,lastSpawn=0;
 private static int wave=0,total=0,kills=0,spawned=0,cursor=0;
 public WorldBattlesArenas(){
  CHANNEL.registerMessage(0,Show.class,(m,b)->b.writeUtf(m.status()),b->new Show(b.readUtf()),
   (m,c)->{c.get().enqueueWork(()->DistExecutor.unsafeRunWhenOn(Dist.CLIENT,()->()->ArenasClient.show(m.status())));c.get().setPacketHandled(true);},Optional.of(NetworkDirection.PLAY_TO_CLIENT));
  CHANNEL.registerMessage(1,Choose.class,(m,b)->b.writeBoolean(m.join()),b->new Choose(b.readBoolean()),
   (m,c)->{ServerPlayer p=c.get().getSender();c.get().enqueueWork(()->{if(p!=null){if(m.join())join(p);else leave(p);}});c.get().setPacketHandled(true);},Optional.of(NetworkDirection.PLAY_TO_SERVER));
  MinecraftForge.EVENT_BUS.register(this);
 }
 public static void select(boolean join){CHANNEL.sendToServer(new Choose(join));}
 private static void say(ServerPlayer p,String msg){p.sendSystemMessage(Component.literal("§6[Zombies] §f"+msg));}
 private static void everyone(String msg){if(server!=null)for(UUID id:players){ServerPlayer p=server.getPlayerList().getPlayer(id);if(p!=null)say(p,msg);}}
 private static String status(){return switch(phase){
  case AVAILABLE->"DISPONIBLE - 0/10";
  case WAITING->"ATTENTE - "+players.size()+"/10 - "+Math.max(0,(deadline-tick+19)/20)+" sec";
  case COUNTDOWN->"DEPART IMMINENT - "+players.size()+"/10";
  case ACTIVE->"PARTIE EN COURS - vague "+wave+"/3 - "+kills+"/"+total;
  case CLEANING->"NETTOYAGE EN COURS";
 };}
 private static void show(ServerPlayer p){CHANNEL.send(PacketDistributor.PLAYER.with(()->p),new Show(status()));}
 private static void lobby(ServerPlayer p){
  ServerLevel level=server.overworld();
  p.setInvulnerable(false);p.removeTag(TAG);p.removeTag(LOCK);
  p.teleportTo(level,23.5,-58,18.5,0,0);p.setGameMode(GameType.ADVENTURE);
  // Safety: never clear inventory or restore structures in this early test build.
 }
 private static void join(ServerPlayer p){
  if(p.level().dimension()!=Level.OVERWORLD){say(p,"Rejoins le lobby d'abord.");return;}
  if(phase!=Phase.AVAILABLE&&phase!=Phase.WAITING){say(p,"Partie deja en cours.");return;}
  if(players.contains(p.getUUID())){say(p,"Tu es deja inscrit.");return;}
  if(players.size()>=10){say(p,"Partie pleine.");return;}
  if(phase==Phase.AVAILABLE){phase=Phase.WAITING;deadline=tick+2400;}
  players.add(p.getUUID());p.addTag(TAG);p.setGameMode(GameType.ADVENTURE);
  p.teleportTo(server.overworld(),-90.5,-60,-3.5,30,4);
  everyone(p.getGameProfile().getName()+" a rejoint ("+players.size()+"/10).");
  if(players.size()==10){phase=Phase.COUNTDOWN;deadline=tick+100;everyone("Complet : debut dans 5 secondes !");}
 }
 private static void leave(ServerPlayer p){
  if(!players.remove(p.getUUID())){say(p,"Tu n'es pas inscrit.");return;}
  dead.remove(p.getUUID());lobby(p);
  if(players.isEmpty())finish(false);
  else if(phase==Phase.ACTIVE && dead.containsAll(players))finish(false);
 }
 private static int[] sizes(){int a=total/5,b=total*3/10;return new int[]{a,b,total-a-b};}
 private static int waveQuota(){return sizes()[wave-1];}
 private static void nextWave(){wave++;spawned=0;lastSpawn=tick-20;everyone("Vague "+wave+"/3 - "+waveQuota()+" zombies.");}
 private static void start(){
  if(players.isEmpty()){finish(false);return;}
  phase=Phase.ACTIVE;total=100+50*(players.size()-1);wave=0;kills=0;dead.clear();nextWave();
 }
 private static void spawn(){
  ServerLevel level=server.overworld();
  for(int tries=0;tries<SPAWNS.length;tries++){
   int[] pt=SPAWNS[cursor++%SPAWNS.length];BlockPos pos=new BlockPos(pt[0],pt[1],pt[2]);
   if(!level.getBlockState(pos).getCollisionShape(level,pos).isEmpty())continue;
   if(!level.getBlockState(pos.above()).getCollisionShape(level,pos.above()).isEmpty())continue;
   Zombie zombie=EntityType.ZOMBIE.create(level);if(zombie==null)return;
   zombie.moveTo(pt[0]+.5,pt[1],pt[2]+.5,level.random.nextFloat()*360,0);
   zombie.addTag(ZTAG);zombie.setPersistenceRequired();
   if(level.addFreshEntity(zombie)){mobs.add(zombie.getUUID());spawned++;}return;
  }
  // Do not count an unsuccessful spawn. Administrator can reset the test.
 }
 private static void finish(boolean victory){
  phase=Phase.CLEANING;
  if(server!=null){
   ServerLevel level=server.overworld();
   for(UUID id:new ArrayList<>(players)){ServerPlayer p=server.getPlayerList().getPlayer(id);if(p!=null){lobby(p);say(p,victory?"VICTOIRE !":"Partie terminee.");}}
   for(UUID id:mobs){Entity mob=level.getEntity(id);if(mob!=null&&mob.getTags().contains(ZTAG))mob.discard();}
  }
  players.clear();dead.clear();mobs.clear();wave=0;kills=0;total=0;phase=Phase.AVAILABLE;
 }
 @SubscribeEvent public void commands(RegisterCommandsEvent e){
  e.getDispatcher().register(Commands.literal("wbarenas")
   .then(Commands.literal("open").then(Commands.literal("zombies")
    .executes(c->{if(c.getSource().getEntity() instanceof ServerPlayer p){show(p);return 1;}return 0;})
    .then(Commands.argument("joueur",EntityArgument.player()).requires(s->s.hasPermission(2))
     .executes(c->{show(EntityArgument.getPlayer(c,"joueur"));return 1;}))))
   .then(Commands.literal("leave").executes(c->{if(c.getSource().getEntity() instanceof ServerPlayer p){leave(p);return 1;}return 0;}))
   .then(Commands.literal("status").executes(c->{c.getSource().sendSuccess(()->Component.literal(status()),false);return 1;}))
   .then(Commands.literal("admin").requires(s->s.hasPermission(2))
    .then(Commands.literal("reset").executes(c->{finish(false);return 1;}))
    .then(Commands.literal("start").executes(c->{if(phase==Phase.WAITING){phase=Phase.COUNTDOWN;deadline=tick+100;everyone("Debut dans 5 secondes !");return 1;}return 0;}))));
 }
 @SubscribeEvent public void onTick(TickEvent.ServerTickEvent e){
  if(e.phase!=TickEvent.Phase.END)return;server=e.getServer();tick++;
  if(phase==Phase.WAITING&&tick>=deadline){phase=Phase.COUNTDOWN;deadline=tick+100;everyone("Debut dans 5 secondes !");}
  if(phase==Phase.COUNTDOWN&&tick>=deadline)start();
  if(phase!=Phase.ACTIVE)return;
  ServerLevel level=server.overworld();int active=0;
  Iterator<UUID> it=mobs.iterator();
  while(it.hasNext()){Entity entity=level.getEntity(it.next());if(entity==null||!entity.isAlive())it.remove();else active++;}
  int completed=0;int[] quotas=sizes();for(int i=0;i<wave-1;i++)completed+=quotas[i];
  if(kills-completed>=waveQuota()&&active==0){if(wave==3)finish(true);else nextWave();return;}
  if(active<18&&spawned<waveQuota()&&tick-lastSpawn>=12){spawn();lastSpawn=tick;}
  if(tick%20==0){for(UUID id:dead){ServerPlayer p=server.getPlayerList().getPlayer(id);if(p!=null&&p.level().dimension()==Level.OVERWORLD){p.setInvulnerable(true);if(p.distanceToSqr(-95.5,-60,-7.5)>16)p.teleportTo(level,-95.5,-60,-7.5,0,0);}}}
  if(tick%100==0)everyone("Vague "+wave+"/3 | "+kills+"/"+total+" | "+active+" zombies actifs");
 }
 @SubscribeEvent public void onDeath(LivingDeathEvent e){
  if(e.getEntity() instanceof Zombie z&&z.getTags().contains(ZTAG)){if(mobs.remove(z.getUUID()))kills++;return;}
  if(e.getEntity() instanceof ServerPlayer p&&phase==Phase.ACTIVE&&players.contains(p.getUUID())&&!dead.contains(p.getUUID())){
   e.setCanceled(true);p.setHealth(1f);dead.add(p.getUUID());p.setInvulnerable(true);
   p.teleportTo(server.overworld(),-95.5,-60,-7.5,0,0);
   say(p,"Elimine. Attends la fin de la partie dans la salle.");
   if(dead.containsAll(players))finish(false);
  }
 }
 @SubscribeEvent public void logout(PlayerEvent.PlayerLoggedOutEvent e){
  if(e.getEntity() instanceof ServerPlayer p&&players.remove(p.getUUID())){
   dead.remove(p.getUUID());p.removeTag(TAG);p.addTag(LOCK);p.setInvulnerable(false);
   if(players.isEmpty()||(phase==Phase.ACTIVE&&dead.containsAll(players)))finish(false);
  }
 }
 @SubscribeEvent public void login(PlayerEvent.PlayerLoggedInEvent e){
  if(e.getEntity() instanceof ServerPlayer p&&(p.getTags().contains(TAG)||p.getTags().contains(LOCK)))lobby(p);
 }
}
