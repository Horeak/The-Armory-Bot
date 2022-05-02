package Core.Commands.Warframe;

import Core.Main.Logging;
import Core.Main.Startup;
import Core.Objects.Annotation.Method.Interval;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;

public class WarframeItemSystem
{
	public static final String JSON_FILE_LOCATION = "https://raw.githubusercontent.com/WFCD/warframe-items/master/data/json/";
	public static final String IMAGE_LINK = "https://cdn.warframestat.us/img/";
	
	public static CopyOnWriteArrayList<org.json.JSONObject> itemsObjects = new CopyOnWriteArrayList<>();
	
	//TODO Find a way to get a list of all the files automatically
	private static final String[] files = new String[]{"Arcanes.json", "Arch-Gun.json", "Arch-Melee.json",
	                                                   "Archwing.json", "Gear.json", "Melee.json",
	                                                   "Misc.json", "Mods.json", "Pets.json", "Primary.json",
	                                                   "Resources.json", "Secondary.json", "SentinelWeapons.json",
	                                                   "Relics.json", "Warframes.json" };
	
	
	//TODO This is very memory intensive. Look at better options for loading large json files
	@Interval( time_interval = 6, time_unit = TimeUnit.HOURS )
	public static void load(){
		itemsObjects.clear();
		
		for(String t : files){
			loadItems(t);
		}
		
		System.out.println("Done loading warframe items. Loaded " + itemsObjects.size() + " objects");
	}
	
	public static void loadItems(String url)
	{
		try {
			InputStream is = new URL(JSON_FILE_LOCATION + url).openStream();
			BufferedReader rd = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));

			JSONParser helper = new JSONParser();
			JSONArray data = (JSONArray)helper.parse(rd);
			
			int num = 0;
			
			top:
			for(Object ob : data){
				if(ob instanceof JSONObject){
					JSONObject object = (JSONObject)ob;
					String jsonText = object.toJSONString();
					
					org.json.JSONObject object1 = new org.json.JSONObject(jsonText);
					
					if(object1 != null){
						if(!object1.has("name")) continue;
						
						if(object1.has("category")){
							String cat = object1.getString("category");
							
							for(String t : ignoredCategories){
								if(cat.equalsIgnoreCase(t)){
									continue top;
								}
							}
							
							if(object1.has("patchlogs")){
								object1.remove("patchlogs");
							}
							
							itemsObjects.add(object1);
							num++;
						}
					}
				}
			}
			
			if(Startup.debug){
				System.out.println("Done loading warframe file: " + url + ", Loaded " + num + " objects");
			}
			
			rd.close();
			is.close();
		} catch (IOException | ParseException e) {
			Logging.exception(e);
		}
	}
	
	public static final String[] componentNames = new String[]{
			"blueprint",
			"systems",
			"chassis",
			"neuroptics",
			"barrel",
			"receiver",
			"grip",
			"string",
			"lower limb",
			"upper limb",
			"blade",
			"handle"
	};
	
	public static final String[] ignoredCategories = new String[]{"skins"};
	
	public static org.json.JSONObject getItem(String name){
		for(org.json.JSONObject object : itemsObjects){
			if(object.has("name")){
				String nam = object.getString("name");
				
				if(name.equalsIgnoreCase(nam)){
					if(!object.has("drops")) {
						return object;
					}
				}
			}
		}
		
		return null;
	}
	
	public static ArrayList<org.json.JSONObject> getDropItem(String name){
		ArrayList<org.json.JSONObject> list = new ArrayList<>();
		
		top:
		for(org.json.JSONObject object : itemsObjects){
			if(object.has("components")){
				org.json.JSONArray array = object.getJSONArray("components");
				
				for(Object obj : array){
					if(obj instanceof org.json.JSONObject){
						org.json.JSONObject jObject = (org.json.JSONObject)obj;
						
						String compName = jObject.getString("name");

						if(name.equalsIgnoreCase(compName)){
							if(jObject.has("itemCount")) {
								for (org.json.JSONObject ok : list) {
									
									if(jObject.toString().equalsIgnoreCase(ok.toString())){
										continue top;
									}
								}
							}
							
							list.add(jObject);
						}
					}
				}
			}
		}
		
		return list;
	}
}
