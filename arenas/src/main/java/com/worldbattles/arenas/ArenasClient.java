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
   @Override protected void init(){
    int cx=width/2;
    for(int i=0;i<3;i++){
     final int index=i;
     addRenderableWidget(Button.builder(Component.literal("REJOINDRE "+(i+1)),b->{WorldBattlesArenas.select(index);onClose();})
       .bounds(cx-115,height/2-23+i*36,230,22).build());
    }
    addRenderableWidget(Button.builder(Component.literal("QUITTER"),b->{WorldBattlesArenas.select(-1);onClose();})
      .bounds(cx-115,height/2+91,110,20).build());
    addRenderableWidget(Button.builder(Component.literal("FERMER"),b->onClose())
      .bounds(cx+5,height/2+91,110,20).build());
   }
   @Override public void render(GuiGraphics g,int mx,int my,float delta){
    renderBackground(g);
    int l=width/2-145,t=height/2-116;
    g.fill(l,t,l+290,t+250,0xF1121929);
    g.fill(l,t,l+290,t+4,0xFFFFA132);
    g.drawCenteredString(font,"WORLDBATTLES",width/2,t+12,0xFFFFAF48);
    g.drawCenteredString(font,"ZOMBIES  |  3 ARENES",width/2,t+31,0xFFFFFFFF);
    for(int i=0;i<3;i++){
      g.drawCenteredString(font,"Arène "+(i+1)+" : "+statuses[i],width/2,t+62+i*36,0xFFBCD3F0);
    }
    super.render(g,mx,my,delta);
   }
   @Override public boolean isPauseScreen(){return false;}
  });
 }
}
