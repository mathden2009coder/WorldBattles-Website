package com.worldbattles.weapons;

import com.mojang.logging.LogUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.slf4j.Logger;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;

@Mod("worldbattlesweapons")
public final class WorldBattlesWeaponDistributor {
    private static final Logger LOG = LogUtils.getLogger();
    private static final String ARENAS = "com.worldbattles.arenas.WorldBattlesArenas";
    private static final String WORLDBATTLE = "com.worldbattles.arenas.WorldBattlesMode";
    private static final String SANDWARS = "com.worldbattles.arenas.SandWarsMode";

    // Ammo counts are the magazine values in TACZ's gun data; the extra round is loaded in the barrel.
    private static final List<GunSpec> GUNS = List.of(
        new GunSpec("deagle", "50ae", 7),
        new GunSpec("m16a1", "556x45", 20),
        new GunSpec("qbz_95", "58x42", 30),
        new GunSpec("m95", "50bmg", 5),
        new GunSpec("m107", "50bmg", 10),
        new GunSpec("spas_12", "12g", 5),
        new GunSpec("m870", "12g", 5),
        new GunSpec("ak47", "762x39", 30),
        new GunSpec("hk_mp5a5", "9mm", 30),
        new GunSpec("taurus943", "22wmr", 8)
    );

    private static final Map<Object, Snapshot> SNAPSHOTS = new IdentityHashMap<>();
    private static final Map<UUID, String> LAST_GUN = new HashMap<>();
    private static final Map<UUID, PendingGrant> PENDING = new HashMap<>();
    private static final Random RANDOM = new Random();
    private static long ticks;

    public WorldBattlesWeaponDistributor() {
        LOG.info("WorldBattles Weapon Distributor test mod loaded; no production server has been changed.");
    }

    private enum Mode { ZOMBIES, WORLDBATTLE, SANDWARS }
    private record GunSpec(String id, String ammo, int magazine) {}
    private record PendingGrant(Mode mode, long readyAt) {}
    private static final class Snapshot {
        String phase = "";
        int count = Integer.MIN_VALUE;
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onDeath(LivingDeathEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        Mode mode = activeMode(player.getUUID());
        if (mode == null) return;

        clearInventory(player);
        // SandWars marks a player eliminated until the next round; don't grant a mid-round weapon.
        if (mode != Mode.SANDWARS) PENDING.put(player.getUUID(), new PendingGrant(mode, ticks + 3));
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onDrops(LivingDropsEvent event) {
        if (event.getEntity() instanceof ServerPlayer player && activeMode(player.getUUID()) != null) {
            event.getDrops().clear();
        }
    }

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        ticks++;
        MinecraftServer server = event.getServer();

        scanMode(server, ARENAS, "ARENAS", Mode.ZOMBIES, "wave", "players", "dead");
        scanMode(server, WORLDBATTLE, "MATCHES", Mode.WORLDBATTLE, "round", "teams", null);
        scanMode(server, SANDWARS, "MATCHES", Mode.SANDWARS, "round", "teams", "eliminated");

        Iterator<Map.Entry<UUID, PendingGrant>> iterator = PENDING.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<UUID, PendingGrant> entry = iterator.next();
            PendingGrant grant = entry.getValue();
            if (ticks < grant.readyAt()) continue;
            ServerPlayer player = server.getPlayerList().getPlayer(entry.getKey());
            if (player == null) {
                iterator.remove();
                continue;
            }
            // Let the arena's own death/teleport code finish first, then grant only to a living participant.
            if (player.isAlive() && activeMode(player.getUUID()) != null) {
                clearInventory(player);
                giveLoadout(player, grant.mode());
                iterator.remove();
            } else if (ticks > grant.readyAt() + 200) {
                iterator.remove();
            }
        }
    }

    private static void scanMode(MinecraftServer server, String owner, String arrayName, Mode mode,
                                 String countField, String participantsField, String excludedField) {
        Object array = staticField(owner, arrayName);
        if (array == null || !array.getClass().isArray()) return;
        Set<?> excluded = Set.of();
        for (int i = 0; i < Array.getLength(array); i++) {
            Object match = Array.get(array, i);
            if (match == null) continue;
            String phase = String.valueOf(field(match, "phase"));
            int count = number(field(match, countField), 0);
            Snapshot previous = SNAPSHOTS.computeIfAbsent(match, ignored -> new Snapshot());
            boolean enteredActive = "ACTIVE".equals(phase) && !"ACTIVE".equals(previous.phase);
            boolean advanced = "ACTIVE".equals(phase) && "ACTIVE".equals(previous.phase) && count != previous.count;
            if (enteredActive || advanced) {
                Collection<UUID> participants = participantIds(field(match, participantsField));
                Object excludedValue = excludedField == null ? null : field(match, excludedField);
                excluded = excludedValue instanceof Set<?> set ? set : Set.of();
                for (UUID id : participants) {
                    if (excluded.contains(id)) continue;
                    ServerPlayer player = server.getPlayerList().getPlayer(id);
                    if (player != null && player.isAlive()) giveLoadout(player, mode);
                }
            }
            previous.phase = phase;
            previous.count = count;
        }
    }

    private static Mode activeMode(UUID uuid) {
        if (isActiveMember(ARENAS, "ARENAS", uuid, "players")) return Mode.ZOMBIES;
        if (isActiveMember(WORLDBATTLE, "MATCHES", uuid, "teams")) return Mode.WORLDBATTLE;
        if (isActiveMember(SANDWARS, "MATCHES", uuid, "teams")) return Mode.SANDWARS;
        return null;
    }

    private static boolean isActiveMember(String owner, String arrayName, UUID uuid, String participantsField) {
        Object array = staticField(owner, arrayName);
        if (array == null || !array.getClass().isArray()) return false;
        for (int i = 0; i < Array.getLength(array); i++) {
            Object match = Array.get(array, i);
            if (match == null || !"ACTIVE".equals(String.valueOf(field(match, "phase")))) continue;
            Object participants = field(match, participantsField);
            if (participants instanceof Map<?, ?> map && map.containsKey(uuid)) return true;
            if (participants instanceof Collection<?> collection && collection.contains(uuid)) return true;
        }
        return false;
    }

    private static Collection<UUID> participantIds(Object value) {
        List<UUID> ids = new ArrayList<>();
        if (value instanceof Map<?, ?> map) {
            for (Object key : map.keySet()) if (key instanceof UUID id) ids.add(id);
        } else if (value instanceof Collection<?> collection) {
            for (Object item : collection) if (item instanceof UUID id) ids.add(id);
        }
        return ids;
    }

    private static void giveLoadout(ServerPlayer player, Mode mode) {
        if (player.level().isClientSide) return;
        GunSpec spec = chooseGun(player.getUUID());
        ItemStack gun = createGun(spec);
        if (gun.isEmpty()) {
            LOG.error("TACZ did not create gun tacz:{}; verify that the gun pack is loaded.", spec.id());
            return;
        }

        clearInventory(player);
        player.getInventory().setItem(0, gun);
        ItemStack box = createAmmoBox(spec, mode);
        if (box.isEmpty()) return;
        player.getInventory().setItem(1, box);
        player.getInventory().setSelected(0);
        LAST_GUN.put(player.getUUID(), spec.id());
        LOG.info("Assigned TACZ gun {} with ammo {} to {} in {}", spec.id(), spec.ammo(), player.getGameProfile().getName(), mode);
    }

    private static ItemStack createGun(GunSpec spec) {
        try {
            Class<?> builderClass = Class.forName("com.tacz.guns.api.item.builder.GunItemBuilder");
            Object builder = builderClass.getMethod("create").invoke(null);
            builder = invoke(builderClass, builder, "setId", new Class<?>[]{ResourceLocation.class}, new ResourceLocation("tacz", spec.id()));
            builder = invoke(builderClass, builder, "setCount", new Class<?>[]{int.class}, 1);
            builder = invoke(builderClass, builder, "setAmmoCount", new Class<?>[]{int.class}, spec.magazine());
            builder = invoke(builderClass, builder, "setAmmoInBarrel", new Class<?>[]{boolean.class}, true);
            return (ItemStack) builderClass.getMethod("build").invoke(builder);
        } catch (ReflectiveOperationException | LinkageError ex) {
            LOG.error("Unable to create TACZ gun tacz:{}", spec.id(), ex);
            return ItemStack.EMPTY;
        }
    }

    private static ItemStack createAmmoBox(GunSpec spec, Mode mode) {
        try {
            Class<?> modItems = Class.forName("com.tacz.guns.init.ModItems");
            Object registryObject = modItems.getField("AMMO_BOX").get(null);
            Object item = registryObject.getClass().getMethod("get").invoke(registryObject);
            ItemStack box = new ItemStack((net.minecraft.world.level.ItemLike) item, 1);
            Class<?> accessor = Class.forName("com.tacz.guns.api.item.nbt.AmmoBoxItemDataAccessor");
            if (mode == Mode.ZOMBIES) {
                // TACZ setCreative(true) marks AllTypeCreative=true, the existing universal infinite-ammo box.
                accessor.getMethod("setCreative", ItemStack.class, boolean.class).invoke(item, box, true);
            } else {
                accessor.getMethod("setAmmoLevel", ItemStack.class, int.class).invoke(item, box, 0);
                accessor.getMethod("setAmmoId", ItemStack.class, ResourceLocation.class).invoke(item, box, new ResourceLocation("tacz", spec.ammo()));
                accessor.getMethod("setAmmoCount", ItemStack.class, int.class).invoke(item, box, 64);
            }
            return box;
        } catch (ReflectiveOperationException | LinkageError ex) {
            LOG.error("Unable to create TACZ ammo box for gun tacz:{}", spec.id(), ex);
            return ItemStack.EMPTY;
        }
    }

    private static Object invoke(Class<?> type, Object target, String name, Class<?>[] parameterTypes, Object argument)
        throws ReflectiveOperationException {
        Method method = type.getMethod(name, parameterTypes);
        return method.invoke(target, argument);
    }

    private static GunSpec chooseGun(UUID player) {
        String last = LAST_GUN.get(player);
        List<GunSpec> options = GUNS.stream().filter(gun -> !gun.id().equals(last)).toList();
        if (options.isEmpty()) options = GUNS;
        return options.get(RANDOM.nextInt(options.size()));
    }

    private static void clearInventory(ServerPlayer player) {
        player.getInventory().clearContent();
        player.containerMenu.broadcastChanges();
    }

    private static Object staticField(String className, String name) {
        try {
            Class<?> type = Class.forName(className);
            Field field = type.getDeclaredField(name);
            field.setAccessible(true);
            return field.get(null);
        } catch (ReflectiveOperationException | LinkageError ex) {
            return null;
        }
    }

    private static Object field(Object target, String name) {
        if (target == null) return null;
        Class<?> type = target.getClass();
        while (type != null) {
            try {
                Field field = type.getDeclaredField(name);
                field.setAccessible(true);
                return field.get(target);
            } catch (NoSuchFieldException ignored) {
                type = type.getSuperclass();
            } catch (ReflectiveOperationException | LinkageError ex) {
                return null;
            }
        }
        return null;
    }

    private static int number(Object value, int fallback) {
        return value instanceof Number n ? n.intValue() : fallback;
    }
}
