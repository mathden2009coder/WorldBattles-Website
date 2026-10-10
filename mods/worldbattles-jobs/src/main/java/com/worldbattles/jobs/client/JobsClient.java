package com.worldbattles.jobs.client;

import com.worldbattles.jobs.JobSnapshot;
import net.minecraft.client.Minecraft;

/** Loaded only on the physical client by the packet handler. */
public final class JobsClient {
    private JobsClient() {}

    public static void accept(JobSnapshot snapshot) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.screen instanceof JobsScreen screen) {
            screen.update(snapshot); // No screen replacement: cursor and current tab stay put.
        } else {
            minecraft.setScreen(new JobsScreen(snapshot));
        }
    }
}
