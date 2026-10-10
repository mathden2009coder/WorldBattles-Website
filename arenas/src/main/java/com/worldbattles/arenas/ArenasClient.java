package com.worldbattles.arenas;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
public final class ArenasClient {
 private ArenasClient(){}
 public static void show(String[] statuses){
  Minecraft.getInstance().setScreen(new Screen(Component.literal("WorldBattles Zombies")) {
   private int top, left, panelHeight;
   @Override protected void init(){
    int cx=width/2;
    panelHeight=282;
    top=(height-panelHeight)/2;
    left=cx-145;
    for(int i=0;i<3;i++){
     final int index=i;
     int buttonY=top+79+i*53;
     addRenderableWidget(Button.builder(Component.literal("REJOINDRE "+(i+1)),
      b->{WorldBattlesArenas.select(index);onClose();})
      .bounds(cx-115,buttonY,230,22).build());
    }
    addRenderableWidget(Button.builder(Component.literal("QUITTER"),
      b->{WorldBattlesArenas.select(-1);onClose();})
      .bounds(cx-115,top+244,110,20).build());
    addRenderableWidget(Button.builder(Component.literal("FERMER"),b->onClose())
      .bounds(cx+5,top+244,110,20).build());
   }
   @Override public void render(GuiGraphics g,int mx,int my,float delta){
    renderBackground(g);
    int cx=width/2;
    g.fill(left,top,left+290,top+panelHeight,0xF1121929);
    g.fill(left,top,left+290,top+4,0xFFFFA132);
    g.drawCenteredString(font,"WORLDBATTLES",cx,top+14,0xFFFFAF48);
    g.drawCenteredString(font,"ZOMBIES  |  3 ARENES",cx,top+34,0xFFFFFFFF);
    for(int i=0;i<3;i++){
     String status=i<statuses.length?statuses[i]:"INDISPONIBLE";
     g.drawCenteredString(font,"Arène "+(i+1)+" : "+status,cx,top+65+i*53,0xFFBCD3F0);
    }
    super.render(g,mx,my,delta);
   }
   @Override public boolean isPauseScreen(){return false;}
  });
 }
}
