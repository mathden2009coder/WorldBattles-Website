package com.worldbattles.factions;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.saveddata.SavedData;
import java.util.*;

/** Entire faction state is authoritative, persistent and modified only on the server thread. */
public final class FactionData extends SavedData {
 public static final long MAX_TREASURY=1_000_000_000_000L;
 public static final int MAX_MEMBERS=30;
 public static final String[] ICONS={"diamond","emerald","nether_star","gold_ingot","iron_sword","shield","ender_eye","blaze_powder","amethyst_shard","heart_of_the_sea","totem_of_undying","redstone"};
 public final Map<String,Faction> factions=new LinkedHashMap<>();
 public final Map<UUID,String> membership=new HashMap<>();
 public static final class Faction {
  public String name,description="",icon="diamond";
  public UUID founder;
  public final LinkedHashMap<UUID,String> members=new LinkedHashMap<>();
  public final Set<UUID> officers=new HashSet<>();
  public final ArrayDeque<String> history=new ArrayDeque<>();
  public long treasury,created;
  public boolean friendlyFire,hasHome;
  public int homeX,homeY,homeZ;
  public Faction(String name,UUID founder){this.name=name;this.founder=founder;this.created=System.currentTimeMillis();}
  public boolean owner(UUID id){return founder.equals(id);}
  public boolean officer(UUID id){return owner(id)||officers.contains(id);}
  public String role(UUID id){return owner(id)?"CHEF":officers.contains(id)?"OFFICIER":"MEMBRE";}
  public void log(String msg){history.addFirst(msg);while(history.size()>15)history.removeLast();}
  public String rank(){
   if(treasury>=10_000_000)return "DIAMANT";
   if(treasury>=2_000_000)return "EMERAUDE";
   if(treasury>=500_000)return "OR";
   if(treasury>=100_000)return "ARGENT";
   return "BRONZE";
  }
 }
 public static FactionData get(MinecraftServer server){
  return server.overworld().getDataStorage().computeIfAbsent(FactionData::load,FactionData::new,"worldbattles_factions_v1");
 }
 public Faction of(UUID id){String key=membership.get(id);return key==null?null:factions.get(key);}
 public Faction byName(String name){return factions.get(name.toLowerCase(Locale.ROOT));}
 public boolean nameFree(String name){return !factions.containsKey(name.toLowerCase(Locale.ROOT));}
 public void register(Faction f){
  String key=f.name.toLowerCase(Locale.ROOT);factions.put(key,f);
  for(UUID id:f.members.keySet())membership.put(id,key);
  setDirty();
 }
 public void add(Faction f,UUID id,String display){
  f.members.put(id,display);membership.put(id,f.name.toLowerCase(Locale.ROOT));setDirty();
 }
 public void remove(Faction f,UUID id){
  f.members.remove(id);f.officers.remove(id);membership.remove(id);setDirty();
 }
 public void dissolve(Faction f){for(UUID id:f.members.keySet())membership.remove(id);factions.remove(f.name.toLowerCase(Locale.ROOT));setDirty();}
 public List<Faction> leaderboard(){
  List<Faction> result=new ArrayList<>(factions.values());
  result.sort(Comparator.comparingLong((Faction f)->f.treasury).reversed().thenComparing(f->f.name));return result;
 }
 private static CompoundTag saveFaction(Faction f){
  CompoundTag tag=new CompoundTag();
  tag.putString("Name",f.name);tag.putString("Desc",f.description);tag.putString("Icon",f.icon);
  tag.putUUID("Founder",f.founder);tag.putLong("Treasury",f.treasury);tag.putLong("Created",f.created);
  tag.putBoolean("FriendlyFire",f.friendlyFire);tag.putBoolean("HasHome",f.hasHome);
  tag.putInt("HomeX",f.homeX);tag.putInt("HomeY",f.homeY);tag.putInt("HomeZ",f.homeZ);
  ListTag people=new ListTag();
  for(var entry:f.members.entrySet()){
   CompoundTag member=new CompoundTag();member.putUUID("Id",entry.getKey());
   member.putString("Name",entry.getValue());member.putBoolean("Officer",f.officers.contains(entry.getKey()));
   people.add(member);
  }
  tag.put("Members",people);
  ListTag logs=new ListTag();
  for(String value:f.history){CompoundTag v=new CompoundTag();v.putString("Line",value);logs.add(v);}
  tag.put("History",logs);return tag;
 }
 public static FactionData load(CompoundTag tag){
  FactionData data=new FactionData();
  for(Tag entry:tag.getList("Factions",Tag.TAG_COMPOUND)){
   CompoundTag n=(CompoundTag)entry;
   if(!n.hasUUID("Founder"))continue;
   String name=n.getString("Name");
   if(!name.matches("[A-Za-z0-9_]{3,16}"))continue;
   Faction f=new Faction(name,n.getUUID("Founder"));
   f.description=n.getString("Desc");f.icon=Arrays.asList(ICONS).contains(n.getString("Icon"))?n.getString("Icon"):"diamond";
   f.treasury=Math.max(0,Math.min(MAX_TREASURY,n.getLong("Treasury")));
   f.created=n.getLong("Created");f.friendlyFire=n.getBoolean("FriendlyFire");
   f.hasHome=n.getBoolean("HasHome");f.homeX=n.getInt("HomeX");f.homeY=n.getInt("HomeY");f.homeZ=n.getInt("HomeZ");
   for(Tag memberTag:n.getList("Members",Tag.TAG_COMPOUND)){
    CompoundTag m=(CompoundTag)memberTag;
    if(!m.hasUUID("Id")||f.members.size()>=MAX_MEMBERS)continue;
    UUID id=m.getUUID("Id");
    if(data.membership.containsKey(id))continue;
    f.members.put(id,m.getString("Name"));
    if(m.getBoolean("Officer")&&!id.equals(f.founder))f.officers.add(id);
    data.membership.put(id,name.toLowerCase(Locale.ROOT));
   }
   if(!f.members.containsKey(f.founder))continue;
   for(Tag log:n.getList("History",Tag.TAG_COMPOUND)){
    if(f.history.size()<15)f.history.addLast(((CompoundTag)log).getString("Line"));
   }
   data.factions.put(name.toLowerCase(Locale.ROOT),f);
  }
  return data;
 }
 @Override public CompoundTag save(CompoundTag result){
  ListTag list=new ListTag();
  for(Faction f:factions.values())list.add(saveFaction(f));
  result.put("Factions",list);return result;
 }
}
