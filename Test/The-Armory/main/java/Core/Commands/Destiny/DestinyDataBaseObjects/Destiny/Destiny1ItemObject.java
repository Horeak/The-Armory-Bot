package Core.Commands.Destiny.DestinyDataBaseObjects.Destiny;

import Core.Commands.Destiny.DestinyDataBaseObjects.BaseObjects.BaseStatObject;
import Core.Commands.Destiny.DestinyDataBaseObjects.BaseObjects.DestinyBaseItemObject;
import Core.Commands.Destiny.DestinyDataBaseObjects.BaseObjects.SocketObject;
import Core.Commands.Destiny.DestinySystem;
import net.dv8tion.jda.api.entities.MessageEmbed;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.StringJoiner;

public class Destiny1ItemObject extends DestinyBaseItemObject
{
	public HashMap<String, String> infoValues = new HashMap<>();
	public HashMap<Integer, ArrayList<SocketObject>> perkMap = new HashMap<>();
	public boolean hasIcon;
	public String icon;
	public String secondaryIcon;
	public String itemDescription;
	public String itemName;
	public String itemTypeName;
	public int itemType;
	public int itemSubType;
	public int[] damageTypes;
	public Long[] sourceHashes;
	public Long itemHash;
	public Long talentGridHash;
	public int classType;
	public int tierType;
	public String tierTypeName;
	public boolean equippable;
	public Long[] itemCategoryHashes;
	public HashMap<Long, StatEntryObject> stats;
	
	public Destiny1ItemObject() { }
	
	@Override
	public String getName()
	{
		return itemName;
	}
	
	@Override
	public String getDescription()
	{
		return itemDescription;
	}
	
	@Override
	public String getIcon()
	{
		return icon;
	}
	
	
	@Override
	public String getImage()
	{
		return !secondaryIcon.endsWith("missing_icon.png") ? secondaryIcon : null;
	}
	
	@Override
	public int getItemTier()
	{
		return tierType;
	}
	
	
	@Override
	public String getItemTierAndType()
	{
		return tierTypeName + " " + itemTypeName;
	}
	
	@Override
	public boolean isEquippable()
	{
		return equippable;
	}
	
	
	@Override
	public HashMap<Long, BaseStatObject> getStats()
	{
		return new HashMap<>(stats);
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
	
	@Override
	public void finalizeObject()
	{
		infoValues.put("Type", "**" + tierTypeName + " " + itemTypeName + "**");
		
		if (damageTypes != null) {
			StringJoiner damageTypeJoiner = new StringJoiner(", ");
			
			for (int dmgType : damageTypes) {
				for (Destiny1DamageTypeObject object : Destiny1ItemSystem.destinyDamageTypeObjects.values()) {
					if (dmgType == object.enumValue) {
						String name = object.damageTypeName;
						String icon = DestinySystem.getIcon(name);
						damageTypeJoiner.add("**" + name + "**" + (icon != null && !icon.isEmpty() ? icon : ""));
					}
				}
			}
			
			infoValues.put("Element", damageTypeJoiner.toString());
		}
		
		if (sourceHashes != null) {
			for (Long source : sourceHashes) {
				Destiny1RewardSourceObject rewardSourceObject = Destiny1ItemSystem.destinyRewardSourceObjects.getOrDefault(
						source.intValue(), null);
				
				if (rewardSourceObject != null) {
					if (rewardSourceObject.description.contains("This item requires")) {
						dlc = rewardSourceObject.sourceName;
					} else {
						if (rewardSourceObject.description != null && !rewardSourceObject.description.isEmpty()) {
							this.source = rewardSourceObject.description;
						}
					}
				}
			}
		}
		
		if (talentGridHash != null) {
			Destiny1TalentGridObject talentGrid = Destiny1ItemSystem.destinyTalentGridObjects.getOrDefault(
					talentGridHash.intValue(), null);
			
			if (talentGrid != null) {
				if (talentGrid.nodes != null) {
					if (talentGrid.nodes != null && talentGrid.nodes.length > 0) {
						for (Destiny1TalentGridObject.Node nd : talentGrid.nodes) {
							if (nd.isRandom) {
								StringJoiner joiner = new StringJoiner(", ");
								int added = 0;
								boolean addedSocket = false;
								
								for (Destiny1TalentGridObject.Step step : nd.steps) {
									if (step != null) {
										if (step.affectsLevel || step.affectsQuality) continue;
										
										if (step.nodeStepName == null || step.nodeStepDescription == null) continue;
										if (step.statHashes != null && step.statHashes.length > 0 || step.perkHashes != null && step.perkHashes.length > 0) {
											
											if (step.nodeStepName != null) {
												if (joiner.length() + step.nodeStepName.length() >= (MessageEmbed.VALUE_MAX_LENGTH - 20)) {
													joiner.add("and " + (nd.steps.length - added) + " more.");
													
													if (!perkMap.containsKey(nd.column)) {
														perkMap.put(nd.column, new ArrayList<>());
													}
													
													perkMap.get(nd.column).add(
															new SocketObject("Random Perk", joiner.toString(),
															                 nd.column, step.nodeStepHash));
													addedSocket = true;
													break;
												} else {
													joiner.add("`" + step.nodeStepName + "`");
													added++;
												}
											}
										}
									}
								}
								
								if (!addedSocket) {
									if (!perkMap.containsKey(nd.column)) {
										perkMap.put(nd.column, new ArrayList<>());
									}
									
									perkMap.get(nd.column).add(
											new SocketObject("Random Perk", joiner.toString(), nd.column));
								}
								
							} else {
								for (Destiny1TalentGridObject.Step step : nd.steps) {
									if (step.affectsLevel || step.affectsQuality) continue;
									
									if (step.nodeStepName == null || step.nodeStepDescription == null) continue;
									
									if (step.statHashes != null && step.statHashes.length > 0 || step.perkHashes != null && step.perkHashes.length > 0) {
										if (!perkMap.containsKey(nd.column)) {
											perkMap.put(nd.column, new ArrayList<>());
										}
										
										perkMap.get(nd.column).add(
												new SocketObject(step.nodeStepName, step.nodeStepDescription, nd.column,
												                 step.nodeStepHash));
									}
								}
							}
						}
					}
				}
			}
		}
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
			
			Destiny1StatObject object1 = Destiny1ItemSystem.destinyStatObjects.getOrDefault(statHash.intValue(), null);
			
			if (object1 != null) {
				return object1.statName;
			}
			
			return null;
		}
		
		
		public String getDescription()
		{
			Destiny1StatObject object1 = Destiny1ItemSystem.destinyStatObjects.getOrDefault(statHash.intValue(), null);
			
			if (object1 != null) {
				return object1.statDescription;
			}
			
			return null;
		}
		
		
		@Override
		public String toString()
		{
			return "StatEntryObject{" + "minimum=" + minimum + ", maximum=" + maximum + ", statHash=" + statHash + ", value=" + value + '}';
		}
	}
}
