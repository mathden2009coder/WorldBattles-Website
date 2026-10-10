package com.worldbattles.arenas;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
public final class ArenasClient {
 private ArenasClient(){}
 public static void show(String status){ Minecraft.getInstance().setScreen(new Screen(Component.literal("WorldBattles Zombies")) {
  @Override protected void init(){
   int cx=width/2;
   addRenderableWidget(Button.builder(Component.literal("REJOINDRE"),b->{WorldBattlesArenas.select(true);onClose();}).bounds(cx-100,height/2+18,200,22).build());
   addRenderableWidget(Button.builder(Component.literal("QUITTER LA FILE"),b->{WorldBattlesArenas.select(false);onClose();}).bounds(cx-100,height/2+45,200,22).build());
   addRenderableWidget(Button.builder(Component.literal("FERMER"),b->onClose()).bounds(cx-100,height/2+72,200,20).build());
  }
  @Override public void render(GuiGraphics g,int x,int y,float delta){
   renderBackground(g);int l=width/2-134,t=height/2-100;
   g.fill(l,t,l+268,t+204,0xF1121929);
   g.fill(l,t,l+268,t+4,0xFFFFA132);
   g.drawCenteredString(font,"WORLDBATTLES",width/2,t+15,0xFFFFAF48);
   g.drawCenteredString(font,"ZOMBIES | ARENE 1 / 3",width/2,t+37,0xFFFFFFFF);
   g.drawCenteredString(font,status,width/2,t+64,0xFFBCD3F0);
   super.render(g,x,y,delta);
  }
  @Override public boolean isPauseScreen(){return false;}
 });}
}
