package com.worldbattles.jobs;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.Scoreboard;

/** Server-authoritative snapshot. No score is modified or reset by this mod. */
public record JobSnapshot(boolean installed, boolean openScreen, int[] levels, int[] xp, int[] needed) {
    public static JobSnapshot capture(ServerPlayer player) {
        Scoreboard board = player.getScoreboard();
        boolean found = board.getObjective("fj_sys") != null;
        for (JobCatalog.Job job : JobCatalog.JOBS) {
            found &= board.getObjective(job.levelObjective()) != null;
            found &= board.getObjective(job.xpObjective()) != null;
            found &= board.getObjective(job.needObjective()) != null;
        }
        int[] lv = new int[JobCatalog.COUNT];
        int[] ex = new int[JobCatalog.COUNT];
        int[] req = new int[JobCatalog.COUNT];
        if (found) {
            for (int i = 0; i < JobCatalog.COUNT; i++) {
                JobCatalog.Job job = JobCatalog.JOBS[i];
                lv[i] = Math.max(0, Math.min(JobCatalog.MAX_LEVEL, readScore(board, player, job.levelObjective())));
                ex[i] = Math.max(0, readScore(board, player, job.xpObjective()));
                req[i] = Math.max(0, readScore(board, player, job.needObjective()));
            }
        }
        return new JobSnapshot(found, true, lv, ex, req);
    }

    private static int readScore(Scoreboard board, ServerPlayer player, String name) {
        Objective objective = board.getObjective(name);
        if (objective == null || !board.hasPlayerScore(player.getScoreboardName(), objective)) return 0;
        return board.getOrCreatePlayerScore(player.getScoreboardName(), objective).getScore();
    }

    public JobSnapshot asRefresh() {
        return new JobSnapshot(installed, false, levels, xp, needed);
    }

    public static void encode(JobSnapshot snapshot, FriendlyByteBuf buf) {
        buf.writeBoolean(snapshot.installed);
        buf.writeBoolean(snapshot.openScreen);
        for (int i = 0; i < JobCatalog.COUNT; i++) {
            buf.writeVarInt(snapshot.levels[i]);
            buf.writeVarInt(snapshot.xp[i]);
            buf.writeVarInt(snapshot.needed[i]);
        }
    }

    public static JobSnapshot decode(FriendlyByteBuf buf) {
        boolean installed = buf.readBoolean();
        boolean openScreen = buf.readBoolean();
        int[] lv = new int[JobCatalog.COUNT];
        int[] ex = new int[JobCatalog.COUNT];
        int[] req = new int[JobCatalog.COUNT];
        for (int i = 0; i < JobCatalog.COUNT; i++) {
            lv[i] = buf.readVarInt();
            ex[i] = buf.readVarInt();
            req[i] = buf.readVarInt();
        }
        return new JobSnapshot(installed, openScreen, lv, ex, req);
    }
}
