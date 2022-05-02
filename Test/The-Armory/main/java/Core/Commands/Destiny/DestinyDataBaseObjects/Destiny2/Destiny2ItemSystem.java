package Core.Commands.Destiny.DestinyDataBaseObjects.Destiny2;

import Core.Main.Logging;
import Core.Util.JsonUtils;
import Core.Util.Utils;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static Core.Commands.Destiny.DestinySystem.d2_connect;

public class Destiny2ItemSystem
{
	public static final ConcurrentHashMap<Integer, Destiny2PowerCapObject> destinyPowerCapObjects = new ConcurrentHashMap<>();
	public static final ConcurrentHashMap<Integer, Destiny2ItemObject> destinyItemObjects = new ConcurrentHashMap<>();
	public static final ConcurrentHashMap<Integer, Destiny2StatObject> destinyStatObjects = new ConcurrentHashMap<>();
	public static final ConcurrentHashMap<Integer, Destiny2PerkObject> destinyPerkObjects = new ConcurrentHashMap<>();
	public static final ConcurrentHashMap<Integer, Destiny2CollectibleObject> destinyCollectibleObjects = new ConcurrentHashMap<>();
	public static final ConcurrentHashMap<Integer, Destiny2DamageTypeObject> destinyDamageTypeObjects = new ConcurrentHashMap<>();
	public static final ConcurrentHashMap<Integer, Destiny2ClassObject> destinyClassObjects = new ConcurrentHashMap<>();
	public static final ConcurrentHashMap<Integer, Destiny2StatGroupObject> destinyStatGroupObjects = new ConcurrentHashMap<>();
	public static final ConcurrentHashMap<Integer, Destiny2PlugSetObject> destinyPlugSetObjects = new ConcurrentHashMap<>();
	public static final ConcurrentHashMap<Integer, Destiny2VendorObject> destinyVendorObjects = new ConcurrentHashMap<>();
	public static final ConcurrentHashMap<Integer, Destiny2ItemBucket> destinyBucketObjects = new ConcurrentHashMap<>();
	
	
	public static final ConcurrentHashMap<Long, Integer> destinySeasonNumbers = new ConcurrentHashMap<>();
	public static final ConcurrentHashMap<Integer, Destiny2SeasonObject> destinySeasons = new ConcurrentHashMap<>();
	
	public static final String SEASON_URL = "https://raw.githubusercontent.com/DestinyItemManager/d2-additional-info/master/data/seasons/seasons_unfiltered.json";
	public static final String SEASON_INFO_URL = "https://raw.githubusercontent.com/DestinyItemManager/d2-additional-info/master/data/seasons/d2-season-info.ts";
	
	public static final String[] IGNORED_TYPES = new String[]{
			"bounty", "quest", "clan"
	};
	
	public static void reInit()
	{
		try {
			clear();
			init();
		} catch (Exception e) {
			Logging.exception(e);
		}
	}
	
	public static void clear()
	{
		destinyItemObjects.clear();
		destinyStatObjects.clear();
		destinyPerkObjects.clear();
		destinyCollectibleObjects.clear();
		destinyDamageTypeObjects.clear();
		destinyClassObjects.clear();
		destinyStatGroupObjects.clear();
		destinyPlugSetObjects.clear();
		destinyVendorObjects.clear();
		destinyPowerCapObjects.clear();
		destinyBucketObjects.clear();
		
		destinySeasonNumbers.clear();
		destinySeasons.clear();
	}
	
	private static final DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
	
	
	public static void init()
	{
		try {
			InputStream is = new URL(SEASON_URL).openStream();
			BufferedReader rd = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
			StringBuilder sb = new StringBuilder();
			int cp;
			while ((cp = rd.read()) != -1) {
				sb.append((char)cp);
			}
			
			String jsonText = sb.toString();
			JSONObject json = new JSONObject(jsonText);
			
			for (String key : json.keySet()) {
				int season = json.getInt(key);
				Long keyNum = Long.parseLong(key);
				destinySeasonNumbers.put(keyNum, season);
			}
			
			is.close();
		} catch (IOException e) {
			Logging.exception(e);
		}
		
		InputStream is = null;
		try {
			is = new URL(SEASON_INFO_URL).openStream();
			BufferedReader rd = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
			StringBuilder sb = new StringBuilder();
			int cp;
			while ((cp = rd.read()) != -1) {
				sb.append((char)cp);
			}
			
			String text = sb.toString();
			String[] seasons = text.split(": \\{");
			
			
			for (String t : seasons) {
				HashMap<String, String> values = new HashMap<>();
				String[] lines = t.split(",");
				
				for (String line : lines) {
					if (line.contains(":")) {
						String[] val = line.replace("\n", "").replace("\"", "").replace("'", "").split(":", 2);
						
						if (val.length == 2) {
							values.put(val[0].replace(" ", ""), val[1]);
						}
					}
				}
				
				if (values.containsKey("DLCName")) {
					Destiny2SeasonObject seasonObject = new Destiny2SeasonObject();
					
					if (values.containsKey("maxLevel")) {
						seasonObject.maxLevel = Integer.parseInt(values.get("maxLevel").replace(" ", ""));
					}
					
					if (values.containsKey("powerFloor")) {
						seasonObject.powerFloor = Integer.parseInt(values.get("powerFloor").replace(" ", ""));
					}
					
					if (values.containsKey("softCap")) {
						seasonObject.softCap = Integer.parseInt(values.get("softCap").replace(" ", ""));
					}
					
					if (values.containsKey("powerfulCap")) {
						seasonObject.powerfulCap = Integer.parseInt(values.get("powerfulCap").replace(" ", ""));
					}
					
					if (values.containsKey("pinnacleCap")) {
						seasonObject.pinnacleCap = Integer.parseInt(values.get("pinnacleCap").replace(" ", ""));
					}
					
					if (values.containsKey("year")) {
						seasonObject.year = Integer.parseInt(values.get("year").replace(" ", ""));
					}
					
					if (values.containsKey("season")) {
						seasonObject.season = Integer.parseInt(values.get("season").replace(" ", ""));
					}
					
					if (values.containsKey("releaseDate")) {
						String date = values.get("releaseDate").replace(" ", "");
						
						try {
							seasonObject.releaseDate = formatter.parse(date);
						} catch (ParseException e) {
							Logging.exception(e);
						}
						
					}
					
					if (values.containsKey("seasonName")) {
						seasonObject.seasonName = values.get("seasonName");
						
						if(seasonObject.seasonName != null){
							seasonObject.seasonName = seasonObject.seasonName.strip();
						}
					}
					
					if (values.containsKey("DLCName")) {
						seasonObject.DLCName = values.get("DLCName");
						
						if(seasonObject.DLCName != null){
							seasonObject.DLCName = seasonObject.DLCName.strip();
						}
					}
					
					destinySeasons.put(seasonObject.season, seasonObject);
				}
			}
			
			
		} catch (IOException e) {
			Logging.exception(e);
		}finally {
			try {
				is.close();
			} catch (IOException e) {
				Logging.exception(e);
			}
		}
		
		
		try {
			PreparedStatement statement = null;
			ResultSet rs = null;
			Connection con = d2_connect();
			
			try {
				try {
					statement = con.prepareStatement("select * from DestinyInventoryItemDefinition");
					rs = statement.executeQuery();
					
					while (rs.next()) {
						Long id = Long.parseLong(rs.getString(1));
						String json = rs.getString(2);
						
						if (json == null || json.isEmpty()) {
							continue;
						}
						
						Destiny2ItemObject object = JsonUtils.getGson_non_pretty().fromJson(json,
						                                                                    Destiny2ItemObject.class);
						
						if (object != null) {
							if ((object.displayProperties == null || object.displayProperties.name == null || object.displayProperties.name.isEmpty()) && (object.displayProperties.description == null || object.displayProperties.description.isEmpty())) {
								continue;
							}
							
							destinyItemObjects.put(id.intValue(), object);
						}
						
					}
				} catch (SQLException e) {
					Logging.exception(e);
				}
			} finally {
				statement.close();
				rs.close();
				con.close();
			}
		} catch (Exception e) {
			Logging.exception(e);
		}
		
		try {
			PreparedStatement statement = null;
			ResultSet rs = null;
			Connection con = d2_connect();
			
			try {
				try {
					statement = con.prepareStatement("select * from DestinyStatDefinition");
					rs = statement.executeQuery();
					
					while (rs.next()) {
						Long id = Long.parseLong(rs.getString(1));
						String json = rs.getString(2);
						
						if (json == null || json.isEmpty()) {
							continue;
						}
						
						Destiny2StatObject object = JsonUtils.getGson_non_pretty().fromJson(json,
						                                                                    Destiny2StatObject.class);
						
						if (object != null) {
							if (object.displayProperties == null || object.displayProperties.name == null || object.displayProperties.name.isEmpty() || object.displayProperties.description == null || object.displayProperties.description.isEmpty()) {
								continue;
							}
							
							destinyStatObjects.put(id.intValue(), object);
						}
						
					}
					
				} catch (SQLException e) {
					Logging.exception(e);
				}
			} finally {
				statement.close();
				rs.close();
				con.close();
			}
		} catch (Exception e) {
			Logging.exception(e);
		}
		
		try {
			PreparedStatement statement = null;
			ResultSet rs = null;
			Connection con = d2_connect();
			
			try {
				try {
					statement = con.prepareStatement("select * from DestinyClassDefinition");
					rs = statement.executeQuery();
					
					while (rs.next()) {
						Long id = Long.parseLong(rs.getString(1));
						String json = rs.getString(2);
						
						if (json == null || json.isEmpty()) {
							continue;
						}
						
						Destiny2ClassObject object = JsonUtils.getGson_non_pretty().fromJson(json,
						                                                                     Destiny2ClassObject.class);
						
						if (object != null) {
							if (object.displayProperties == null || object.displayProperties.name == null || object.displayProperties.name.isEmpty()) {
								continue;
							}
							
							destinyClassObjects.put(id.intValue(), object);
						}
						
					}
				} catch (SQLException e) {
					Logging.exception(e);
				}
			} finally {
				statement.close();
				rs.close();
				con.close();
			}
		} catch (Exception e) {
			Logging.exception(e);
		}
		
		try {
			PreparedStatement statement = null;
			ResultSet rs = null;
			Connection con = d2_connect();
			
			try {
				try {
					statement = con.prepareStatement("select * from DestinyCollectibleDefinition");
					rs = statement.executeQuery();
					
					while (rs.next()) {
						Long id = Long.parseLong(rs.getString(1));
						String json = rs.getString(2);
						
						if (json == null || json.isEmpty()) {
							continue;
						}
						
						Destiny2CollectibleObject object = JsonUtils.getGson_non_pretty().fromJson(json,
						                                                                           Destiny2CollectibleObject.class);
						
						if (object != null) {
							if ((object.displayProperties == null || object.displayProperties.name == null || object.displayProperties.name.isEmpty()) && (object.sourceString == null || object.sourceString.isEmpty())) {
								continue;
							}
							
							destinyCollectibleObjects.put(id.intValue(), object);
						}
						
					}
					
				} catch (SQLException e) {
					Logging.exception(e);
				}
			} finally {
				statement.close();
				rs.close();
				con.close();
			}
		} catch (Exception e) {
			Logging.exception(e);
		}
		
		try {
			PreparedStatement statement = null;
			ResultSet rs = null;
			Connection con = d2_connect();
			
			try {
				try {
					statement = con.prepareStatement("select * from DestinyDamageTypeDefinition");
					rs = statement.executeQuery();
					
					while (rs.next()) {
						Long id = Long.parseLong(rs.getString(1));
						String json = rs.getString(2);
						
						if (json == null || json.isEmpty()) {
							continue;
						}
						
						Destiny2DamageTypeObject object = JsonUtils.getGson_non_pretty().fromJson(json,
						                                                                          Destiny2DamageTypeObject.class);
						
						if (object != null) {
							if (object.displayProperties == null || object.displayProperties.name == null || object.displayProperties.name.isEmpty() || object.displayProperties.description == null || object.displayProperties.description.isEmpty()) {
								continue;
							}
							
							destinyDamageTypeObjects.put(id.intValue(), object);
						}
						
					}
				} catch (SQLException e) {
					Logging.exception(e);
				}
			} finally {
				statement.close();
				rs.close();
				con.close();
			}
		} catch (Exception e) {
			Logging.exception(e);
		}
		
		try {
			PreparedStatement statement = null;
			ResultSet rs = null;
			Connection con = d2_connect();
			
			try {
				try {
					statement = con.prepareStatement("select * from DestinySandboxPerkDefinition");
					rs = statement.executeQuery();
					
					while (rs.next()) {
						Long id = Long.parseLong(rs.getString(1));
						String json = rs.getString(2);
						
						if (json == null || json.isEmpty()) {
							continue;
						}
						
						Destiny2PerkObject object = JsonUtils.getGson_non_pretty().fromJson(json,
						                                                                    Destiny2PerkObject.class);
						
						if (object != null) {
							if (object.displayProperties == null || object.displayProperties.name == null || object.displayProperties.name.isEmpty() || object.displayProperties.description == null || object.displayProperties.description.isEmpty()) {
								continue;
							}
							
							destinyPerkObjects.put(id.intValue(), object);
						}
						
					}
				} catch (SQLException e) {
					Logging.exception(e);
				}
			} finally {
				statement.close();
				rs.close();
				con.close();
			}
		} catch (Exception e) {
			Logging.exception(e);
		}
		
		try {
			PreparedStatement statement = null;
			ResultSet rs = null;
			Connection con = d2_connect();
			
			try {
				try {
					statement = con.prepareStatement("select * from DestinyStatGroupDefinition");
					rs = statement.executeQuery();
					
					while (rs.next()) {
						Long id = Long.parseLong(rs.getString(1));
						String json = rs.getString(2);
						
						if (json == null || json.isEmpty()) {
							continue;
						}
						
						Destiny2StatGroupObject object = JsonUtils.getGson_non_pretty().fromJson(json,
						                                                                         Destiny2StatGroupObject.class);
						
						if (object != null) {
							destinyStatGroupObjects.put(id.intValue(), object);
						}
						
					}
					
				} catch (SQLException e) {
					Logging.exception(e);
				}
			} finally {
				statement.close();
				rs.close();
				con.close();
			}
		} catch (Exception e) {
			Logging.exception(e);
		}
		
		try {
			PreparedStatement statement = null;
			ResultSet rs = null;
			Connection con = d2_connect();
			
			try {
				statement = con.prepareStatement("select * from DestinyPlugSetDefinition");
				rs = statement.executeQuery();
				while (rs.next()) {
					Long id = Long.parseLong(rs.getString(1));
					String json = rs.getString(2);
					
					if (json == null || json.isEmpty()) {
						continue;
					}
					
					Destiny2PlugSetObject object = JsonUtils.getGson_non_pretty().fromJson(json,
					                                                                       Destiny2PlugSetObject.class);
					
					if (object != null) {
						destinyPlugSetObjects.put(id.intValue(), object);
					}
				}
				
				
			} catch (Exception e) {
				Logging.exception(e);
			} finally {
				statement.close();
				rs.close();
				con.close();
			}
		} catch (Exception e) {
			Logging.exception(e);
		}
		
		
		try {
			PreparedStatement statement = null;
			ResultSet rs = null;
			Connection con = d2_connect();
			
			try {
				statement = con.prepareStatement("select * from DestinyVendorDefinition");
				rs = statement.executeQuery();
				while (rs.next()) {
					Long id = Long.parseLong(rs.getString(1));
					String json = rs.getString(2);
					
					if (json == null || json.isEmpty()) {
						continue;
					}
					
					Destiny2VendorObject object = JsonUtils.getGson_non_pretty().fromJson(json,
					                                                                      Destiny2VendorObject.class);
					
					if (object != null) {
						destinyVendorObjects.put(id.intValue(), object);
					}
					
				}
				
				
			} catch (Exception e) {
				Logging.exception(e);
			} finally {
				statement.close();
				rs.close();
				con.close();
			}
		} catch (Exception e) {
			Logging.exception(e);
		}
		
		try {
			PreparedStatement statement = null;
			ResultSet rs = null;
			Connection con = d2_connect();
			
			try {
				try {
					statement = con.prepareStatement("select * from DestinyPowerCapDefinition");
					rs = statement.executeQuery();
					
					while (rs.next()) {
						Long id = Long.parseLong(rs.getString(1));
						String json = rs.getString(2);
						
						if (json == null || json.isEmpty()) {
							continue;
						}
						
						Destiny2PowerCapObject object = JsonUtils.getGson_non_pretty().fromJson(json, Destiny2PowerCapObject.class);
						
						if (object != null) {
							destinyPowerCapObjects.put(id.intValue(), object);
						}
					}
					
				} catch (SQLException e) {
					Logging.exception(e);
				}
			} finally {
				statement.close();
				rs.close();
				con.close();
			}
		} catch (Exception e) {
			Logging.exception(e);
		}
		
		try {
			PreparedStatement statement = null;
			ResultSet rs = null;
			Connection con = d2_connect();
			
			try {
				try {
					statement = con.prepareStatement("select * from DestinyInventoryBucketDefinition");
					rs = statement.executeQuery();
					
					while (rs.next()) {
						Long id = Long.parseLong(rs.getString(1));
						String json = rs.getString(2);
						
						if (json == null || json.isEmpty()) {
							continue;
						}
						
						Destiny2ItemBucket object = JsonUtils.getGson_non_pretty().fromJson(json,
						                                                                    Destiny2ItemBucket.class);
						
						if (object != null) {
							if ((object.displayProperties == null || object.displayProperties.name == null || object.displayProperties.name.isEmpty()) && (object.displayProperties.description == null || object.displayProperties.description.isEmpty())) {
								continue;
							}
							
							destinyBucketObjects.put(id.intValue(), object);
						}
						
					}
				} catch (SQLException e) {
					Logging.exception(e);
				}
			} finally {
				statement.close();
				rs.close();
				con.close();
			}
		} catch (Exception e) {
			Logging.exception(e);
		}
		
		for (Map.Entry<Integer, Destiny2ItemObject> ent : destinyItemObjects.entrySet()) {
			if (ent.getValue() != null) {
				ent.getValue().finalizeObject();
			} else {
				destinyItemObjects.remove(ent.getKey(), ent.getValue());
			}
		}
		
		System.out.println("Done initializing destiny 2 systems.");
	}
	
	public static Destiny2ItemObject getItemByName(
			String name)
	{
		ArrayList<Destiny2ItemObject> list = getItemsByName(name);
		return list.size() > 0 ? list.get(0) : null;
	}
	
	
	public static ArrayList<Destiny2ItemObject> getItemsByName(String name)
	{
		return getItemsByName(name, true);
	}
	
	public static ArrayList<Destiny2ItemObject> getItemsByName(
			String name, boolean curated)
	{
		ArrayList<Destiny2ItemObject> inObjects = new ArrayList<>(destinyItemObjects.values());
		
		addSandboxPerks(name, inObjects);
		
		if (inObjects.isEmpty()) return new ArrayList<>();
		
		inObjects.removeIf(Objects::isNull);
		
		//TODO Some old destiny items have had their description removed?
		
		inObjects.removeIf(
				(ob) -> ob.displayProperties == null || ob.displayProperties.name == null || ob.displayProperties.name.isEmpty());
//		inObjects.removeIf((ob) -> ob.displayProperties.description == null || ob.displayProperties.description.isEmpty());
		inObjects.removeIf((ob) -> ob.displayProperties.name == null || ob.displayProperties.name.isEmpty());
		inObjects.removeIf((ob) -> {
			String j1 = ob.displayProperties.name.toLowerCase().replace(" ", "");
			String j2 = name.toLowerCase().replace(" ", "");
			
			return !j1.contains(j2) && !j1.equals(j2);
		});
		
		inObjects.removeIf((ob) -> Arrays.stream(IGNORED_TYPES).anyMatch(
				(s) -> ob.itemTypeDisplayName != null && ob.itemTypeDisplayName.toLowerCase().contains(s.toLowerCase())));
		handleCurated(inObjects);
		
		inObjects.forEach((ob) -> ob.destinyVersion = 2);
		
		inObjects.sort(
				Comparator.comparingDouble(o -> Utils.compareStrings(o.getName().toLowerCase().replace(" ", ""), name.toLowerCase().replace(" ", ""))));
		inObjects.sort((j1, j2) -> (j1.equippable && j2.equippable) ? (Integer.compare(j2.itemType,
		                                                                               j1.itemType)) : (j1.equippable ? -1 : j2.equippable ? 1 : 0));
		inObjects.sort((j1, j2) -> (j1.curated && j2.curated) ? (Integer.compare(j2.itemType,
		                                                                         j1.itemType)) : (j1.curated ? 1 : j2.curated ? -1 : 0));
		inObjects.sort(
				(j1, j2) -> (j1.inventory == null && j2.inventory != null) ? -1 : (j1.inventory != null && j2.inventory == null) ? 1 : (j1.inventory == null && j2.inventory == null) ? 0 : Integer.compare(
						j2.inventory.tierType, j1.inventory.tierType));
		inObjects.sort((j1, j2) -> (Integer.compare(j2.source != null && !j2.source.isEmpty() ? 1 : 0,
		                                            j1.source != null && !j1.source.isEmpty() ? 1 : 0)));
		
		//If items have same rarity then weapons get priority
		inObjects.sort((j1, j2) -> {
			if(j1.inventory != null && j2.inventory != null && j1.inventory.tierType == j2.inventory.tierType){
				if(j1.itemType == 3 && j2.itemType != 3){
					return -1;
				}else if(j2.itemType == 3 && j1.itemType != 3){
					return 1;
				}
			}

			return 0;
		});
		
		handleEmptyItemType(inObjects);
		
		return inObjects;
	}
	
	//This is hard coded and needs to be changed as soon as possible
	@Deprecated
	protected static void handleEmptyItemType(ArrayList<Destiny2ItemObject> inObjects)
	{
		inObjects.forEach((o) -> {
			if (o.itemTypeDisplayName == null || o.itemTypeDisplayName.isEmpty() || o.itemTypeAndTierDisplayName == null || o.itemTypeAndTierDisplayName.isEmpty()) {
				
				if (o.plug != null) {
					if (o.plug.uiPlugLabel != null && !o.plug.uiPlugLabel.isEmpty()) {
						switch (o.plug.uiPlugLabel.toLowerCase()) {
							case "masterwork": {
								if (o.displayProperties.name.toLowerCase().contains("catalyst")) {
									o.itemTypeDisplayName = "Catalyst";
									o.itemTypeAndTierDisplayName = "Catalyst";
								} else {
									o.itemTypeDisplayName = "Masterwork";
									o.itemTypeAndTierDisplayName = "Masterwork";
								}
							}
						}
					}
				}
			}
		});
	}
	
	protected static void addSandboxPerks(
			String name, ArrayList<Destiny2ItemObject> inObjects)
	{
		ArrayList<Destiny2PerkObject> perkObjects = new ArrayList<>(destinyPerkObjects.values());
		
		perkObjects.removeIf(Objects::isNull);
		
		perkObjects.removeIf(
				(ob) -> ob.displayProperties == null || ob.displayProperties.name == null || ob.displayProperties.name.isEmpty());
		perkObjects.removeIf(
				(ob) -> ob.displayProperties.description == null || ob.displayProperties.description.isEmpty());
		perkObjects.removeIf((ob) -> ob.displayProperties.name == null || ob.displayProperties.name.isEmpty());
		perkObjects.removeIf((ob) -> !ob.displayProperties.name.toLowerCase().contains(name.toLowerCase()));
		
		
		perkObjects.sort((j1, j2) -> {
			Pattern p = Pattern.compile("(?i)(?:^|\\W)" + name + "(?:$|\\W)");
			Matcher m1 = p.matcher(j1.displayProperties.name);
			Matcher m2 = p.matcher(j2.displayProperties.name);
			
			boolean e1 = m1.find();
			boolean e2 = m2.find();
			
			return e1 && e2 ? 0 : e1 ? -1 : e2 ? 1 : 0;
		});
		
		if (perkObjects.size() > 0) {
			for (Destiny2PerkObject object : perkObjects) {
				Destiny2ItemObject object1 = new Destiny2ItemObject();
				object1.displayProperties = object.displayProperties;
				object1.hash = object.hash;
				
				object1.itemTypeDisplayName = "Perk";
				object1.itemTypeAndTierDisplayName = "Perk";
				
				inObjects.add(object1);
			}
		}
	}
	
	private static void handleCurated(ArrayList<Destiny2ItemObject> list)
	{
		ArrayList<Destiny2ItemObject> addObjects = new ArrayList<>();
		
		for (Destiny2ItemObject object : list) {
			if (!object.curated && !object.preset) {
				if (object.inventory != null && object.inventory.tierType != 5) continue; //Only curated legendaries
				
				boolean hasCurated = true;
				
				if (object.sockets != null && object.sockets.socketEntries != null) {
					for (Destiny2ItemObject.SocketEntry socket : object.sockets.socketEntries) {
						if (socket.singleInitialItemHash == null) {
							hasCurated = false;
							break;
						}
					}
				}
				
				if (hasCurated) {
					//This is done using json to prevent objects being used as refrences
					String json = JsonUtils.getGson_non_pretty().toJson(object);
					Destiny2ItemObject curatedCopy = JsonUtils.getGson_non_pretty().fromJson(json,
					                                                                         Destiny2ItemObject.class);
					if (curatedCopy != null) {
						curatedCopy.curated = true;
						curatedCopy.finalizeObject();
						
						//Curated and preset weapons are the only weapons with only one masterwork and only curated weapons should be detected here
						if (curatedCopy.masterworkObjects.size() == 1) {
							addObjects.add(curatedCopy);
						}
					}
				}
			}
		}
		
		list.addAll(addObjects);
	}
	
	
	public static ArrayList<Destiny2ItemObject> getSinglePerks(Destiny2ItemObject object)
	{
		ArrayList<Destiny2ItemObject> list = new ArrayList<>();
		
		if (object.sockets != null && object.sockets.socketEntries != null) {
			for (Destiny2ItemObject.SocketEntry socket : object.sockets.socketEntries) {
				if (socket != null && socket.singleInitialItemHash != null) {
					Destiny2ItemObject perkObject = Destiny2ItemSystem.destinyItemObjects.getOrDefault(
							socket.singleInitialItemHash.intValue(), null);
					
					if (perkObject == null) {
						continue;
					}
					
					list.add(perkObject);
				}
			}
		}
		
		return list;
	}
}
