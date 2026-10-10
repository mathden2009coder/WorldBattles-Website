package com.worldbattles.gunrules;

import net.minecraftforge.common.ForgeConfigSpec;

/** Default to a reliable Forge-event bench ban; selective Mixin remains optional. */
public final class GunRulesConfig {
    public static final ForgeConfigSpec SPEC;
    public static final ForgeConfigSpec.BooleanValue STRICT_BENCH_BLOCK;

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
        builder.comment(
            "Freelands protection for TaCZ guns.",
            "true (recommended): blocks opening any TaCZ gunsmith bench in Freelands.",
            "This also prevents crafting TaCZ ammo and attachments in those dimensions.",
            "false: allows benches and uses the selective TaCZ Mixin to block only guns.",
            "If the Mixin is missing, a fallback still blocks the benches."
        ).push("crafting");
        STRICT_BENCH_BLOCK = builder
            .define("blockAllTaczWorkbenchesInFreelands", true);
        builder.pop();
        SPEC = builder.build();
    }

    private GunRulesConfig() {}
}
