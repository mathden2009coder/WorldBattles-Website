package com.worldbattles.companion;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.CommandEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
@Mod("worldbattlescompanion")
public class WorldBattlesCompanion {
 private static final String PAUSE_TAG="wb_jobs_paused";
 private static final String ARENA_TAG="wb_arena_active";
 public WorldBattlesCompanion(){MinecraftForge.EVENT_BUS.register(this);}
 /**
  * Only intercept player-issued commands. Command blocks and console must remain
  * operational for arena control and emergency administration.
  * This tag is assigned/removed by the separate WorldBattles Arenas mod.
  */
 @SubscribeEvent public void restrictCommands(CommandEvent event){
  CommandSourceStack source=event.getParseResults().getContext().getSource();
  if(!(source.getEntity() instanceof ServerPlayer player))return;
  if(source.hasPermission(2))return;
  String entered=event.getParseResults().getReader().getString().trim();
  if(entered.startsWith("/"))entered=entered.substring(1);
  String label=entered.split("\\s+",2)[0].toLowerCase(java.util.Locale.ROOT);
  boolean inGame=player.getTags().contains(ARENA_TAG);
  String namespace=player.level().dimension().location().getNamespace();
  boolean inSurvivalWorld=namespace.equals("secondworld")||namespace.equals("minage");
  if(inGame){
   event.setCanceled(true);
   source.sendFailure(Component.literal("Commandes indisponibles pendant une partie de mini-jeu."));
  }else if((label.equals("jobs")||label.equals("wbshop"))&&!inSurvivalWorld){
   event.setCanceled(true);
   source.sendFailure(Component.literal("Cette commande est réservée à Freelands et Minage."));
  }
 }
 @SubscribeEvent public void registerCommands(RegisterCommandsEvent event){
 CommandDispatcher<CommandSourceStack> d=event.getDispatcher();
 d.register(Commands.literal("spawn").requires(s->s.getEntity() instanceof ServerPlayer).executes(c->teleport(c.getSource(),"minecraft:overworld",23,-58,18)));
 d.register(Commands.literal("freelands").requires(s->s.getEntity() instanceof ServerPlayer).executes(c->teleport(c.getSource(),"secondworld:overworld",-541,120,1209)));
 d.register(Commands.literal("minage").requires(s->s.getEntity() instanceof ServerPlayer).executes(c->teleport(c.getSource(),"minage:overworld",-541,120,1209)));
 d.register(Commands.literal("metiersadmin").requires(s->s.hasPermission(2))
 .then(Commands.literal("pause").then(Commands.argument("joueur",EntityArgument.player()).executes(c->setPaused(c.getSource(),EntityArgument.getPlayer(c,"joueur"),true))))
 .then(Commands.literal("resume").then(Commands.argument("joueur",EntityArgument.player()).executes(c->setPaused(c.getSource(),EntityArgument.getPlayer(c,"joueur"),false))))
 .then(Commands.literal("status").then(Commands.argument("joueur",EntityArgument.player()).executes(c->status(c.getSource(),EntityArgument.getPlayer(c,"joueur"))))));
 }
 private static int teleport(CommandSourceStack src,String id,double x,double y,double z) throws com.mojang.brigadier.exceptions.CommandSyntaxException{
 ServerPlayer p=src.getPlayerOrException();
  if(!src.hasPermission(2)&&p.getTags().contains(ARENA_TAG)){
   src.sendFailure(Component.literal("Impossible de se téléporter pendant un mini-jeu."));return 0;
  }
  if(id.equals("minage:overworld")&&!src.hasPermission(2)){
   String namespace=p.level().dimension().location().getNamespace();
   if(!namespace.equals("secondworld")&&!namespace.equals("minage")){
    src.sendFailure(Component.literal("/minage est disponible uniquement depuis Freelands ou Minage."));return 0;
   }
  }
 ResourceKey<Level> key=ResourceKey.create(Registries.DIMENSION,new ResourceLocation(id));
 ServerLevel level=src.getServer().getLevel(key);
 if(level==null){src.sendFailure(Component.literal("Dimension introuvable : "+id));return 0;}
 p.teleportTo(level,x+0.5,y,z+0.5,0f,0f);
 src.sendSuccess(()->Component.literal("Téléportation vers "+id),false);return 1;
 }
 private static int setPaused(CommandSourceStack src,ServerPlayer p,boolean paused){
 if(paused)p.addTag(PAUSE_TAG);else p.removeTag(PAUSE_TAG);
 src.sendSuccess(()->Component.literal("Métiers de "+p.getGameProfile().getName()+(paused?" : en pause":" : actifs")),true);return 1;
 }
 private static int status(CommandSourceStack src,ServerPlayer p){
 src.sendSuccess(()->Component.literal("Métiers de "+p.getGameProfile().getName()+(p.isCreative()||p.getTags().contains(PAUSE_TAG)?" : en pause":" : actifs")),false);return 1;
 }
}
