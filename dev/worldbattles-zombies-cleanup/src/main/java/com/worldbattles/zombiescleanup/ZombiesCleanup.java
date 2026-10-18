package com.worldbattles.zombiescleanup;

import com.mojang.logging.LogUtils;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Set;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.slf4j.Logger;

/**
 * Cleans leftover zombies as their OWN arena enters Arenas 0.4.1 CLEANING.
 * Does not alter arena membership, lifecycle, or structure restoration.
 */
@Mod(ZombiesCleanup.MOD_ID)
public final class ZombiesCleanup {
    public static final String MOD_ID = "worldbattleszombiescleanup";
    private static final Logger LOG=LogUtils.getLogger();
    private static final int MAX_ARENAS=3;
    private static final int X0=-142, Y0=-62, Z0=-12;
    private static final int SX=65, SY=10, SZ=101;
    private static final int SPACING=300;
    private final Set<Object> alreadyCleaned=Collections.newSetFromMap(new IdentityHashMap<>());
    private Field arenasField, phaseField;
    private boolean reflectionAttempted;
    private boolean linked;
    public ZombiesCleanup(){MinecraftForge.EVENT_BUS.register(this);}

    private void link(){
        if(reflectionAttempted)return;
        reflectionAttempted=true;
        try{
            Class<?> owner=Class.forName("com.worldbattles.arenas.WorldBattlesArenas");
            Class<?> inner=Class.forName("com.worldbattles.arenas.WorldBattlesArenas$Arena");
            arenasField=owner.getDeclaredField("ARENAS");
            phaseField=inner.getDeclaredField("phase");
            arenasField.setAccessible(true);
            phaseField.setAccessible(true);
            linked=true;
            LOG.info("WorldBattles Zombies Cleanup linked to Arenas 0.4.1");
        }catch(ReflectiveOperationException | RuntimeException ex){
            LOG.error("WorldBattles Arenas 0.4.1 internals not found; cleanup disabled safely",ex);
        }
    }
    @SubscribeEvent
    public void onTick(TickEvent.ServerTickEvent event){
        if(event.phase!=TickEvent.Phase.END)return;
        link();
        if(!linked)return;
        MinecraftServer server=event.getServer();
        ServerLevel level=server.overworld();
        try{
            Object[] arenas=(Object[])arenasField.get(null);
            int count=Math.min(MAX_ARENAS,arenas.length);
            for(int i=0;i<count;i++){
                Object arena=arenas[i];
                if(arena==null)continue;
                Object phase=phaseField.get(arena);
                boolean cleaning=phase!=null && phase.toString().equals("CLEANING");
                if(!cleaning){alreadyCleaned.remove(arena);continue;}
                if(!alreadyCleaned.add(arena))continue;
                int offset=i*SPACING;
                AABB area=new AABB(X0+offset,Y0,Z0,X0+offset+SX,Y0+SY,Z0+SZ);
                int removed=0;
                for(Zombie zombie:level.getEntitiesOfClass(Zombie.class,area,z->z.isAlive())){
                    zombie.discard();
                    removed++;
                }
                LOG.info("Zombies arena {} cleanup: removed {} remaining zombies",i+1,removed);
            }
        }catch(ReflectiveOperationException | RuntimeException ex){
            LOG.error("Zombies cleanup could not read arena states; disabling until server restart",ex);
            linked=false;
        }
    }
}
