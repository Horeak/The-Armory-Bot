package Core.Commands.Destiny.DestinyDataBaseObjects.Destiny2;

import Core.Commands.Destiny.DestinyDataBaseObjects.BaseObjects.BasePerkObject;
import Core.Commands.Destiny.DestinyDataBaseObjects.BaseObjects.BaseStatObject;
import Core.Commands.Destiny.DestinyDataBaseObjects.BaseObjects.DestinyBaseItemObject;
import Core.Commands.Destiny.DestinyDataBaseObjects.BaseObjects.SocketObject;
import Core.Commands.Destiny.DestinyDataBaseObjects.Destiny2.Destiny2StatGroupObject.displayInterpolation;
import Core.Commands.Destiny.DestinySystem;
import Core.Commands.Destiny.Objects.CatalystObject;
import Core.Commands.Destiny.Objects.MasterworkObject;
import Core.Objects.Annotation.Fields.JsonExclude;
import net.dv8tion.jda.api.entities.MessageEmbed;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.math3.analysis.interpolation.LinearInterpolator;
import org.apache.commons.math3.analysis.polynomials.PolynomialSplineFunction;

import java.util.*;

import static Core.Commands.Destiny.DestinyItemCommand.DEL_NAMES;

public class Destiny2ItemObject extends DestinyBaseItemObject
{
	public Destiny2ItemObject() { }
	
	public DisplayProperties displayProperties;
	public String displaySource;
	
	@JsonExclude
	public boolean curated = false;
	
	@JsonExclude
	public boolean preset = false;
	
	public int classType;
	public int[] damageTypes;
	public int defaultDamageType;
	public Long defaultDamageTypeHash;
	public Long[] damageTypeHashes;
	public Long[] itemCategoryHashes;
	public Long collectibleHash;
	public boolean equippable;
	public int itemType;
	public int itemSubType;
	public String damageType;
	public String itemTypeAndTierDisplayName;
	public String itemTypeDisplayName;
	public String screenshot;
	
	public String iconWatermark;
	public String iconWatermarkShelved;
	
	public int itemVersion = -1;
	public boolean nonTransferrable;
	
	@JsonExclude
	public CatalystObject catalyst;
	
	@JsonExclude
	public HashMap<String, String> infoValues = new HashMap<>();
	
	@JsonExclude
	public HashMap<String, String> seasonInfo = new HashMap<>();
	
	@JsonExclude
	public HashMap<String, String> powerInfo = new HashMap<>();
	
	@JsonExclude
	public HashMap<Integer, ArrayList<SocketObject>> perkMap = new HashMap<>();
	
	@JsonExclude
	public ArrayList<MasterworkObject> masterworkObjects = new ArrayList<>();
	
	public Quality quality;
	
	
	public void finalizeObject()
	{
		String dlc = getDLCName();
		String season = getSeasonName();
		
		boolean multipleVersions = itemVersion == -1 && quality != null && quality.versions != null && quality.versions.length > 1;
		
		//For some reason items that dont have a limit just has a very very high number so this is to limit those
		int powerLimit = 10000;
		
		if(dlc != null){
			this.dlc = dlc;
			if(getItemTier() != 6) {
				if (multipleVersions) {
					int num = 0;
					StringJoiner joiner = new StringJoiner(", ");
					joiner.add("**" + dlc + "**");
					
					for (versionObject object : quality.versions) {
						Destiny2PowerCapObject val = Destiny2ItemSystem.destinyPowerCapObjects.getOrDefault(object.powerCapHash.intValue(), null);
						
						if (val != null) {
							if (Destiny2ItemSystem.destinySeasons.containsKey(val.index)) {
								Destiny2SeasonObject seasonObject = Destiny2ItemSystem.destinySeasons.get(val.index);
								
								if (seasonObject.DLCName != null && !seasonObject.DLCName.isBlank()) {
									if (num != 0) { //Cheaty way to fix bungie setting the first season to 7 or 8 even if they came before. It skips the first dlc because it is already added earlier
										joiner.add("**" + seasonObject.DLCName + "**");
									}
									num++;
								}
							}
						}
					}
					
					if (!joiner.toString().isBlank()) {
						seasonInfo.put(num > 1 ? "DLCs" : "DLC", joiner.toString());
					}
					
				} else {
					if (itemVersion == -1) {
						seasonInfo.put("DLC", "**" + dlc + "**");
					} else {
						Long hashId = quality.versions[itemVersion].powerCapHash;
						
						Destiny2PowerCapObject val = Destiny2ItemSystem.destinyPowerCapObjects.getOrDefault(hashId.intValue(), null);
						
						if (val != null) {
							if (val.index == 7) {
								seasonInfo.put("DLC", "**" + dlc + "**");
								
							} else {
								if (Destiny2ItemSystem.destinySeasons.containsKey(val.index)) {
									Destiny2SeasonObject seasonObject = Destiny2ItemSystem.destinySeasons.get(val.index);
									
									if (seasonObject.DLCName != null && !seasonObject.DLCName.isBlank()) {
										seasonInfo.put("DLC", "**" + seasonObject.DLCName + "**");
									}
								}
							}
						}
					}
				}
			}else{
				seasonInfo.put("DLC", "**" + dlc + "**");
				
			}
		}
		
		if(season != null){
			if(getItemTier() != 6) {
				if (multipleVersions) {
					int num = 0;
					StringJoiner joiner = new StringJoiner(", ");
					joiner.add("**" + season + "**");
					for (versionObject object : quality.versions) {
						Destiny2PowerCapObject val = Destiny2ItemSystem.destinyPowerCapObjects.getOrDefault(object.powerCapHash.intValue(), null);
						
						if (val != null) {
							if (Destiny2ItemSystem.destinySeasons.containsKey(val.index)) {
								Destiny2SeasonObject seasonObject = Destiny2ItemSystem.destinySeasons.get(val.index);
								
								if (seasonObject.seasonName != null && !seasonObject.seasonName.isBlank()) {
									if (num != 0) { //Cheaty way to fix bungie setting the first season to 7 or 8 even if they came before. It skips the first season because it is already added earlier
										joiner.add("**" + seasonObject.seasonName + "**");
									}
									num++;
								}
							}
						}
					}
					
					if (!joiner.toString().isBlank()) {
						seasonInfo.put(num > 1 ? "Seasons" : "Season", joiner.toString());
					}
				} else {
					if (itemVersion == -1) {
						seasonInfo.put("Season", "**" + season + "**");
					} else {
						Long hashId = quality.versions[itemVersion].powerCapHash;
						
						Destiny2PowerCapObject val = Destiny2ItemSystem.destinyPowerCapObjects.getOrDefault(hashId.intValue(), null);
						
						if (val != null) {
							if (val.index == 7) {
								seasonInfo.put("Season", "**" + season + "**");
								
							} else {
								if (Destiny2ItemSystem.destinySeasons.containsKey(val.index)) {
									Destiny2SeasonObject seasonObject = Destiny2ItemSystem.destinySeasons.get(val.index);
									
									if (seasonObject.seasonName != null && !seasonObject.seasonName.isBlank()) {
										seasonInfo.put("Season", "**" + seasonObject.seasonName + "**");
									}
								}
							}
						}
					}
				}
			}else{
				seasonInfo.put("Season", "**" + season + "**");
			}
		}
		
		if(getItemTier() != 6) {
			if (quality != null) {
				if (quality.versions != null && quality.versions.length > 0) {
					if(quality.versions.length > 1){
						if(itemVersion != -1){
							Long hashId = quality.versions[itemVersion].powerCapHash;
							
							Destiny2PowerCapObject val = Destiny2ItemSystem.destinyPowerCapObjects.getOrDefault(hashId.intValue(), null);
							
							if (val != null) {
								if (val.powerCap < powerLimit) {
									powerInfo.put("Power Cap", "**" + val.powerCap + "**");
								}
							}
						}else{
							StringJoiner joiner = new StringJoiner(", ");
							for(versionObject object : quality.versions){
								Destiny2PowerCapObject val = Destiny2ItemSystem.destinyPowerCapObjects.getOrDefault(object.powerCapHash.intValue(), null);
								
								if (val != null) {
									if (val.powerCap < powerLimit) {
										joiner.add("**" + val.powerCap + "**");
									}
								}
							}
							
							if(!joiner.toString().isBlank()) {
								powerInfo.put("Power Cap", joiner.toString());
							}
						}
					}else{
						Long hashId = quality.versions[0].powerCapHash;
						
						Destiny2PowerCapObject val = Destiny2ItemSystem.destinyPowerCapObjects.getOrDefault(hashId.intValue(), null);
						
						if (val != null) {
							if (val.powerCap < powerLimit) {
								powerInfo.put("Power Cap", "**" + val.powerCap + "**");
							}
						}
					}
				}
			}
		}
		
		if (sockets != null && sockets.socketEntries != null && Arrays.stream(sockets.socketEntries).noneMatch(
				(ob) -> ob.randomizedPlugSetHash != null
				        && Destiny2ItemSystem.destinyPlugSetObjects.getOrDefault(ob.randomizedPlugSetHash.intValue(), null) != null
						&& Destiny2ItemSystem.destinyPlugSetObjects.getOrDefault(ob.randomizedPlugSetHash.intValue(), null).reusablePlugItems.length > 0)) {
			
			preset = true;
		}
		
		if (stats != null && stats.stats != null && stats.stats.size() > 0) {
			ArrayList<Long> removeList = new ArrayList<>();
			
			stats.stats.keySet().removeIf(Objects::isNull);
			stats.stats.values().removeIf(Objects::isNull);
			
			stats.stats.entrySet().removeIf(
					(ent) -> ent.getValue() == null || ent.getValue().getName() == null || ent.getKey() == null);
			
			stats.stats.forEach((key, value) -> {
				if (Arrays.stream(DEL_NAMES).anyMatch((del) -> value.getName().equalsIgnoreCase(del))) {
					removeList.add(key);
				} else if (value.value == 0 && value.maximum == 0 && value.minimum == 0) {
					removeList.add(key);
				}
			});
			
			
			removeList.forEach((l) -> stats.stats.remove(l));
		}
		
		//Handle curated and non curated stats
		if (!preset) {
			//Correct the stats to account for random perks
			//TODO This may be using the wrong statGroup?
			for (Destiny2ItemObject perkObject : Destiny2ItemSystem.getSinglePerks(this)) {
				for (BaseStatObject stat : perkObject.getStats().values()) {
					BaseStatObject curStat = null;
					
					for (BaseStatObject stat1 : getStats().values()) {
						if (stat1.statHash.equals(stat.statHash)) {
							curStat = stat1;
							break;
						}
					}
					
					if (curStat != null) {
						if (curStat.getName() == null) continue;
						
						if (curStat.getName().equalsIgnoreCase("charge time") || curStat.getName().equalsIgnoreCase(
								"draw time")) {
							stat.value = stat.value * -1;
						}
						
						if (!curated) {
							curStat.value -= stat.value;
						} else {
							curStat.value += stat.value;
						}
					}
				}
			}
		}
		
		if (preset) {
			//Increase stats based on preset perks
			for (Destiny2ItemObject perkObject : Destiny2ItemSystem.getSinglePerks(this)) {
				
				for (InvestmentStat stat : perkObject.investmentStats) {
					InvestmentStat curStat = null;
					
					for (InvestmentStat stat1 : investmentStats) {
						if (stat1.statTypeHash.equals(stat.statTypeHash)) {
							curStat = stat1;
							break;
						}
					}
					
					if (curStat != null) {
						curStat.value += stat.value;
					}
				}
			}
		}
		
		if (curated || preset) {
			//Currated and preset shouldnt show max/min as it has predeterminted stats
			for (BaseStatObject stat : getStats().values()) {
				stat.maximum = 0;
				stat.minimum = 0;
			}
		}
		
		if (curated) {
			infoValues.put("Curated", "**This is a curated item.**");
			
			//Clearing values that are left over from clone of non curated item
			if (sockets != null) {
				if (sockets.socketEntries != null) {
					for (SocketEntry entry : sockets.socketEntries) {
						entry.reusablePlugItems = null;
						entry.randomizedPlugSetHash = null;
					}
				}
			}
		}
		
		String t = (itemType == 3 ? itemTypeAndTierDisplayName : itemTypeDisplayName);
		
		if (t == null || t.isEmpty()) {
			if (itemTypeAndTierDisplayName != null && !itemTypeAndTierDisplayName.isEmpty()) {
				t = itemTypeAndTierDisplayName;
			} else {
				t = itemTypeDisplayName;
			}
		}
		
		if (t != null && !t.isEmpty()) {
			infoValues.put("Item Type", "**" + t + "**");
		}
		
		
		//Class type if there is one
		if (classType != 3) {
			String className = getClassName();
			
			if (className != null) {
				infoValues.put("Class", "**" + className + "**");
			}
		}
		
		//Set damage type
		if (defaultDamageTypeHash != null) {
			Destiny2DamageTypeObject damageTypeObject = Destiny2ItemSystem.destinyDamageTypeObjects.getOrDefault(
					defaultDamageTypeHash.intValue(), null);
			
			if (damageTypeObject != null) {
				infoValues.put("Element", "**" + damageTypeObject.displayProperties.name + "** " + DestinySystem.getIcon(
						damageTypeObject.displayProperties.name));
			}
		}
		
		//Set ammo type/slot type
		if (equippingBlock != null) {
			String slot = equippingBlock.ammoType == 1 ? "**Primary** " + DestinySystem.PRIMARY_ICON : equippingBlock.ammoType == 2 ? "**Special** " + DestinySystem.SPECIAL_ICON : equippingBlock.ammoType == 3 ? "**Heavy** " + DestinySystem.HEAVY_ICON : "";
			
			if (slot != null && !slot.isEmpty()) {
				infoValues.put("Ammo type", slot);
			}
		}
		
		//Add perks and masterworks
		if (sockets != null) {
			if (sockets.socketEntries != null) {
				int i = 0;
				
				entries:
				for (SocketEntry entry : sockets.socketEntries) {
					i++;
					
					if (curated) {
						entry.randomizedPlugSetHash = null;
						entry.reusablePlugItems = null;
					}
					
					//Masterwork
					if (entry.defaultVisible) {
						if (!curated && !preset && entry.reusablePlugItems != null && entry.reusablePlugItems.length > 0) {
							for (PlugItem item : entry.reusablePlugItems) {
								getMasterworkFromObject(
										Destiny2ItemSystem.destinyItemObjects.getOrDefault(item.plugItemHash.intValue(),
										                                                   null));
							}
						} else {
							if (entry.singleInitialItemHash != null) {
								getMasterworkFromObject(Destiny2ItemSystem.destinyItemObjects.getOrDefault(
										entry.singleInitialItemHash.intValue(), null));
							}
						}
					}
					
					masterworkObjects.removeIf((m) -> {
						for (BaseStatObject object1 : getStats().values()) {
							if (object1 != null && object1.getName() != null) {
								if (object1.getName().equalsIgnoreCase(m.statName)) {
									return false;
								}
							}
						}
						
						return true;
					});
					
					masterworkObjects.removeIf((m) -> m.statName.equalsIgnoreCase(
							"impact") && itemSubType != 18); //18 is subtype for sword
					
					masterworkObjects.removeIf((m) -> m.statName.equalsIgnoreCase(
							"accuracy") && itemSubType != 31); //31 is subtype for bow
					
					
					//TODO Catalyst stats are broken atm
					//Catalyst
					if (entry.plugSources == 3) {
						String catalystName = "";
						String catalystDesc = "";
						String unlock = "";
						StringJoiner statsJoiner = new StringJoiner("\n");
						
						boolean isMasterwork = false;
						
						//For some reason the itemobject only links to its DUMMY catalyst therefor not possible to accurately lookup related perks. Why bungie....
						if (entry.reusablePlugItems != null && entry.reusablePlugItems.length > 0) {
							for (PlugItem item : entry.reusablePlugItems) {
								if (item != null) {
									Destiny2ItemObject object = Destiny2ItemSystem.destinyItemObjects.getOrDefault(
											item.plugItemHash.intValue(), null);
									
									if (object != null) {
										if (!isMasterwork) {
											if (object.plug != null) {
												if (object.plug.plugStyle == 1) {
													isMasterwork = true;
													catalystName = object.displayProperties.name;
													catalystDesc = object.displayProperties.description;
													
													if (!catalystName.toLowerCase().contains("catalyst")) {
														continue entries;
													}
												}
											}
										} else {
											unlock = object.displayProperties.description;
										}
										
										if (object.getStats().size() > 0) {
											for (InvestmentStat stat : object.investmentStats) {
												
												//TODO !isConditionallyActive
												if (stat.statTypeHash != null) {
													Destiny2StatObject object1 = Destiny2ItemSystem.destinyStatObjects.getOrDefault(stat.statTypeHash.intValue(), null);
													
													if(object1 != null) {
														int val = stat.value;
														String name = object1.displayProperties.name;
														
														if (name.equalsIgnoreCase("charge time") || name.equalsIgnoreCase("draw time")) {
															val *= -1;
														}
														
														statsJoiner.add("**" + name + "**: " + (val > 0 ? "+" : "-") + val);
													}
												}
											}
										}
									}
								}
							}
						}
						//2732814938
						if (!catalystDesc.isEmpty() && !catalystName.isEmpty()) {
							if (catalystName.toLowerCase().contains("catalyst")) {
								catalyst = new CatalystObject(catalystName, catalystDesc, unlock,
								                              statsJoiner.toString());
							}
						}
					}
					
					
					if (entry.preventInitializationOnVendorPurchase) {
						continue;
					}
					
					if (entry.plugSources == 3 || entry.plugSources == 7 || entry.plugSources == 13) {
						continue;
					}
					
					if(entry.singleInitialItemHash == 2285418970L || entry.singleInitialItemHash == 4248210736L){
						continue;
					}
					
					if (!preset && entry.randomizedPlugSetHash != null) {
						Destiny2PlugSetObject setObject = Destiny2ItemSystem.destinyPlugSetObjects.getOrDefault(entry.randomizedPlugSetHash.intValue(), null);
						
						if (setObject != null){
							StringJoiner joiner = new StringJoiner(", ");
							int added = 0;
							boolean addedSocket = false;
							
							for (RandomizedPlugItem socket : setObject.reusablePlugItems) {
								Destiny2ItemObject socketItem = Destiny2ItemSystem.destinyItemObjects.getOrDefault(
										socket.plugItemHash.intValue(), null);
								
								if (socketItem != null) {
									if (joiner.length() + socketItem.getName().length() >= (MessageEmbed.VALUE_MAX_LENGTH - 20)) {
										joiner.add("and " + (entry.reusablePlugItems.length - added) + " more.");
										
										if (!perkMap.containsKey(i)) {
											perkMap.put(i, new ArrayList<>());
										}
										
										perkMap.get(i).add(
												new SocketObject("Random Perk", joiner.toString(), i, socket.plugItemHash));
										addedSocket = true;
										break;
									} else {
										if(!joiner.toString().contains("`" + socketItem.getName() + "`")) {
											joiner.add("`" + socketItem.getName() + "`");
											added++;
										}
									}
								}
							}
							
							if (!addedSocket) {
								if (!perkMap.containsKey(i)) {
									perkMap.put(i, new ArrayList<>());
								}
								
								perkMap.get(i).add(new SocketObject("Random Perk", joiner.toString(), i));
							}
						}
					} else if (entry.reusablePlugItems != null && entry.reusablePlugItems.length > 0) {
						for (PlugItem item : entry.reusablePlugItems) {
							Destiny2ItemObject socketItem = Destiny2ItemSystem.destinyItemObjects.getOrDefault(
									item.plugItemHash.intValue(), null);
							
							if (socketItem != null) {
								if (!perkMap.containsKey(i)) {
									perkMap.put(i, new ArrayList<>());
								}
								
								perkMap.get(i).add(new SocketObject(socketItem.getName(),
								                                    socketItem.getDescription(), i,
								                                    item.plugItemHash));
							}
						}
						
					} else if (entry.singleInitialItemHash != null) {
						Destiny2ItemObject socketItem = Destiny2ItemSystem.destinyItemObjects.getOrDefault(
								entry.singleInitialItemHash.intValue(), null);
						
						if (socketItem != null) {
							if (!perkMap.containsKey(i)) {
								perkMap.put(i, new ArrayList<>());
							}
							
							perkMap.get(i).add(new SocketObject(socketItem.getName(),
							                                    socketItem.getDescription(), i,
							                                    entry.singleInitialItemHash));
						}
					}
				}
			}
		}
		
		if (collectibleHash != null) {
			Destiny2CollectibleObject collectibleObject = Destiny2ItemSystem.destinyCollectibleObjects.getOrDefault(
					collectibleHash.intValue(), null);
			
			if (collectibleObject != null) {
				if (collectibleObject.sourceString != null && !collectibleObject.sourceString.isEmpty()) {
					source = collectibleObject.sourceString;
				}
			}
		}
		
		
		//Add any perks the item may have to the end of the perk map, this will likely only affect catalysts
		if(perks != null && perks.length > 0){
			for(Perk perk : perks){
				Destiny2PerkObject perkObject = Destiny2ItemSystem.destinyPerkObjects.getOrDefault(perk.perkHash.intValue(), null);
				
				if(perkObject != null){
					if(perkObject.displayProperties != null && perkObject.displayProperties.name != null){
						int perkMapSize = perkMap.size() + 1;
						
						perkMap.put(perkMapSize, new ArrayList<>());
						perkMap.get(perkMapSize).add(new SocketObject(perkObject.displayProperties.name, perkObject.displayProperties.description, perkMapSize));
					}
				}
			}
		}
	}
	
	protected void getMasterworkFromObject( Destiny2ItemObject object)
	{
		if (object != null) {
			if (!object.displayProperties.name.replace(" ", "").equalsIgnoreCase("masterwork")) {
				return;
			}
			
			Destiny2StatObject statOb = null;
			InvestmentStat stat = null;
			
			if (object.investmentStats != null && object.investmentStats.length > 0) {
				for (InvestmentStat stat1 : object.investmentStats) {
					statOb = Destiny2ItemSystem.destinyStatObjects.getOrDefault(stat1.statTypeHash.intValue(), null);
					
					if (statOb != null) {
						stat = stat1;
						break;
					}
				}
			}
			
			if (statOb != null && stat != null) {
				//TODO Maybe add support for tier 1-9 masterwork later down the line
				MasterworkObject masterworkObject = new MasterworkObject(object.displayProperties.name,
				                                                         object.displayProperties.description,
				                                                         object.displayProperties.icon,
				                                                         stat.statTypeHash, stat.value, false);
				masterworkObject.statName = statOb.displayProperties.name;
				masterworkObject.statDescription = statOb.displayProperties.description;
				masterworkObjects.add(masterworkObject);
			}
		}
	}
	
	@Override
	public String getName()
	{
		switch (itemType){
			case 19:
				if(displayProperties.name.equalsIgnoreCase("Masterwork")){
					if(investmentStats != null && investmentStats.length > 0){
						for(InvestmentStat stat : investmentStats){
							Destiny2StatObject object = Destiny2ItemSystem.destinyStatObjects.getOrDefault(stat.statTypeHash.intValue(), null);
							
							if(object != null && object.displayProperties != null){
								return object.displayProperties.name + " " + displayProperties.name;
							}
						}
					}
				}
				
			default:
				return displayProperties.name;
		}
	}
	
	@Override
	public String getDescription()
	{
		switch (itemType){
			case 19:
				{
					//4104513227 is the ID for armor mods
					if(itemCategoryHashes != null && Arrays.asList(itemCategoryHashes).contains(4104513227L)) {
						return displayProperties.description + " (" + itemTypeDisplayName + ")";
					}
					
					if(displayProperties.description == null || displayProperties.description.isBlank()){
						if(perks != null && perks.length > 0){
							for(Perk perk : perks){
								Destiny2PerkObject object = Destiny2ItemSystem.destinyPerkObjects.getOrDefault(perk.perkHash.intValue(), null);
								
								if(object != null && object.displayProperties != null && !object.displayProperties.description.isBlank()){
									return object.displayProperties.description;
								}
							}
						}
					}
				}
			
			default:
				return displayProperties.description;
		}
	}
	
	@Override
	public String getIcon()
	{
		return displayProperties.icon;
	}
	
	@Override
	public String getImage()
	{
		return screenshot;
	}
	
	@Override
	public int getItemTier()
	{
		return inventory != null ? inventory.tierType : 0;
	}
	
	@Override
	public String getItemTierAndType()
	{
		return itemTypeAndTierDisplayName;
	}
	
	@Override
	public boolean isEquippable()
	{
		return equippable;
	}
	
	public String getClassName(){
		if (classType != 3) {
			String className = null;
			
			for (Destiny2ClassObject clas : Destiny2ItemSystem.destinyClassObjects.values()) {
				if (clas.classType == classType) {
					className = clas.displayProperties.name;
					break;
				}
			}
			
			return className;
		}
		
		return null;
	}
	
	
	public int getSeasonNumber(){
		return Destiny2ItemSystem.destinySeasonNumbers.getOrDefault(hash, -1);
	}
	
	public String getSeasonName(){
		if(getSeasonNumber() != -1){
			Destiny2SeasonObject object = Destiny2ItemSystem.destinySeasons.getOrDefault(getSeasonNumber(), null);
			
			if(object != null){
				if(object.seasonName != null && !object.seasonName.isEmpty()){
					return object.seasonName;
				}
			}
		}
		
		return null;
	}
	
	public String getDLCName(){
		if(getSeasonNumber() != -1){
			Destiny2SeasonObject object = Destiny2ItemSystem.destinySeasons.getOrDefault(getSeasonNumber(), null);
			
			if(object != null){
				if(object.DLCName != null && !object.DLCName.isEmpty()){
					return object.DLCName;
				}
			}
		}
		
		return null;
	}
	
	
	
	public HashMap<Long, BaseStatObject> getStats(Destiny2ItemObject baseObject)
	{
		
		//TODO If baseObject does not equal to this, subtract the investment stat from this item onto baseObject and set the difference as the stat value for this object
		
		if (stats != null && stats.statGroupHash != null) {
			Destiny2StatGroupObject statGroup = Destiny2ItemSystem.destinyStatGroupObjects.getOrDefault(
					stats.statGroupHash.intValue(), null);
			
			if (statGroup != null && statGroup.scaledStats != null) {
				//TODO If investment stat is used for an item it will need to be calculated using the stat group
				if (investmentStats != null && investmentStats.length > 0) {
					HashMap<Long, BaseStatObject> map = new HashMap<>();
					
					for (Destiny2StatGroupObject.scaledStat scaledStat : statGroup.scaledStats) {
						for (InvestmentStat stat : investmentStats) {
							if (scaledStat.statHash.equals(stat.statTypeHash)) {
								if (scaledStat.displayInterpolation != null && scaledStat.displayInterpolation.length >= 2) {
									StatEntryObject object = new StatEntryObject();
									
									displayInterpolation start = scaledStat.displayInterpolation[0];
									displayInterpolation end = scaledStat.displayInterpolation[scaledStat.displayInterpolation.length - 1];
									
									object.minimum = start.weight;
									object.maximum = end.weight;
									
									object.displayAsNumeric = scaledStat.displayAsNumeric;
									
									ArrayList<Double> xPos = new ArrayList<>();
									ArrayList<Double> yPos = new ArrayList<>();
									
									for (displayInterpolation dis : scaledStat.displayInterpolation) {
										xPos.add((double)dis.value);
										yPos.add((double)dis.weight);
									}
									
									//TODO Replace this with something more reliable and faster
									LinearInterpolator li = new LinearInterpolator();
									PolynomialSplineFunction function = li.interpolate(
											ArrayUtils.toPrimitive(xPos.toArray(new Double[0])),
											ArrayUtils.toPrimitive(yPos.toArray(new Double[0])));
									
									
									//TODO This is a cheaty way to do it. Look more at expanding the model if it goes out of bounds
									if (stat.value < start.value) {
										stat.value = start.value;
									}
									
									if (stat.value > end.value) {
										stat.value = end.value;
									}
									
									object.value = (int)Math.round(function.value(stat.value));
									object.statHash = stat.statTypeHash;
									object.oldValue = stat.oldValue;
									
									if (stats != null && stats.stats != null && stats.stats.size() > 0) {
										for(StatEntryObject statEntry : stats.stats.values()){
											if(stat.statTypeHash.equals(statEntry.statHash)){
												if(statEntry.minimum != 0) object.minimum = statEntry.minimum;
												if(statEntry.maximum != 0) object.maximum = statEntry.maximum;
												break;
											}
										}
									}
									
									map.put(stat.statTypeHash, object);
									break;
								}
							}
						}
						
						if(!map.containsKey(scaledStat.statHash)){
							StatEntryObject statObject = new StatEntryObject();
							statObject.statHash = scaledStat.statHash;
							statObject.value = -1;
							
							displayInterpolation start = scaledStat.displayInterpolation[0];
							displayInterpolation end = scaledStat.displayInterpolation[scaledStat.displayInterpolation.length - 1];

							statObject.minimum = start.weight;
							statObject.maximum = end.weight;

							map.put(scaledStat.statHash, statObject);
						}
					}
					
					if(stats != null){
						for(StatEntryObject ob : stats.stats.values()){
							if(!map.containsKey(ob.statHash)){
								map.put(ob.statHash, ob);
							}
						}
					}
					
					return map;
				}
			}
		}else if(investmentStats != null){
			if (investmentStats.length > 0) {
				HashMap<Long, BaseStatObject> map = new HashMap<>();
				
				for (InvestmentStat stat : investmentStats) {
					StatEntryObject object = new StatEntryObject();
					object.value = stat.value;
					object.statHash = stat.statTypeHash;
					object.oldValue = stat.oldValue;
					
					if (stats != null && stats.stats != null && stats.stats.size() > 0) {
						for(StatEntryObject statEntry : stats.stats.values()){
							if(statEntry.value == 0 && statEntry.maximum == 0 && statEntry.minimum == 0) continue;
							
							if(stat.statTypeHash.equals(statEntry.statHash)){
								if(statEntry.minimum != 0) object.minimum = statEntry.minimum;
								if(statEntry.maximum != 0) object.maximum = statEntry.maximum;
								
								break;
							}
						}
					}
					
					map.put(stat.statTypeHash, object);
				}
				
				return map;
			}
		}
		
		if (stats != null && stats.stats != null && stats.stats.size() > 0) {
			return new HashMap<>(stats.stats);
		}
		
		return new HashMap<>();
	}
	
	
	@Override
	public HashMap<Long, BaseStatObject> getStats()
	{
		return getStats(this);
	}
	
	
	@Override
	public HashMap<String, String> getInfo()
	{
		return infoValues;
	}
	
	
	@Override
	public HashMap<Integer, ArrayList<SocketObject>> getPerks()
	{
		return perkMap;
	}
	
	
	public static class Quality{
		public versionObject[] versions;
	}
	
	public static class versionObject{
		public Long powerCapHash;
	}
	
	public Perk[] perks;
	
	public static class Perk extends BasePerkObject
	{
		public Long perkHash;
		public int perkVisibility;
		public String requirementDisplayString;
		
		@Override
		public Long hash()
		{
			return perkHash;
		}
		
		
		public String getName()
		{
			Destiny2PerkObject object = Destiny2ItemSystem.destinyPerkObjects.getOrDefault(perkHash.intValue(), null);
			
			if (object != null) {
				return object.displayProperties.name;
			}
			
			return null;
		}
		
		
		public String getDescription()
		{
			Destiny2PerkObject object = Destiny2ItemSystem.destinyPerkObjects.getOrDefault(perkHash.intValue(), null);
			
			if (object != null) {
				return object.displayProperties.description;
			}
			
			return null;
		}
		
		
		public String getIcon()
		{
			Destiny2PerkObject object = Destiny2ItemSystem.destinyPerkObjects.getOrDefault(perkHash.intValue(), null);
			
			if (object != null) {
				return object.displayProperties.icon;
			}
			
			return null;
		}
	}
	
	public Plug plug;
	
	public static class Plug
	{
		public String plugCategoryIdentifier;
		public Long plugCategoryHash;
		public String uiPlugLabel;
		public int plugStyle;
	}
	
	public EquippingBlock equippingBlock;
	
	public static class EquippingBlock
	{
		public int ammoType;
		public int attributes;
		public String[] displayStrings;
		public Long equipmentSlotHash;
	}
	
	public InventoryDatabaseObject inventory;
	
	public static class InventoryDatabaseObject
	{
		public InventoryDatabaseObject() {}
		
		public String tierTypeName;
		public Long tierTypeHash;
		public int tierType;
		public int maxStackSize;
		public boolean isInstanceItem;
		public Long bucketTypeHash;
	}
	
	public StatsEntryObject stats;
	
	public static class StatsEntryObject
	{
		public StatsEntryObject() {}
		
		public boolean hasDisplayableStats;
		public Long primaryBaseStatHash;
		public Long statGroupHash;
		public HashMap<Long, StatEntryObject> stats;
	}
	
	public static class StatEntryObject extends BaseStatObject
	{
		public StatEntryObject() {}
		
		public String nameOveride;
		
		public String getName()
		{
			if(nameOveride != null){
				return nameOveride;
			}
			
			if(statHash == null) return null;
			
			Destiny2StatObject object1 = Destiny2ItemSystem.destinyStatObjects.getOrDefault(statHash.intValue(), null);
			
			if (object1 != null) {
				return object1.displayProperties.name;
			}
			
			return null;
		}
		
		
		public String getDescription()
		{
			if(statHash == null) return null;
			
			Destiny2StatObject object1 = Destiny2ItemSystem.destinyStatObjects.getOrDefault(statHash.intValue(), null);
			
			if (object1 != null) {
				return object1.displayProperties.description;
			}
			
			return null;
		}
		
		@Override
		public String toString()
		{
			return "StatEntryObject{" + "minimum=" + minimum + ", maximum=" + maximum + ", statHash=" + statHash + ", value=" + value + ", name=" + getName() + '}';
		}
	}
	
	public InvestmentStat[] investmentStats;
	
	public static class InvestmentStat
	{
		public InvestmentStat() { }
		
		public boolean isConditionallyActive;
		public Long statTypeHash;
		public int value;
		public int oldValue = -1;
	}
	
	public SocketsEntry sockets;
	
	public static class SocketsEntry
	{
		public SocketsEntry() {}
		
		public IntrinsicSocketEntry[] intrinsicSockets;
		public SocketCategoryEntry[] socketCategories;
		public SocketEntry[] socketEntries;
	}
	
	public static class SocketCategoryEntry
	{
		public SocketCategoryEntry() { }
		
		public Long socketCategoryHash;
		public int[] socketIndexes;
	}
	
	public static class IntrinsicSocketEntry
	{
		public IntrinsicSocketEntry() { }
		
		public boolean defaultVisible;
		public Long plugItemHash;
		public Long socketTypeHash;
	}
	
	public static class SocketEntry
	{
		public SocketEntry() {}
		
		public boolean defaultVisible;
		public boolean hidePerksInItemTooltip;
		public boolean preventInitializationOnVendorPurchase;
		public boolean preventInitializationWhenVersioning;
		public Integer plugSources;
		public Long singleInitialItemHash;
		public Long socketTypeHash;
		
		public PlugItem[] reusablePlugItems;
		public Long randomizedPlugSetHash;
	}
	
	public static class PlugItem
	{
		public PlugItem() { }
		
		public Long plugItemHash;
	}
	
	public static class RandomizedPlugItem extends PlugItem
	{
		public RandomizedPlugItem() { }
		
		public int alternateWeight;
		public int weight;
	}
	
	@Override
	public boolean equals(Object o)
	{
		if (this == o) {
			return true;
		}
		if (!(o instanceof Destiny2ItemObject)) {
			return false;
		}
		Destiny2ItemObject that = (Destiny2ItemObject)o;
		return classType == that.classType && defaultDamageType == that.defaultDamageType && isEquippable() == that.isEquippable() && itemType == that.itemType && itemSubType == that.itemSubType && Objects.equals(
				displayProperties, that.displayProperties) && Objects.equals(displaySource,
		                                                                     that.displaySource) && Arrays.equals(
				damageTypes, that.damageTypes) && Objects.equals(defaultDamageTypeHash,
		                                                         that.defaultDamageTypeHash) && Arrays.equals(
				damageTypeHashes, that.damageTypeHashes) && Arrays.equals(itemCategoryHashes,
		                                                                  that.itemCategoryHashes) && Objects.equals(
				collectibleHash, that.collectibleHash) && Objects.equals(damageType, that.damageType) && Objects.equals(
				itemTypeAndTierDisplayName, that.itemTypeAndTierDisplayName) && Objects.equals(itemTypeDisplayName,
		                                                                                       that.itemTypeDisplayName) && Objects.equals(
				screenshot, that.screenshot) && Objects.equals(catalyst, that.catalyst) && Objects.equals(infoValues,
		                                                                                                  that.infoValues) && Objects.equals(
				perkMap, that.perkMap) && Objects.equals(masterworkObjects, that.masterworkObjects) && Objects.equals(
				plug, that.plug) && Objects.equals(equippingBlock, that.equippingBlock) && Objects.equals(inventory,
		                                                                                                  that.inventory) && Objects.equals(
				getStats(), that.getStats()) && Arrays.equals(investmentStats, that.investmentStats) && Objects.equals(
				sockets, that.sockets);
	}
}

