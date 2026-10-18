package com.worldbattles.spawnexit;

import java.lang.reflect.Method;
import java.lang.reflect.Field;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import java.util.UUID;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.CommandEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.slf4j.Logger;
import com.mojang.logging.LogUtils;

/**
 * Bridge to the tested WorldBattles Arenas 0.4.1:
 * use the Arena's OWN leave method, never manipulate internal participant maps.
 * This happens before FTB Essentials /spawn executes, keeping its configured destination.
 */
@Mod(SpawnExit.MODID)
public final class SpawnExit {
    public static final String MODID="worldbattlesspawnexit";
    private static final Logger LOGGER=LogUtils.getLogger();
    private static final String PREFIX="com.worldbattles.arenas.";
    private static final String[] MODES={"SandWarsMode","WorldBattlesMode","WorldBattlesArenas"};
    private static final Method[] FINDS=new Method[MODES.length];
    private static final Method[] LEAVES=new Method[MODES.length];
    private static final Field[] PHASES=new Field[MODES.length];
    private static final Field[] INDICES=new Field[MODES.length];
    private static final String[] LABELS={"SandWars","WorldsBattle","Zombies"};
    private static boolean initAttempted=false;

    public SpawnExit(){MinecraftForge.EVENT_BUS.register(this);}

    private static void init(){
        if(initAttempted)return;
        initAttempted=true;
        for(int i=0;i<MODES.length;i++){
            try{
                Class<?> owner=Class.forName(PREFIX+MODES[i]);
                Class<?> session=Class.forName(PREFIX+MODES[i]+(i==2?"$Arena":"$Match"));
                FINDS[i]=owner.getDeclaredMethod(i==2?"findArena":(i==1?"getMatch":"findMatch"),UUID.class);
                LEAVES[i]=session.getDeclaredMethod("leave",ServerPlayer.class);
                FINDS[i].setAccessible(true);
                LEAVES[i].setAccessible(true);
                PHASES[i]=session.getDeclaredField("phase");
                PHASES[i].setAccessible(true);
                INDICES[i]=session.getDeclaredField("index");
                INDICES[i].setAccessible(true);
            }catch(ReflectiveOperationException | RuntimeException ex){
                LOGGER.error("Cannot link Arenas 0.4.1 mode {}. Leave-on-spawn unavailable for that mode.",MODES[i],ex);
            }
        }
    }

    @SubscribeEvent(priority=EventPriority.LOWEST)
    public void beforeSpawn(CommandEvent e){
        if(e.isCanceled())return;
        String input=e.getParseResults().getReader().getString().trim();
        if(input.startsWith("/"))input=input.substring(1);
        if(!input.equalsIgnoreCase("spawn")&&!input.toLowerCase(java.util.Locale.ROOT).startsWith("spawn "))return;
        if(!(e.getParseResults().getContext().getSource().getEntity() instanceof ServerPlayer player))return;
        init();
        for(int i=0;i<MODES.length;i++){
            if(FINDS[i]==null||LEAVES[i]==null)continue;
            try{
                Object instance=FINDS[i].invoke(null,player.getUUID());
                if(instance!=null){
                    // Snapshot the state BEFORE leave() modifies or resets the match.
                    String state=String.valueOf(PHASES[i].get(instance));
                    int arenaNumber=((Number)INDICES[i].get(instance)).intValue()+1;
                    boolean waiting=state.equals("WAITING")||state.equals("COUNTDOWN");
                    LEAVES[i].invoke(instance,player);
                    String destination=waiting?"salle d'attente":"partie";
                    player.sendSystemMessage(Component.literal("✦ ").withStyle(ChatFormatting.GOLD)
                        .append(Component.literal("Sortie de la "+destination).withStyle(ChatFormatting.YELLOW))
                        .append(Component.literal(" • "+LABELS[i]+" #"+arenaNumber).withStyle(ChatFormatting.GRAY)));
                    LOGGER.info("Player {} left {} via /spawn",player.getGameProfile().getName(),MODES[i]);
                }
            }catch(ReflectiveOperationException | RuntimeException ex){
                LOGGER.error("Could not remove {} from {} on /spawn",player.getGameProfile().getName(),MODES[i],ex);
            }
        }
    }
}
