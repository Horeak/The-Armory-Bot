package Core.Commands.Destiny.DestinyDataBaseObjects.Destiny;

import Core.Main.Logging;
import Core.Util.JsonUtils;
import Core.Util.Utils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static Core.Commands.Destiny.DestinySystem.d1_connect;

public class Destiny1ItemSystem
{
	public static final ConcurrentHashMap<Integer, Destiny1ItemObject> destinyItemObjects = new ConcurrentHashMap<>();
	public static final ConcurrentHashMap<Integer, Destiny1StatObject> destinyStatObjects = new ConcurrentHashMap<>();
	public static final ConcurrentHashMap<Integer, Destiny1TalentGridObject> destinyTalentGridObjects = new ConcurrentHashMap<>();
	public static final ConcurrentHashMap<Integer, Destiny1RewardSourceObject> destinyRewardSourceObjects = new ConcurrentHashMap<>();
	public static final ConcurrentHashMap<Integer, Destiny1DamageTypeObject> destinyDamageTypeObjects = new ConcurrentHashMap<>();
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
		destinyTalentGridObjects.clear();
		destinyRewardSourceObjects.clear();
		destinyDamageTypeObjects.clear();
	}
	
	public static void init()
	{
		
		try {
			PreparedStatement statement = null;
			ResultSet rs = null;
			Connection con = d1_connect();
			
			try {
				statement = con.prepareStatement("select * from DestinyInventoryItemDefinition");
				rs = statement.executeQuery();
				
				while (rs.next()) {
					Long id = Long.parseLong(rs.getString(1));
					String json = rs.getString(2);
					
					if (json == null || json.isEmpty()) {
						continue;
					}
					
					Destiny1ItemObject object = JsonUtils.getGson_non_pretty().fromJson(json, Destiny1ItemObject.class);
					
					if (object != null) {
						destinyItemObjects.put(id.intValue(), object);
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
			Connection con = d1_connect();
			
			try {
				statement = con.prepareStatement("select * from DestinyStatDefinition");
				rs = statement.executeQuery();
				
				while (rs.next()) {
					Long id = Long.parseLong(rs.getString(1));
					String json = rs.getString(2);
					
					if (json == null || json.isEmpty()) {
						continue;
					}
					
					Destiny1StatObject object = JsonUtils.getGson_non_pretty().fromJson(json, Destiny1StatObject.class);
					
					if (object != null) {
						if (object.statName == null || object.statName.isEmpty() || object.statDescription == null || object.statDescription.isEmpty()) {
							continue;
						}
						
						destinyStatObjects.put(id.intValue(), object);
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
			Connection con = d1_connect();
			
			try {
				statement = con.prepareStatement("select * from DestinyRewardSourceDefinition");
				rs = statement.executeQuery();
				
				while (rs.next()) {
					Long id = Long.parseLong(rs.getString(1));
					String json = rs.getString(2);
					
					if (json == null || json.isEmpty()) {
						continue;
					}
					
					Destiny1RewardSourceObject object = JsonUtils.getGson_non_pretty().fromJson(json,
					                                                                            Destiny1RewardSourceObject.class);
					
					if (object != null) {
						if (object.sourceName == null || object.sourceName.isEmpty() || object.description == null || object.description.isEmpty()) {
							continue;
						}
						
						destinyRewardSourceObjects.put(id.intValue(), object);
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
			Connection con = d1_connect();
			
			try {
				statement = con.prepareStatement("select * from DestinyDamageTypeDefinition");
				rs = statement.executeQuery();
				
				while (rs.next()) {
					Long id = Long.parseLong(rs.getString(1));
					String json = rs.getString(2);
					
					if (json == null || json.isEmpty()) {
						continue;
					}
					
					Destiny1DamageTypeObject object = JsonUtils.getGson_non_pretty().fromJson(json,
					                                                                          Destiny1DamageTypeObject.class);
					
					if (object != null) {
						if (object.damageTypeName == null || object.damageTypeName.isEmpty() || object.description == null || object.description.isEmpty()) {
							continue;
						}
						
						destinyDamageTypeObjects.put(id.intValue(), object);
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
			Connection con = d1_connect();
			
			try {
				statement = con.prepareStatement("select * from DestinyTalentGridDefinition");
				rs = statement.executeQuery();
				
				while (rs.next()) {
					Long id = Long.parseLong(rs.getString(1));
					String json = rs.getString(2);
					
					if (json == null || json.isEmpty()) {
						continue;
					}
					
					Destiny1TalentGridObject object = JsonUtils.getGson_non_pretty().fromJson(json,
					                                                                          Destiny1TalentGridObject.class);
					
					if (object != null) {
						destinyTalentGridObjects.put(id.intValue(), object);
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
		
		for (Map.Entry<Integer, Destiny1ItemObject> ent : destinyItemObjects.entrySet()) {
			if (ent.getValue() != null) {
				ent.getValue().finalizeObject();
			} else {
				destinyItemObjects.remove(ent.getKey(), ent.getValue());
			}
		}
		
		System.out.println("Done initializing destiny 1 systems.");
	}
	
	public static Destiny1ItemObject getItemByName(
			String name)
	{
		ArrayList<Destiny1ItemObject> list = getItemsByName(name);
		return list.size() > 0 ? list.get(0) : null;
	}
	
	
	public static ArrayList<Destiny1ItemObject> getItemsByName(
			String name)
	{
		ArrayList<Destiny1ItemObject> inObjects = new ArrayList<>(destinyItemObjects.values());
		
		if (inObjects.size() <= 0) {
			return new ArrayList<>();
		}
		
		inObjects.removeIf((j) -> j.itemName == null || j.itemName.isEmpty());
		inObjects.sort((j1, j2) -> Integer.compare(j2.tierType, j1.tierType));
		
		inObjects.sort((j1, j2) -> {
			boolean eq1 = j1.equippable;
			boolean eq2 = j2.equippable;
			
			if (eq1 && eq2) {
				return Integer.compare(j2.itemType, j1.itemType);
			} else if (eq1) {
				return -1;
			} else if (eq2) {
				return 1;
			}
			
			return 0;
		});
		
		inObjects.sort((j1, j2) -> {
			int eq1 = j1.source != null ? 1 : 0;
			int eq2 = j2.source != null ? 1 : 0;
			
			return Integer.compare(eq2, eq1);
		});
		
		
		inObjects.sort((j1, j2) -> {
			double dif1 = Utils.compareStrings(name.replace(" ", ""), j1.itemName.replace(" ", ""));
			double dif2 = Utils.compareStrings(name.replace(" ", ""), j2.itemName.replace(" ", ""));
			
			return Double.compare(dif1, dif2);
		});
		
		for (Destiny1ItemObject ob1 : inObjects) {
			if (ob1 != null) {
				ob1.destinyVersion = 1;
			}
		}
		
		inObjects.removeIf((ob) -> ob.itemDescription == null || ob.itemDescription.isEmpty());
		inObjects.removeIf((ob) -> ob.itemName == null || ob.itemName.isEmpty());
		
		inObjects.removeIf((ob) -> {
			for (String s : IGNORED_TYPES) {
				if (ob.itemTypeName != null && !ob.itemTypeName.isEmpty()) {
					if (ob.itemTypeName.equalsIgnoreCase(s) || ob.itemTypeName.toLowerCase().contains(
							s.toLowerCase())) {
						return true;
					}
				}
			}
			return ob.itemTypeName == null || ob.itemTypeName.isEmpty();
		});
		
		//		inObjects.removeIf((ob) -> Utils.compareStrings(name, ob.displayProperties.name) >= MAX_DIFFERENCE);
		inObjects.removeIf((ob) -> !name.toLowerCase().replace(" ", "").contains(ob.itemName.toLowerCase().replace(" ", "")));
		inObjects.removeIf((ob) -> !ob.isEquippable());
		
		return inObjects;
	}
	
	public static Destiny1ItemObject getItemById(Long id)
	{
		return destinyItemObjects.getOrDefault(id, null);
	}
}
