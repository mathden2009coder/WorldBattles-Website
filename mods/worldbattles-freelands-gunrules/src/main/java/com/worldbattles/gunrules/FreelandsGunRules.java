package com.worldbattles.gunrules;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.event.RegisterCommandsEvent;

import java.util.Set;

/** Protect only the four existing Freelands dimensions. */
@Mod(FreelandsGunRules.MODID)
public final class FreelandsGunRules {
    public static final String MODID = "worldbattlesgunrules";

    private static final Set<ResourceLocation> FREELANDS = Set.of(
        new ResourceLocation("secondworld", "overworld"),
        new ResourceLocation("secondworld", "the_nether"),
        new ResourceLocation("minage", "overworld"),
        new ResourceLocation("minage", "the_nether")
    );

    public FreelandsGunRules() {
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, GunRulesConfig.SPEC);
        MinecraftForge.EVENT_BUS.register(new GunSmithBenchGuard());
        MinecraftForge.EVENT_BUS.addListener(this::registerCommands);
    }

    public static boolean isFreelands(ResourceLocation dimensionId) {
        return FREELANDS.contains(dimensionId);
    }

    private void registerCommands(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();
        dispatcher.register(Commands.literal("wbgunrules")
            .requires(source -> source.hasPermission(2))
            .executes(ctx -> {
                ServerPlayer player = ctx.getSource().getPlayerOrException();
                ResourceLocation dim = player.serverLevel().dimension().location();
                String mode = GunRulesConfig.STRICT_BENCH_BLOCK.get()
                    ? "SECURITE : etablis TaCZ bloques"
                    : (GunSmithBenchGuard.isSelectiveMixinInstalled()
                        ? "SELECTIF : seulement les fusils bloques"
                        : "SECOURS : etablis bloques (Mixin absent)");
                player.sendSystemMessage(Component.literal(
                    "[WorldBattles] Dimension: " + dim
                    + " | Freelands: " + isFreelands(dim)
                    + " | Mode: " + mode));
                return 1;
            }));
    }
}
