package com.worldbattles.gunrules;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fml.common.Mod;

import java.util.Set;

/** Only the four existing Freelands dimensions are affected. */
@Mod(FreelandsGunRules.MODID)
public final class FreelandsGunRules {
    public static final String MODID = "worldbattlesgunrules";

    private static final Set<ResourceLocation> FREELANDS = Set.of(
        new ResourceLocation("secondworld", "overworld"),
        new ResourceLocation("secondworld", "the_nether"),
        new ResourceLocation("minage", "overworld"),
        new ResourceLocation("minage", "the_nether")
    );

    public static boolean isFreelands(ResourceLocation dimensionId) {
        return FREELANDS.contains(dimensionId);
    }
}
