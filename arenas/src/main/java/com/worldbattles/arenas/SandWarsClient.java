package com.worldbattles.arenas;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
public final class SandWarsClient {
 private SandWarsClient(){}
 public static void show(String[] statuses){
  Minecraft.getInstance().setScreen(new Screen(Component.literal("Sand Wars")) {
   int top,left;
   @Override protected void init(){
    top=(height-282)/2;left=width/2-145;
    for(int i=0;i<3;i++){final int index=i;
     addRenderableWidget(Button.builder(Component.literal("REJOINDRE "+(i+1)),
       b->{SandWarsMode.choose(index);onClose();}).bounds(width/2-115,top+79+i*53,230,22).build());
    }
    addRenderableWidget(Button.builder(Component.literal("QUITTER LA FILE"),
     b->{SandWarsMode.choose(-1);onClose();}).bounds(width/2-115,top+244,110,20).build());
    addRenderableWidget(Button.builder(Component.literal("FERMER"),b->onClose()).bounds(width/2+5,top+244,110,20).build());
   }
   @Override public void render(GuiGraphics g,int mx,int my,float delta){
    renderBackground(g);int cx=width/2;
    g.fill(left,top,left+290,top+282,0xF1121929);
    g.fill(left,top,left+290,top+4,0xFFFF9F2E);
    g.drawCenteredString(font,"WORLDBATTLES",cx,top+14,0xFFFFB24F);
    g.drawCenteredString(font,"SAND WARS | ORANGE VS SABLE",cx,top+34,0xFFFFFFFF);
    for(int i=0;i<3;i++)g.drawCenteredString(font,"Arène "+(i+1)+" : "+(i<statuses.length?statuses[i]:"INDISPONIBLE"),cx,top+65+i*53,0xFFFFD6AB);
    super.render(g,mx,my,delta);
   }
   @Override public boolean isPauseScreen(){return false;}
  });
 }
}
