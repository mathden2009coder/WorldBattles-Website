package com.worldbattles.shop;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.LongArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayDeque;
import java.util.Queue;
import java.util.Set;
import java.util.function.Consumer;

@Mod(WorldBattlesShop.MOD_ID)
public final class WorldBattlesShop {
    public static final String MOD_ID = "worldbattlesshop";
    private static final Logger LOG = LoggerFactory.getLogger("WorldBattlesShop");
    private static final Set<ResourceLocation> ALLOWED_DIMENSIONS = Set.of(
        new ResourceLocation("secondworld", "overworld"),
        new ResourceLocation("secondworld", "the_nether"),
        new ResourceLocation("minage", "overworld"),
        new ResourceLocation("minage", "the_nether")
    );
    private static final Queue<Runnable> NEXT_TICK = new ArrayDeque<>();

    public WorldBattlesShop() {
        ShopRegistry.MENUS.register(FMLJavaModLoadingContext.get().getModEventBus());
        MinecraftForge.EVENT_BUS.register(this);
        LOG.info("WorldBattles Shop 0.2.0 loaded; custom GUI and dimension restrictions enabled.");
    }

    public static boolean allowed(ServerPlayer player) {
        return ALLOWED_DIMENSIONS.contains(player.level().dimension().location());
    }

    public static void message(ServerPlayer player, String text) {
        player.sendSystemMessage(Component.literal("[WorldBattles Shop] " + text));
    }

    public static void defer(Runnable action) { NEXT_TICK.add(action); }

    @SubscribeEvent
    public void serverTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        // Menu transitions occur after container click packets finish processing.
        for (int i = 0; i < 500 && !NEXT_TICK.isEmpty(); i++) NEXT_TICK.remove().run();
    }

    @SubscribeEvent
    public void playerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        ShopData data = ShopData.get(player.getServer());
        data.remember(player);
        if (!data.mail(player.getUUID()).isEmpty())
            message(player, "Tu as des colis a recuperer avec /wbshop dans SecondWorld ou Minage.");
    }

    @SubscribeEvent
    public void registerCommands(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> d = event.getDispatcher();
        d.register(Commands.literal("wbshop")
            .executes(ctx -> run(ctx, ShopGui::home))
            .then(Commands.literal("market").executes(ctx -> run(ctx, p -> ShopGui.market(p, 0))))
            .then(Commands.literal("sell").executes(ctx -> run(ctx, ShopGui::serverSell)))
            .then(Commands.literal("list").executes(ctx -> run(ctx, ShopGui::draft)))
            .then(Commands.literal("bank").executes(ctx -> run(ctx, ShopGui::bank)))
            .then(Commands.literal("balances").executes(ctx -> run(ctx, p -> ShopGui.balances(p, 0))))
            .then(Commands.literal("mail").executes(ctx -> run(ctx, p -> ShopGui.mailbox(p, 0)))));
        d.register(Commands.literal("shop").executes(ctx -> run(ctx, ShopGui::home)));
        d.register(Commands.literal("ah").executes(ctx -> run(ctx, p -> ShopGui.market(p, 0))));
        d.register(Commands.literal("bal").executes(ctx -> run(ctx, ShopGui::bank)));
        d.register(Commands.literal("baltop").executes(ctx -> run(ctx, p -> ShopGui.balances(p, 0))));

        d.register(Commands.literal("wbshopadmin")
            .requires(source -> source.hasPermission(2))
            .then(Commands.literal("set")
                .then(Commands.argument("player", EntityArgument.player())
                    .then(Commands.argument("amount", LongArgumentType.longArg(0, ShopData.MAX_BALANCE))
                        .executes(ctx -> admin(ctx, false)))))
            .then(Commands.literal("add")
                .then(Commands.argument("player", EntityArgument.player())
                    .then(Commands.argument("amount", LongArgumentType.longArg(0, ShopData.MAX_BALANCE))
                        .executes(ctx -> admin(ctx, true))))));
    }

    private static int run(CommandContext<CommandSourceStack> ctx, Consumer<ServerPlayer> action) throws CommandSyntaxException {
        ServerPlayer player = ctx.getSource().getPlayerOrException();
        if (!allowed(player)) {
            message(player, "Shop interdit dans l'Overworld, le Nether et l'End normaux.");
            return 0;
        }
        action.accept(player);
        return 1;
    }

    private static int admin(CommandContext<CommandSourceStack> ctx, boolean add) throws CommandSyntaxException {
        ServerPlayer target = EntityArgument.getPlayer(ctx, "player");
        long amount = LongArgumentType.getLong(ctx, "amount");
        ShopData data = ShopData.get(ctx.getSource().getServer());
        ShopData.Result result = add
            ? data.adminAdd(target.getUUID(), target.getGameProfile().getName(), amount)
            : data.adminSet(target.getUUID(), target.getGameProfile().getName(), amount);
        ctx.getSource().sendSuccess(() -> Component.literal(result.message()), true);
        LOG.info("Admin {} money: target={}, amount={}, success={}", add ? "add" : "set", target.getUUID(), amount, result.ok());
        return result.ok() ? 1 : 0;
    }
}
