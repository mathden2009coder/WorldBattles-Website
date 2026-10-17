package com.worldbattles.hammers;
import java.util.*;
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
 public WorldBattlesHammers(){ ITEMS.register(FMLJavaModLoadingContext.get().getModEventBus());MinecraftForge.EVENT_BUS.register(this); }
 private boolean hammer(ItemStack s){if(s.is(STRONG.get())||s.is(WEAK.get()))return true;CompoundTag t=s.getTag();return t!=null&&"hammer".equals(t.getString("type"))&&(t.contains("sandwars_tier")||t.contains("sandwars_weak"));}
 @SubscribeEvent(priority=EventPriority.LOWEST)
 public void broken(BlockEvent.BreakEvent e){
  if(e.isCanceled()||!(e.getPlayer() instanceof ServerPlayer p)||!(p.level() instanceof ServerLevel l)||p.isCreative()||p.isSpectator()||!p.isAlive()||!hammer(p.getMainHandItem()))return;
  BlockPos origin=e.getPos();BlockState source=e.getState();
  if(!source.is(BlockTags.MINEABLE_WITH_PICKAXE)||!p.hasCorrectToolForDrops(source)||source.getDestroySpeed(l,origin)<0)return;
  if(!working.add(p.getUUID()))return;
  try{
   HitResult ray=p.pick(6.0,0.0f,false);
   Direction face=ray instanceof BlockHitResult hit&&hit.getBlockPos().equals(origin)?hit.getDirection():Direction.getNearest(p.getLookAngle().x,p.getLookAngle().y,p.getLookAngle().z);
   for(int a=-1;a<=1;a++)for(int b=-1;b<=1;b++){
    if(a==0&&b==0)continue;
    BlockPos at=switch(face.getAxis()){case X->origin.offset(0,a,b);case Y->origin.offset(a,0,b);case Z->origin.offset(a,b,0);};
    if(!l.hasChunkAt(at))continue;BlockState state=l.getBlockState(at);
    if(state.isAir()||state.hasBlockEntity()||!state.is(BlockTags.MINEABLE_WITH_PICKAXE)||!p.hasCorrectToolForDrops(state)||state.getDestroySpeed(l,at)<0)continue;
    p.gameMode.destroyBlock(at);
   }
  }finally{working.remove(p.getUUID());}
 }
}