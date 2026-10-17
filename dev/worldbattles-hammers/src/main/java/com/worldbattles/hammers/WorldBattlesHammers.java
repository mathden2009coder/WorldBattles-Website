package com.worldbattles.hammers;
import java.util.*;
import net.minecraftforge.event.TickEvent;
import net.minecraft.core.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.*;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.*;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.*;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.*;

@Mod(WorldBattlesHammers.MOD_ID)
public final class WorldBattlesHammers {
 public static final String MOD_ID="worldbattleshammers";
 private static final DeferredRegister<Item> ITEMS=DeferredRegister.create(ForgeRegistries.ITEMS,MOD_ID);
 public static final RegistryObject<Item> STRONG=ITEMS.register("netherite_diamond_hammer",()->new PickaxeItem(Tiers.NETHERITE,1,-2.8F,new Item.Properties().stacksTo(1).fireResistant()){ @Override public boolean isDamageable(ItemStack s){return false;} });
 public static final RegistryObject<Item> WEAK=ITEMS.register("iron_gold_hammer",()->new PickaxeItem(Tiers.IRON,1,-2.8F,new Item.Properties().stacksTo(1)){ @Override public boolean isDamageable(ItemStack s){return false;} });
 private final Set<UUID> working=new HashSet<>();
 private final Map<UUID,ArrayDeque<BlockPos>> pending=new HashMap<>();
 private final Map<UUID,Long> lastMine=new HashMap<>();
 public WorldBattlesHammers(){ ITEMS.register(FMLJavaModLoadingContext.get().getModEventBus());MinecraftForge.EVENT_BUS.register(this); }
 private boolean hammer(ItemStack s){if(s.is(STRONG.get())||s.is(WEAK.get()))return true;CompoundTag t=s.getTag();return t!=null&&"hammer".equals(t.getString("type"))&&(t.contains("sandwars_tier")||t.contains("sandwars_weak"));}

 @SubscribeEvent(priority=EventPriority.LOWEST)
 public void broken(BlockEvent.BreakEvent e){
  if(e.isCanceled()||!(e.getPlayer() instanceof ServerPlayer p)||!(p.level() instanceof ServerLevel l)
   ||p.isCreative()||p.isSpectator()||!p.isAlive()||!hammer(p.getMainHandItem()))return;
  UUID id=p.getUUID();
  if(working.contains(id)||pending.containsKey(id))return;
  long time=l.getGameTime();
  if(time-lastMine.getOrDefault(id,-100L)<8)return;
  BlockPos origin=e.getPos();BlockState source=e.getState();
  if(!source.is(BlockTags.MINEABLE_WITH_PICKAXE)||!p.hasCorrectToolForDrops(source)
    ||source.getDestroySpeed(l,origin)<0)return;
  HitResult ray=p.pick(6,0,false);
  Direction face=ray instanceof BlockHitResult hit&&hit.getBlockPos().equals(origin)
    ?hit.getDirection():Direction.getNearest(p.getLookAngle().x,p.getLookAngle().y,p.getLookAngle().z);
  ArrayDeque<BlockPos> queue=new ArrayDeque<>();
  for(int a=-1;a<=1;a++)for(int b=-1;b<=1;b++){
   if(a==0&&b==0)continue;
   BlockPos pos=switch(face.getAxis()){case X->origin.offset(0,a,b);case Y->origin.offset(a,0,b);case Z->origin.offset(a,b,0);};
   if(l.hasChunkAt(pos))queue.add(pos.immutable());
  }
  if(!queue.isEmpty()){pending.put(id,queue);lastMine.put(id,time);}
 }
 @SubscribeEvent
 public void tick(TickEvent.PlayerTickEvent e){
  if(e.phase!=TickEvent.Phase.END||!(e.player instanceof ServerPlayer p))return;
  UUID id=p.getUUID();ArrayDeque<BlockPos> queue=pending.get(id);if(queue==null)return;
  if(!p.isAlive()||p.isCreative()||p.isSpectator()||!hammer(p.getMainHandItem())){pending.remove(id);return;}
  if(!working.add(id))return;
  try{
   int budget=2;
   while(budget-->0&&!queue.isEmpty()){
    BlockPos pos=queue.poll();ServerLevel l=p.serverLevel();
    if(!l.hasChunkAt(pos)||p.distanceToSqr(pos.getX()+.5,pos.getY()+.5,pos.getZ()+.5)>64)continue;
    BlockState state=l.getBlockState(pos);
    if(state.isAir()||state.hasBlockEntity()||!state.is(BlockTags.MINEABLE_WITH_PICKAXE)
      ||state.getDestroySpeed(l,pos)<0||!p.hasCorrectToolForDrops(state))continue;
    p.gameMode.destroyBlock(pos);
   }
  }finally{working.remove(id);if(queue.isEmpty())pending.remove(id);}
 }
}
