package com.worldbattles.windplate;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.function.Supplier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterials;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

@Mod(WindPlate.ID)
public final class WindPlate {
  public static final String ID="worldbattleswindplate";
  private static final String PROTOCOL="1";
  private static final SimpleChannel CHANNEL=NetworkRegistry.newSimpleChannel(
      new net.minecraft.resources.ResourceLocation(ID,"jump"),()->PROTOCOL,PROTOCOL::equals,PROTOCOL::equals);
  private static final DeferredRegister<Item> ITEMS=DeferredRegister.create(ForgeRegistries.ITEMS,ID);
  public static final RegistryObject<Item> PLATE=ITEMS.register("wind_chestplate",()->new ArmorItem(ArmorMaterials.NETHERITE,ArmorItem.Type.CHESTPLATE,new Item.Properties().fireResistant()){
    @Override public String getArmorTexture(ItemStack stack, net.minecraft.world.entity.Entity entity, EquipmentSlot slot, String type){
      return ID+":textures/models/armor/wind_layer_1.png";
    }
  });
  private static final Set<UUID> USED=new HashSet<>();
  public WindPlate(){
    IEventBus bus=FMLJavaModLoadingContext.get().getModEventBus();
    ITEMS.register(bus);
    CHANNEL.registerMessage(0,JumpMessage.class,(m,b)->{},b->new JumpMessage(),WindPlate::onJump);
    MinecraftForge.EVENT_BUS.register(this);
    DistExecutor.unsafeRunWhenOn(Dist.CLIENT,()->()->ClientEvents.init());
  }
  private static void onJump(JumpMessage msg,Supplier<NetworkEvent.Context> context){
    NetworkEvent.Context ctx=context.get();
    ctx.enqueueWork(()->{
      ServerPlayer p=ctx.getSender();
      if(p==null||p.isSpectator()||p.getAbilities().flying||p.isPassenger()||p.isInWaterOrBubble()||p.isInLava()||p.onGround())return;
      if(!p.getItemBySlot(EquipmentSlot.CHEST).is(PLATE.get()))return;
      if(!USED.add(p.getUUID()))return;
      Vec3 motion=p.getDeltaMovement();
      p.setDeltaMovement(motion.x,0.68,motion.z);
      p.hurtMarked=true;
      p.serverLevel().sendParticles(net.minecraft.core.particles.ParticleTypes.CLOUD,p.getX(),p.getY()+0.2,p.getZ(),14,0.4,0.1,0.4,0.035);
    });
    ctx.setPacketHandled(true);
  }
  @SubscribeEvent public void tick(TickEvent.PlayerTickEvent e){
    if(e.phase!=TickEvent.Phase.END || e.player.level().isClientSide)return;
    if(e.player.onGround() || e.player.isInWaterOrBubble() || e.player.isInLava() || e.player.isPassenger()) USED.remove(e.player.getUUID());
  }
  @SubscribeEvent public void logout(net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedOutEvent e){USED.remove(e.getEntity().getUUID());}
  public static final class JumpMessage {}
  @net.minecraftforge.api.distmarker.OnlyIn(Dist.CLIENT)
  public static final class ClientEvents {
    private static void init(){MinecraftForge.EVENT_BUS.register(new ClientEvents());}
    @SubscribeEvent public void key(net.minecraftforge.client.event.InputEvent.Key e){
      if(e.getKey()!=org.lwjgl.glfw.GLFW.GLFW_KEY_SPACE || e.getAction()!=org.lwjgl.glfw.GLFW.GLFW_PRESS)return;
      net.minecraft.client.Minecraft mc=net.minecraft.client.Minecraft.getInstance();
      if(mc.player==null || mc.screen!=null || mc.player.onGround() || mc.player.getAbilities().flying)return;
      if(!mc.player.getItemBySlot(EquipmentSlot.CHEST).is(PLATE.get()))return;
      CHANNEL.sendToServer(new JumpMessage());
    }
  }
}