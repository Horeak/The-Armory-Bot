package Core.Commands.BDO;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;

public class BDOItem
{
	public String name;
	public int id;
	
	public Long updateTime = System.currentTimeMillis();
	
	public BDOItemData data = new BDOItemData();
	
	public BDOItem(
			String name, int grade, int id, ArrayList<String> description, String icon, String category, LinkedHashMap<String, ArrayList<String>> itemEffects)
	{
		this.data.grade = grade;
		this.data.icon = icon;
		this.data.category = category;
		this.data.description = description;
		this.data.name = name;
		this.data.id = id;
		this.data.itemEffects = itemEffects;
		this.name = name;
		this.id = id;
	}
	
	public static class BDOItemData{
		public int grade;
		
		public String icon;
		public String name;
		public int id;
		
		public Long vendorPrice;
		public Long vendorSell;
		public Long repairCost;
		
		
		//TODO Make this a hashmap
		public HashMap<Integer, Long> EU_marketPrice;
		public HashMap<Integer, Long> NA_marketPrice;
		
		public String category;
		
		public String requiredClass;
		
		public BDOItemEnhancement[] enhancementLevels;
		
		public ArrayList<String> description;
		public ArrayList<String> specialInfo;
		
		public LinkedHashMap<String, ArrayList<String>> itemEffects;
	}
	
	public static class BDOItemEnhancement
	{
		public String requiredItem;
		public int requiredItemId;
		
		public int requiredItemAmount;
		public boolean usesFS;
		
		public int durabilityLoss;
		public int cron_cost;
		
		
		public float chance;
		
		public String damage;
		public String defense;
		public String accuracy;
		public String evasion;
		public String dreduction;
		
		public String hevasion;
		public String hdreduction;
		
		public LinkedHashMap<String, ArrayList<String>> itemEffects;
	}
	
}

