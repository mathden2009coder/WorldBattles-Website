package com.worldbattles.commands;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.Scoreboard;
import net.minecraft.world.scores.criteria.ObjectiveCriteria;

/** Manual pause is persistent in scoreboard objective fj_pause. */
public final class JobsPauseCommands {
    public static final String OBJECTIVE = "fj_pause";

    private JobsPauseCommands() {}

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("metiersadmin")
            .requires(source -> source.hasPermission(2))
            .then(Commands.literal("pause")
                .executes(ctx -> setPause(ctx.getSource(), ctx.getSource().getPlayerOrException(), true))
                .then(Commands.argument("joueur", EntityArgument.player())
                    .executes(ctx -> setPause(ctx.getSource(), EntityArgument.getPlayer(ctx, "joueur"), true))))
            .then(Commands.literal("resume")
                .executes(ctx -> setPause(ctx.getSource(), ctx.getSource().getPlayerOrException(), false))
                .then(Commands.argument("joueur", EntityArgument.player())
                    .executes(ctx -> setPause(ctx.getSource(), EntityArgument.getPlayer(ctx, "joueur"), false))))
            .then(Commands.literal("status")
                .executes(ctx -> status(ctx.getSource(), ctx.getSource().getPlayerOrException()))
                .then(Commands.argument("joueur", EntityArgument.player())
                    .executes(ctx -> status(ctx.getSource(), EntityArgument.getPlayer(ctx, "joueur"))))));
    }

    private static Objective ensureObjective(Scoreboard board) {
        Objective objective = board.getObjective(OBJECTIVE);
        if (objective == null) {
            objective = board.addObjective(OBJECTIVE, ObjectiveCriteria.DUMMY,
                Component.literal("Pause metiers"), ObjectiveCriteria.RenderType.INTEGER);
        }
        return objective;
    }

    private static int setPause(CommandSourceStack source, ServerPlayer player, boolean paused) {
        Scoreboard board = source.getServer().getScoreboard();
        Objective objective = ensureObjective(board);
        board.getOrCreatePlayerScore(player.getScoreboardName(), objective).setScore(paused ? 1 : 0);
        String state = paused ? "en pause" : "reactives";
        source.sendSuccess(() -> Component.literal("[WorldBattles] Metiers " + state
            + " pour " + player.getScoreboardName() + "."), true);
        if (source.getEntity() != player) {
            player.sendSystemMessage(Component.literal("[WorldBattles] Tes metiers sont " + state + "."));
        }
        return 1;
    }

    private static int status(CommandSourceStack source, ServerPlayer player) {
        Scoreboard board = source.getServer().getScoreboard();
        Objective objective = board.getObjective(OBJECTIVE);
        boolean manualPause = objective != null
            && board.hasPlayerScore(player.getScoreboardName(), objective)
            && board.getOrCreatePlayerScore(player.getScoreboardName(), objective).getScore() == 1;
        boolean creative = player.isCreative();
        String status = manualPause || creative ? "PAUSE" : "ACTIFS";
        source.sendSuccess(() -> Component.literal("[WorldBattles] " + player.getScoreboardName()
            + " : " + status + " (pause admin=" + manualPause + ", creatif=" + creative + ")."), false);
        return 1;
    }
}
