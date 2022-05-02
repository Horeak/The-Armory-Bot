package Core.Commands.BDO;

import Core.Main.Logging;
import Core.Main.Startup;
import Core.Objects.Annotation.Fields.DataObject;
import Core.Objects.Annotation.Method.Interval;
import Core.Objects.Annotation.Method.Startup.PostInit;
import Core.Util.Utils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class BDOItemIndex
{
	public static String BASE_URL = "https://bddatabase.net/";
	
	@DataObject(file_path = "bdo/item_index.json", name = "item_index")
	public static ConcurrentHashMap<Integer, String> ITEM_INDEX = new ConcurrentHashMap<>();
	
	@DataObject(file_path = "bdo/item_index.json", name = "enhancement")
	public static ConcurrentHashMap<Integer, Integer> ITEM_ENHANCEMENTS = new ConcurrentHashMap<>();
	public static SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");
	
	@PostInit
	public static void loadItems(){
		Startup.executor.execute(() -> {
			if(ITEM_INDEX.size() <= 0) {
				loadItemsWithVal("");
			}
		});
	}
	
	public static void loadItemsWithVal(String val){
		loadItems("a=items" + val); //No idea what this is?
		loadItems("a=items&type=powerup"); //Upgrade items
		loadItems("a=items&type=materials"); //Materials
		loadItems("a=items&type=stones"); //Alchemy stones
		loadItems("a=items&type=gems"); //Magic crystals
		loadItems("a=items&type=tools" + val); //Life skill tools
		
		//loadItems("a=costume" + val); //Costumes
		
		loadItems("a=armor" + val); //Armor
		loadItems("a=armor&type=work" + val); //Work clothes
		loadItems("a=armor&type=body" + val); //Body armor
		loadItems("a=armor&type=hand" + val); //Gloves
		loadItems("a=armor&type=head" + val); //Helmets
		loadItems("a=armor&type=foot" + val); //Shoes
		
		loadItems("a=weapon" + val); //Main weapons
		loadItems("a=weapon&type=longsword" + val);
		loadItems("a=weapon&type=longbow" + val);
		loadItems("a=weapon&type=amulet" + val);
		loadItems("a=weapon&type=axe" + val);
		loadItems("a=weapon&type=blade" + val);
		loadItems("a=weapon&type=shortsword" + val);
		loadItems("a=weapon&type=staff" + val);
		loadItems("a=weapon&type=krieg" + val);
		loadItems("a=weapon&type=gauntlet" + val);
		loadItems("a=weapon&type=pendulum" + val);
		loadItems("a=weapon&type=crossbow" + val);
		loadItems("a=weapon&type=florang" + val);
		loadItems("a=weapon&type=battleaxe" + val);
		loadItems("a=weapon&type=shamshir" + val);
		loadItems("a=weapon&type=morgenshtern" + val);
		loadItems("a=weapon&type=kyve" + val);
		
		loadItems("a=subweapon" + val);//Sub weapons
		loadItems("a=subweapon&type=shield" + val);
		loadItems("a=subweapon&type=dagger" + val);
		loadItems("a=subweapon&type=talisman" + val);
		loadItems("a=subweapon&type=knot" + val);
		loadItems("a=subweapon&type=trinket" + val);
		loadItems("a=subweapon&type=shortbow" + val);
		loadItems("a=subweapon&type=kunai" + val);
		loadItems("a=subweapon&type=star" + val);
		loadItems("a=subweapon&type=vambrace" + val);
		loadItems("a=subweapon&type=noblesword" + val);
		loadItems("a=subweapon&type=harpoon" + val);
		loadItems("a=subweapon&type=vitclari" + val);
		loadItems("a=subweapon&type=haladie" + val);
		loadItems("a=subweapon&type=kratum" + val);
		
		loadItems("a=awakening" + val); //Awakening weapons
		loadItems("a=awakening&type=2hsword" + val);
		loadItems("a=awakening&type=scythe" + val);
		loadItems("a=awakening&type=handgun" + val);
		loadItems("a=awakening&type=elementblade" + val);
		loadItems("a=awakening&type=chanbon" + val);
		loadItems("a=awakening&type=spear" + val);
		loadItems("a=awakening&type=glaive" + val);
		loadItems("a=awakening&type=snakespear" + val);
		loadItems("a=awakening&type=asurablade" + val);
		loadItems("a=awakening&type=chakram" + val);
		loadItems("a=awakening&type=naturesphere" + val);
		loadItems("a=awakening&type=elementsphere" + val);
		loadItems("a=awakening&type=vediant" + val);
		loadItems("a=awakening&type=bracers" + val);
		loadItems("a=awakening&type=cestus" + val);
		loadItems("a=awakening&type=glaives" + val);
		loadItems("a=awakening&type=bow" + val);
		loadItems("a=awakening&type=jordun" + val);
		loadItems("a=awakening&type=dualglaives" + val);
		loadItems("a=awakening&type=sting" + val);
		loadItems("a=awakening&type=kibelius" + val);
		
		loadItems("a=accessory" + val); //Accessories
		loadItems("a=accessory&type=ring" + val);
		loadItems("a=accessory&type=necklace" + val);
		loadItems("a=accessory&type=earring" + val);
		loadItems("a=accessory&type=belt" + val);
		
		System.out.println("Done loading bdo items.");
	}
	
	public static void loadItems(String url){
		url = BASE_URL + "query.php?" + url;
		System.out.println("Loading items from url: " + url);
		BDOItemUtils.rateLimiter.acquire(5);
		
		try {
			Document doc = Jsoup.connect(url).ignoreContentType(true).requestBody("JSON").post();
			String text = doc.select("body").text();
			
			Object tg = new JSONTokener(text).nextValue();
			
			if(tg instanceof JSONObject) {
				JSONObject object = (JSONObject)tg;
				
				if(object.has("aaData")){
					JSONArray array = object.getJSONArray("aaData");
					
					for(Object ob : array){
						JSONArray arrayObject = (JSONArray)ob;
						
						String id = arrayObject.getString(0);
						String name = Jsoup.parseBodyFragment(arrayObject.getString(2)).text();
						
						//TODO Find a way to add enhance properly
						int enhance = 20;
						
						String cleanName = BDOItemUtils.cleanString(name).trim();
						
						if (!cleanName.isBlank()) {
							if (Utils.isInteger(id)) {
								Integer nId = Integer.parseInt(id);
								
								if (!BDOItemIndex.ITEM_INDEX.containsKey(nId)) {
									BDOItemIndex.ITEM_INDEX.put(nId, name);
									
									System.out.println("Added BDO item: \"" + name + "\" with id: " + nId);
									
									if(enhance > 0){
										ITEM_ENHANCEMENTS.put(nId, enhance);
									}
								}
							}else{
								System.err.println("Non int: " + name + " -> " + id);
							}
						}else{
							System.err.println(name + " -> " + id);
						}
					}
				}
			}
			
//			String[] ll = text.split("img src=");
//
//			for (String s : ll) {
//				String sk = "<\\/span>";
//				String sk1 = "<\\/b>";
//
//				if(s.contains(sk)) {
//					String name = s.substring(s.indexOf(sk) + sk.length());
//					name = name.substring(0, name.indexOf(sk1)).trim();
//
//					String s1 = "data-id=\\\"item--";
//					String id = s.substring(s.indexOf(s1) + s1.length());
//					id = id.substring(0, id.indexOf("\\\"")).trim();
//
//					int enhance = StringUtils.countMatches(s, "\"ev\\\"") - 1;
//
//					if(enhance <= 0){
//						if(url.contains("a=items&type=tools")){
//							if(name.contains("Manos") || name.contains("Fishing")
//							|| name.contains("Dostter") || name.contains(" Float")
//							|| name.contains("Loggia") || name.contains("Techthon")){
//								enhance = 20;
//							}
//
//							if(name.contains("Matchlock")){
//								enhance = 10;
//							}
//						}
//					}
//
//					String cleanName = BDOItemUtils.cleanString(name).trim();
//
//					if (!cleanName.isBlank()) {
//						if (Utils.isInteger(id)) {
//							Integer nId = Integer.parseInt(id);
//
//							if (!BDOItemIndex.ITEM_INDEX.containsKey(nId)) {
//								BDOItemIndex.ITEM_INDEX.put(nId, name);
//
//								System.out.println("Added BDO item: \"" + name + "\" with id: " + nId);
//
//								if(enhance > 0){
//									ITEM_ENHANCEMENTS.put(nId, enhance);
//								}
//							}
//						}else{
//							System.err.println("Non int: " + name + " -> " + id);
//						}
//					}else{
//						System.err.println(name + " -> " + id);
//					}
//				}
//			}
		}catch (Exception e){
			Logging.exception(e);
		}
	}
	
	public static String fullCleanString(String text){
		text = BDOItemUtils.cleanString(text);
		text = text.replace("(", "").replace(")", "").trim();
		text = text.replace("[", " ").replace("]", " ").trim();
		text = text.replaceAll("[^a-zA-Z0-9\\s\\\\]", "");
		return text;
	}
	
	@Interval( time_unit = TimeUnit.DAYS, time_interval = 1, initial_delay = 1)
	public static void checkForUpdate(){
		loadItemsWithVal("&type=version&slot=" + format.format(new Date()));
	}
	
	@Interval( time_unit = TimeUnit.DAYS, time_interval = 3, initial_delay = 3)
	public static void fullUpdateCheck(){
		loadItemsWithVal("");
		
		for(Entry<Integer, BDOItem> ent : BDOItemUtils.ITEM_CACHE.entrySet()){
			if(System.currentTimeMillis() - ent.getValue().updateTime > TimeUnit.DAYS.toMillis(7)){
				BDOItemUtils.ITEM_CACHE.remove(ent.getKey());
			}
		}
	}
}
