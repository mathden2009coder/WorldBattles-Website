package com.worldbattles.jobs;

import com.worldbattles.jobs.client.JobsClient;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

/** Only read-only score snapshots travel over the wire. */
public final class JobsNetwork {
    private static final String PROTOCOL = "1";
    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
        new ResourceLocation(WorldBattlesJobs.MODID, "sync"), () -> PROTOCOL,
        PROTOCOL::equals, PROTOCOL::equals);
    private static final Map<UUID, Long> LAST_REFRESH = new ConcurrentHashMap<>();

    private JobsNetwork() {}

    public static void register() {
        CHANNEL.registerMessage(0, JobSnapshot.class, JobSnapshot::encode, JobSnapshot::decode, JobsNetwork::onSnapshot);
        CHANNEL.registerMessage(1, RefreshRequest.class, (message, buf) -> {},
            buf -> new RefreshRequest(), JobsNetwork::onRefresh);
    }

    public static void sendSnapshot(ServerPlayer player) {
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), JobSnapshot.capture(player));
    }

    private static void onSnapshot(JobSnapshot snapshot, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
            () -> () -> JobsClient.accept(snapshot)));
        context.setPacketHandled(true);
    }

    public record RefreshRequest() {}

    public static void requestRefresh() {
        CHANNEL.sendToServer(new RefreshRequest());
    }

    private static void onRefresh(RefreshRequest request, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player == null) return;
            long now = player.serverLevel().getGameTime();
            long last = LAST_REFRESH.getOrDefault(player.getUUID(), -100L);
            if (now >= last && now - last < 20L) return;
            LAST_REFRESH.put(player.getUUID(), now);
            sendSnapshot(player);
        });
        context.setPacketHandled(true);
    }
}
