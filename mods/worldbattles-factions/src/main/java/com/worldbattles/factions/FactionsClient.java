package com.worldbattles.factions;

import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;

/** Client-only UI gateway. No GUI class is loaded on a dedicated server. */
public final class FactionsClient {
 private FactionsClient(){}
 public static void receive(CompoundTag packet){
  Minecraft client=Minecraft.getInstance();
  if(client.level==null)return;
  FactionsScreen.Page page=client.screen instanceof FactionsScreen current?current.page():FactionsScreen.Page.HOME;
  client.setScreen(new FactionsScreen(packet,page));
 }
}
