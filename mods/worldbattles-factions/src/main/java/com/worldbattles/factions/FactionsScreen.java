package com.worldbattles.factions;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import java.util.*;

/** Compact custom dashboard, not a chest. Uses real Minecraft item icons as emblems. */
public final class FactionsScreen extends Screen {
 public enum Page {HOME, MEMBERS, BANK, LEADERBOARD, SETTINGS}
 private static final int BG=0xFF080D17,PANEL=0xFF101A2A,CARD=0xFF19283B;
 private static final int ORANGE=0xFFFFA02C,WHITE=0xFFF1F5FA,MUTED=0xFFA0B2C6,GREEN=0xFF7FE0A8,RED=0xFFFA8D85;
 private static final String[] ICONS=FactionData.ICONS;
 private CompoundTag data;
 private Page page;
 private int x,y,w,h,contentX,contentW;
 private EditBox primary,secondary;
 private int selection=0;
 public FactionsScreen(CompoundTag data,Page page){super(Component.literal("WorldBattles Factions"));this.data=data;this.page=page;
  for(int i=0;i<ICONS.length;i++)if(ICONS[i].equals(data.getString("Icon")))selection=i;
 }
 public Page page(){return page;}
 private void dispatch(String op,String value,String extra){FactionsMod.send(op,value,extra);}
 private void action(String op){dispatch(op,primary==null?"":primary.getValue(),"");}
 private void button(String title,int atX,int atY,int width,int height,Runnable callback){
  if(width<10)return;
  addRenderableWidget(Button.builder(Component.literal(title),b->callback.run()).bounds(atX,atY,width,height).build());
 }
 private EditBox edit(String hint,int px,int py,int width,int length){
  EditBox field=new EditBox(font,px,py,width,20,Component.literal(hint));
  field.setMaxLength(length);field.setHint(Component.literal(hint));addRenderableWidget(field);return field;
 }
 private boolean joined(){return data.getBoolean("InFaction");}
 private boolean owner(){return "CHEF".equals(data.getString("Role"));}
 private boolean officer(){return owner()||"OFFICIER".equals(data.getString("Role"));}
 private String money(long value){return String.format(Locale.FRANCE,"%,d $",value);}
 private void text(GuiGraphics g,String value,int px,int py,int color){g.drawString(font,value,px,py,color,false);}
 private void fitText(GuiGraphics g,String value,int px,int py,int max,int color){
  if(font.width(value)>max)value=font.plainSubstrByWidth(value,Math.max(10,max-font.width("...")))+"...";
  text(g,value,px,py,color);
 }
 private void line(GuiGraphics g,int py){g.fill(contentX,py,contentX+contentW,py+1,0xFF32435B);}
 private void row(GuiGraphics g,int py,String left,String right){
  fitText(g,left,contentX+12,py,contentW/2-18,WHITE);
  int rx=contentX+contentW-12-font.width(right);text(g,right,Math.max(contentX+100,rx),py,ORANGE);
 }
 private static ItemStack stack(String name){
  return new ItemStack(BuiltInRegistries.ITEM.get(new ResourceLocation("minecraft",name)));
 }
 @Override protected void init(){
  w=Math.min(490,width-28);h=Math.min(324,height-24);
  x=(width-w)/2;y=(height-h)/2;contentX=x+114;contentW=w-128;
  primary=null;secondary=null;
  int bW=94;int tabY=y+70;
  String[] labels={"ACCUEIL","MEMBRES","BANQUE","CLASSEMENT","GESTION"};
  Page[] pages=Page.values();
  for(int i=0;i<pages.length;i++){final Page chosen=pages[i];
   button(labels[i],x+10,tabY+i*36,bW,27,()->{page=chosen;clearWidgets();init();});
  }
  button("X",x+w-29,y+12,18,18,this::onClose);
  if(!joined()){showCreate();return;}
  switch(page){
   case HOME->showHome();
   case MEMBERS->showMembers();
   case BANK->showBank();
   case LEADERBOARD->showLeaderboard();
   case SETTINGS->showSettings();
  }
 }
 private void showCreate(){
  if(!data.getString("Invite").isEmpty()){
   button("ACCEPTER INVITATION",contentX+11,y+118,Math.min(156,contentW-23),22,()->dispatch("accept","",""));
   button("REFUSER",contentX+172,y+118,Math.min(90,contentW-190),22,()->dispatch("decline","",""));
  }
  primary=edit("Nom de faction (3-16 caractères)",contentX+12,y+177,contentW-26,16);
  for(int i=0;i<ICONS.length;i++){
   final int index=i;
   int px=contentX+12+(i%6)*Math.max(32,(contentW-28)/6);
   int py=y+210+(i/6)*29;
   button(selection==i?"*":"",px,py,Math.min(42,(contentW-28)/6-2),25,()->{selection=index;});
  }
  button("CRÉER POUR 100 000 $",contentX+13,y+h-37,Math.min(188,contentW-24),22,
   ()->dispatch("create",primary.getValue(),ICONS[selection]));
 }
 private void showHome(){
  button("ACTUALISER",contentX+11,y+h-38,96,22,()->dispatch("refresh","",""));
  if(data.getBoolean("Home"))button("POINT DE RALLIEMENT",contentX+117,y+h-38,Math.min(172,contentW-125),22,()->dispatch("home","",""));
 }
 private void showMembers(){
  if(officer()){
   primary=edit("Pseudo du joueur à inviter",contentX+10,y+81,Math.min(175,contentW-112),16);
   button("INVITER",contentX+contentW-97,y+81,85,20,()->action("invite"));
  }
  if(owner()){
   secondary=edit("Pseudo du membre",contentX+10,y+h-69,Math.max(100,contentW-16),16);
   int bw=(contentW-18)/4;
   button("PROMOUVOIR",contentX+10,y+h-43,bw,20,()->dispatch("promote",secondary.getValue(),""));
   button("RÉTROGRADER",contentX+12+bw,y+h-43,bw,20,()->dispatch("demote",secondary.getValue(),""));
   button("EXCLURE",contentX+14+bw*2,y+h-43,bw,20,()->dispatch("kick",secondary.getValue(),""));
   button("CHEF",contentX+16+bw*3,y+h-43,bw-7,20,()->dispatch("transfer",secondary.getValue(),""));
  }
 }
 private void showBank(){
  primary=edit("Montant en $ (ex. 50000)",contentX+11,y+135,Math.min(225,contentW-24),20);
  button("DÉPOSER",contentX+11,y+163,Math.min(125,(contentW-32)/2),22,()->action("deposit"));
  if(owner())button("RETIRER",contentX+146,y+163,Math.min(125,(contentW-158)),22,()->action("withdraw"));
 }
 private void showLeaderboard(){button("ACTUALISER",contentX+11,y+h-38,104,22,()->dispatch("refresh","",""));}
 private void showSettings(){
  if(officer()){
   primary=edit("Description publique (100 caractères max)",contentX+11,y+117,contentW-22,100);
   button("ENREGISTRER",contentX+11,y+144,105,20,()->action("setdesc"));
   button("FIXER LE RALLIEMENT",contentX+122,y+144,Math.min(184,contentW-137),20,()->dispatch("sethome","",""));
  }
  if(owner()){
   button("DÉGÂTS ALLIÉS : "+(data.getBoolean("FriendlyFire")?"ON":"OFF"),contentX+11,y+180,Math.min(196,contentW-22),21,
    ()->dispatch("friendlyfire","",""));
   button("EMBLÈME SUIVANT",contentX+11,y+207,Math.min(135,contentW-23),21,
    ()->{selection=(selection+1)%ICONS.length;dispatch("emblem",ICONS[selection],"");});
   secondary=edit("Nom exact pour dissoudre",contentX+11,y+237,Math.max(110,contentW-24),16);
   button("DISSOUDRE",contentX+11,y+h-39,Math.min(112,contentW-22),21,()->dispatch("disband",secondary.getValue(),""));
  }else{
   button("QUITTER LA FACTION",contentX+11,y+172,170,22,()->dispatch("leave","",""));
  }
 }
 @Override public void render(GuiGraphics g,int mx,int my,float partial){
  renderBackground(g);
  g.fill(x-5,y-5,x+w+5,y+h+5,0x99000000);
  g.fillGradient(x,y,x+w,y+h,BG,PANEL);
  g.fill(x,y,x+w,y+3,ORANGE);
  g.fillGradient(x+2,y+4,x+w-2,y+57,0xFF20334D,BG);
  text(g,"WORLDBATTLES",x+14,y+12,WHITE);text(g,"FACTIONS",x+120,y+12,ORANGE);
  text(g,"FREELANDS  /  ÉCONOMIE  /  CLASSEMENT",x+14,y+31,MUTED);
  g.fill(x+108,y+65,x+109,y+h-14,0xFF31445B);
  g.fill(contentX,y+65,contentX+contentW,y+h-55,0x361C314A);
  if(joined())renderMember(g);
  else renderCreate(g);
  String note=data.getString("Notice");
  if(!note.isEmpty())fitText(g,note,x+16,y+h-16,w-35,GREEN);
  super.render(g,mx,my,partial);
 }
 private void renderCreate(GuiGraphics g){
  text(g,"FONDE TA FACTION",contentX+11,y+74,ORANGE);
  text(g,"Coût de création : 100 000 $",contentX+11,y+92,WHITE);
  text(g,"Ton solde : "+money(data.getLong("Balance")),contentX+11,y+105,GREEN);
  if(!data.getString("Invite").isEmpty()){
   fitText(g,"Invitation : "+data.getString("Invite"),contentX+11,y+144,contentW-22,ORANGE);
  }
  text(g,"NOM DE FACTION",contentX+12,y+164,MUTED);
  text(g,"CHOISIS UN EMBLÈME",contentX+12,y+197,MUTED);
  for(int i=0;i<ICONS.length;i++){
   int px=contentX+12+(i%6)*Math.max(32,(contentW-28)/6);
   int py=y+210+(i/6)*29;
   if(i==selection)g.fill(px-2,py-2,px+26,py+24,0x55FFA12C);
   g.renderItem(stack(ICONS[i]),px+8,py+5);
  }
 }
 private void renderMember(GuiGraphics g){
  switch(page){
   case HOME->{
    text(g,""+data.getString("Name").toUpperCase(Locale.ROOT),contentX+11,y+78,ORANGE);
    fitText(g,data.getString("Desc").isBlank()?"Aucune description pour le moment":data.getString("Desc"),contentX+11,y+101,contentW-25,MUTED);
    g.renderItem(stack(data.getString("Icon")),contentX+contentW-32,y+75);
    row(g,y+132,"RANG",data.getString("Rank"));line(g,y+149);
    row(g,y+162,"TRÉSORERIE",money(data.getLong("Treasury")));line(g,y+179);
    row(g,y+190,"TON GRADE",data.getString("Role"));line(g,y+207);
    row(g,y+218,"MEMBRES",String.valueOf(data.getList("Members",Tag.TAG_COMPOUND).size()));
    text(g,"Le prestige dépend des dépôts en banque.",contentX+11,y+h-64,MUTED);
   }
   case MEMBERS->{
    text(g,"MEMBRES ET GRADES",contentX+11,y+70,ORANGE);
    ListTag members=data.getList("Members",Tag.TAG_COMPOUND);
    int top=officer()?y+114:y+99;
    int limit=owner()?Math.min(members.size(),8):Math.min(members.size(),13);
    for(int i=0;i<limit;i++){
     CompoundTag m=members.getCompound(i);int cy=top+i*14;
     fitText(g,(m.getBoolean("Online")?"§a● ":"§7○ ")+m.getString("Name"),contentX+12,cy,contentW-115,WHITE);
     fitText(g,m.getString("Role"),contentX+contentW-81,cy,76,ORANGE);
    }
    if(members.size()>limit)text(g,"+"+(members.size()-limit)+" autres membres",contentX+13,top+limit*14,MUTED);
   }
   case BANK->{
    text(g,"BANQUE DE FACTION",contentX+11,y+76,ORANGE);
    text(g,"Trésorerie",contentX+11,y+100,MUTED);
    text(g,money(data.getLong("Treasury")),contentX+11,y+113,GREEN);
    text(g,"Ton compte personnel : "+money(data.getLong("Balance")),contentX+11,y+203,WHITE);
    text(g,"Les dépôts font progresser le rang.",contentX+11,y+225,MUTED);
    text(g,"Retraits : chef uniquement.",contentX+11,y+240,MUTED);
   }
   case LEADERBOARD->{
    text(g,"TOP FACTIONS · TRÉSORERIE",contentX+11,y+75,ORANGE);
    ListTag list=data.getList("Leaderboard",Tag.TAG_COMPOUND);
    for(int i=0;i<Math.min(12,list.size());i++){
     CompoundTag item=list.getCompound(i);int py=y+100+i*15;
     fitText(g,(i+1)+". "+item.getString("Name"),contentX+11,py,contentW-143,WHITE);
     fitText(g,item.getString("Rank"),contentX+contentW-137,py,58,MUTED);
     fitText(g,money(item.getLong("Treasury")),contentX+contentW-83,py,83,GREEN);
    }
   }
   case SETTINGS->{
    text(g,"GESTION DE FACTION",contentX+11,y+76,ORANGE);
    text(g,"Ton rôle : "+data.getString("Role"),contentX+11,y+93,MUTED);
    if(!officer())text(g,"Contacte un officier pour modifier la faction.",contentX+11,y+119,MUTED);
    if(owner())g.renderItem(stack(ICONS[selection]),contentX+159,y+209);
    ListTag history=data.getList("History",Tag.TAG_COMPOUND);
    if(!owner()){
     text(g,"DERNIÈRES ACTIVITÉS",contentX+11,y+207,ORANGE);
     for(int i=0;i<Math.min(4,history.size());i++)fitText(g,history.getCompound(i).getString("Text"),contentX+11,y+225+i*14,contentW-20,MUTED);
    }
   }
  }
 }
 @Override public boolean isPauseScreen(){return false;}
}
