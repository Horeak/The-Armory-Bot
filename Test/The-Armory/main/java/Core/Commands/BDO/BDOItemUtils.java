package Core.Commands.BDO;

import Core.Commands.BDO.BDOItem.BDOItemData;
import Core.Commands.BDO.BDOItem.BDOItemEnhancement;
import Core.Main.Logging;
import Core.Main.Startup;
import Core.Objects.Annotation.Fields.DataObject;
import Core.Util.Utils;
import com.google.common.util.concurrent.RateLimiter;
import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BDOItemUtils
{
	static final RateLimiter rateLimiter = RateLimiter.create(1);
	
	@DataObject( file_path = "bdo/item_cache.json", name = "item_cache")
	public static ConcurrentHashMap<Integer, BDOItem> ITEM_CACHE = new ConcurrentHashMap<>();
	
	public static BDOItem getItem(int id)
	{
		if(ITEM_CACHE.containsKey(id)){
			BDOItem item = ITEM_CACHE.get(id);
			
			if(System.currentTimeMillis() - item.updateTime <= TimeUnit.DAYS.toMillis(7)){
				return item;
			}
		}
		
		rateLimiter.acquire();
		Element e = null;
		
		//String url = "https://bdocodex.com/tip.php?id=item--" + id;
		String url = BDOItemIndex.BASE_URL + "us/item/" + id;
		
		try {
			Document doc = Jsoup.parse(new URL(url), (int)TimeUnit.MILLISECONDS.convert(5, TimeUnit.SECONDS));
			
			if (doc == null) {
				return null;
			}
			
			e = doc.selectFirst(".item_info");
			
			if (e == null) {
				return null;
			}
			
			//TODO Marketplace price
			//TODO Have a check on interval check the bdocodex changelog for updates and do a slow update when a change is detected (One request a second or something)
			
		} catch (IOException ex) {
			if(ex instanceof SocketTimeoutException) {
				System.err.println("Timeout on id: " + id);
				return null;
			}
			Logging.exception(ex);
		}
		
		Element enhancementArray = e.selectFirst("#enchantment_array");
		
		e.select(".hide_me,.hide").remove();
		
		String name = cleanString(e.selectFirst(".item_title").text());
		String icon = BDOItemIndex.BASE_URL + e.selectFirst(".item_icon").attr("src");
		String category = cleanString(e.selectFirst(".category_text").text());
		
		ArrayList<StringBuilder> descriptionBuilder = new ArrayList<>();
		
		BDOItemEnhancement[] enhancementList = null;
		
		if (enhancementArray != null) {
			String json = enhancementArray.text();
			JSONObject jsonOb = (JSONObject)new JSONTokener(json).nextValue();
			
			int maxEnhance = jsonOb.getInt("max_enchant");
			ArrayList<BDOItemEnhancement> enhances = new ArrayList<>();
			
			
			for(int i = 0; i <= maxEnhance; i++){
				JSONObject enhanceLevel = jsonOb.getJSONObject(i + "");
				
				BDOItemEnhancement enhancement = new BDOItemEnhancement();
				
				Object t = enhanceLevel.get("enchant_probability");
				
				enhancement.chance = t instanceof Float ? (Float)t : Float.parseFloat(((String)t).replace(",", "."));
				
				enhancement.damage = enhanceLevel.has("damage") ? enhanceLevel.get("damage").toString() : null;
				enhancement.defense = enhanceLevel.has("defense") ? enhanceLevel.get("defense").toString() : null;
				enhancement.accuracy = enhanceLevel.has("accuracy") ? enhanceLevel.get("accuracy").toString() : null;
				enhancement.evasion = enhanceLevel.has("evasion") ? enhanceLevel.get("evasion").toString() : null;
				enhancement.dreduction = enhanceLevel.has("dreduction") ? enhanceLevel.get("dreduction").toString() : null;
				enhancement.hevasion = enhanceLevel.has("hevasion") ? enhanceLevel.get("hevasion").toString() : null;
				enhancement.hdreduction = enhanceLevel.has("hdreduction") ? enhanceLevel.get("hdreduction").toString() : null;
				
				enhancement.durabilityLoss = enhanceLevel.has("fail_dura_dec") ? enhanceLevel.getInt("fail_dura_dec") : 0;
				enhancement.cron_cost = enhanceLevel.has("cron_value") ? enhanceLevel.getInt("cron_value") : 0;
				
				enhancement.requiredItem = enhanceLevel.has("need_enchant_item_name") ? enhanceLevel.getString("need_enchant_item_name")  : null;
			
				
				if(enhanceLevel.has("need_enchant_item_id")){
					String val = enhanceLevel.getString("need_enchant_item_id");
					
					if(Utils.isInteger(val)){
						enhancement.requiredItemId = Integer.parseInt(val);
					}
				}
				
				enhancement.requiredItemAmount = enhanceLevel.has("enchant_item_counter") ? Math.max(1, enhanceLevel.getInt("enchant_item_counter")) : 1;
				
				String eDescription = enhanceLevel.getString("description");
				Document desc = Jsoup.parseBodyFragment(eDescription);
				enhancement.itemEffects = getItemEffectDescription(desc.selectFirst("body"));
				
				if(enhancement.requiredItem != null) {
					enhancement.usesFS = enhancement.requiredItem == null || (enhancement.requiredItemId != 5000 //Black gem
					                      && enhancement.requiredItemId != 4987); //Concentrated Magical Black Gem
				}
				enhances.add(enhancement);
			}
			
			if(enhances.size() > 0){
				enhancementList = enhances.toArray(new BDOItemEnhancement[0]);
			}
		}
		
		
		String tempRaritySearch = "item_grade_";
		String rarityString = e.selectFirst("[class*=" + tempRaritySearch + "]").attr("class");
		rarityString = rarityString.substring(rarityString.indexOf(tempRaritySearch) + tempRaritySearch.length()).trim();
		int grade = -1;
		
		if(Utils.isInteger(rarityString)){
			grade = Integer.parseInt(rarityString);
		}
		
		
		Element el = e.selectFirst("#ed_box #edescription");
		
		if(el == null){
			el = e.selectFirst("td #description");
			
			if(el != null){
				if(el.childNodes().size() > 0){
					Node nd = el.childNodes().get(0);
					
					String text = nd.toString();
					text = cleanString(text);
					
					if(!text.contains("Item Effect")) {
						descriptionBuilder.add(new StringBuilder(text));
						nd.remove();
					}
				}
			}
		}
		
		
		LinkedHashMap<String, ArrayList<String>> itemEffects = getItemEffectDescription(el);
		
		if(enhancementList != null){
			itemEffects.clear();
		}
		
		ArrayList<String> description = new ArrayList<>();
		
		if(descriptionBuilder.size() <= 0 || descriptionBuilder.get(0).toString().isBlank()) {
			Elements desc = e.select("tr td");
			
			if (desc != null) {
				String desOb = desc.toString();
				
				if (desOb.contains("Description:")){
					desOb = desOb.substring(desOb.indexOf("Description:"));
					
					if (desOb.contains(":")){
						desOb = desOb.substring(desOb.indexOf(":") + 1);
					}
					
					if (desOb.contains("<div")){
						desOb = desOb.substring(0, desOb.indexOf("<div"));
					}
					
					if (desOb.contains("tooltiphr")){
						desOb = desOb.substring(0, desOb.indexOf("<hr"));
					}
				}else{
					desOb = null;
				}
				
				if(desOb != null) {
					Document docE = Jsoup.parse(desOb);
					
					for (Node nd : docE.selectFirst("body").childNodes()) {
						String text = (nd instanceof Element ? ((Element)nd).text() : nd.toString()).trim();
						text = cleanString(text);
						
						if (!text.isBlank()) {
							if (descriptionBuilder.size() <= 0 || ((nd.previousSibling() != null && nd.previousSibling().toString().contains(
									"br")))) {
								descriptionBuilder.add(new StringBuilder(text));
							} else {
								descriptionBuilder.get(descriptionBuilder.size() - 1).append(" " + text);
							}
						}
					}
				}
			}
		}
		
		descriptionBuilder.forEach(s -> description.add(s.toString()));
		
		BDOItem item = new BDOItem(name, grade, id, description, icon, category, itemEffects);
		Element desc = e.select("tr td").last();
		Elements specialInfoText = desc.select("span[class*=light_blue_text]");
		
		ArrayList<String> specialInfo = new ArrayList<>();
		specialInfoText.forEach(s -> {
			String text = s.text();
			text = cleanString(text);
			
			if(!text.isBlank()){
				specialInfo.add(text);
			}
		});
		
		if(specialInfo.size() > 0){
			item.data.specialInfo = specialInfo;
		}
		
		if(enhancementList != null){
			item.data.enhancementLevels = enhancementList;
		}
		
		if(desc != null && desc.childNodes() != null && desc.childNodeSize() > 0) {
			desc.childNodes().forEach(s -> {
				String text = s.toString();
				text = cleanString(text);
				
				item.data.requiredClass = Utils.getContentBetweenCorresponding(text, "Exclusive:", "\n<br>");
				if(item.data.requiredClass != null){
					item.data.requiredClass = item.data.requiredClass.trim();
					
					if(item.data.requiredClass.isBlank()){
						item.data.requiredClass = null;
					}
				}
			});
		}
		
		Elements scripts = e.select("script");
		
		for(Element sc : scripts){
			String text = sc.html();
			String sp = Utils.getContentBetweenCorresponding(text, "real_item_prices=", ";");
			if(!sp.isBlank()){
				JSONObject jsonOb = (JSONObject)new JSONTokener(sp).nextValue();
				
				if(jsonOb.has("prices")){
					JSONObject prices = jsonOb.getJSONObject("prices");
					
					if(prices.has("EU")){
						item.data.EU_marketPrice = extractPriceList(prices.get("EU"));
					}
					
					if(prices.has("NA")){
						item.data.NA_marketPrice = extractPriceList(prices.get("NA"));
					}
				}
			}
		}
		
		String vendorCost = Utils.getContentBetweenCorresponding(e.html(), "Buy price: ", "<br>").replace(",", "").trim();
		String vendorSell = Utils.getContentBetweenCorresponding(e.html(), "Sell price: ", "<br>").replace(",", "").trim();
		String vendorRepairCost = Utils.getContentBetweenCorresponding(e.html(), "Repair price: ", "</td>").replace(",", "").trim();
		
		if(Utils.isLong(vendorRepairCost)){
			item.data.repairCost = Long.parseLong(vendorRepairCost);
		}
		
		if(Utils.isLong(vendorSell)){
			item.data.vendorSell = Long.parseLong(vendorSell);
		}
		
		if(Utils.isLong(vendorCost)){
			item.data.vendorPrice = Long.parseLong(vendorCost);
		}
		
//TODO Item drop location can be gotten with this
//		https://bdocodex.com/query.php?a=nodes&type=nodedrop&id=12059
		
		if(!Startup.debug) {
			if(item != null) {
				if (item.data != null && (item.data.EU_marketPrice != null && item.data.EU_marketPrice.size() > 0 ||  item.data.NA_marketPrice != null && item.data.NA_marketPrice.size() > 0)) {
					ITEM_CACHE.put(id, item);
				}
			}
		}
		
		return item;
	}
	
	public static String processItemName(BDOItemData itemData, String name, int enhance_level){
		return (getEnhancePrefix(itemData, getEnhanceIndex(itemData, enhance_level)) + name).trim();
	}
	
	public static int getEnhanceIndex(int max, int enhance_level){
		int index = enhance_level;
		
		if (max - 1 <= 5 && index > 15) {
			index -= 15;
		}
		
		index = Math.min(max, index);
		
		return index;
	}
	
	public static int getEnhanceIndex(BDOItemData itemData, int enhance_level){
		return getEnhanceIndex(itemData.enhancementLevels.length - 1, enhance_level);
	}
	
	public static BDOItemEnhancement getEnhanceObject(BDOItemData itemData, int enhance_level){
		int index = getEnhanceIndex(itemData, enhance_level);
		
		if(index >= 0 && index < itemData.enhancementLevels.length){
			return itemData.enhancementLevels[index];
		}
		
		return null;
	}
	
	private static HashMap<Integer, Long> extractPriceList(Object ar)
	{
		HashMap<Integer, Long> rMap = new HashMap<>();
		
		if(ar instanceof JSONArray) {
			int i = 0;
			for (Object ob : (JSONArray)ar) {
				String t = (ob instanceof JSONArray ? ((JSONArray)ob).get(0) : ((ArrayList)ob).get(0)) + "";
				
				if (Utils.isLong(t)) {
					rMap.put(i, Long.parseLong(t));
				}
				
				i++;
			}
		}else{
			Map<String, Object> map = ((JSONObject)ar).toMap();
			
			for(Entry<String, Object> ent : map.entrySet()){
				if(Utils.isInteger(ent.getKey())){
					int key = Integer.parseInt(ent.getKey());
					
					String t = (ent.getValue() instanceof JSONArray ? ((JSONArray)ent.getValue()).get(0) : ((ArrayList)ent.getValue()).get(0)) + "";
					
					if (Utils.isLong(t)) {
						rMap.put(key, Long.parseLong(t));
					}
				}
			}
		}
		
		return rMap;
	}
	
	public static Long getItemPrice(HashMap<Integer, Long> priceList, int enhanceLevel){
		if(priceList.containsKey(enhanceLevel)){
			return priceList.get(enhanceLevel);
		}
		
		for(int i = enhanceLevel; i >= 0; i--){
			if(priceList.containsKey(i)){
				return priceList.get(i);
			}
		}
		
		return null;
	}
	
	@NotNull
	public static LinkedHashMap<String, ArrayList<String>> getItemEffectDescription(Element el)
	{
		LinkedHashMap<String, ArrayList<String>> itemEffects = new LinkedHashMap<>();
		
		String tx = el.html();
		String[] kl = tx.split("<br>\n<br>");
		
		for(String tk : kl){
			String[] jk = tk.replace("\n", "").split("<br>");
			String key = BDOItemUtils.cleanString(jk[0]);
			
			if(key.isBlank()) continue;
			
			if(key.contains("<span") && itemEffects.size() > 0){
				List<String> ls = new ArrayList<>(itemEffects.keySet());
				key = ls.get(ls.size() - 1);
				
			}else if(key.contains("<span")){
				continue;
			}
			
			ArrayList<String> list = new ArrayList<>();
			
			for(int i = 1; i < jk.length; i++){
				list.add(Jsoup.parseBodyFragment(jk[i]).text());
			}
			
			itemEffects.put(key, list);
		}
		
		return itemEffects;
	}
	
	@NotNull
	public static String cleanString(String text)
	{
		text = text.replace("\n", "").replace("\t", "").trim();
		text = text.replace("–", " ").trim();
		text = text.replace("-", " ").trim();
		text = text.replace("–", "").trim();
		return text;
	}
	
	public static double getIncrementedChance(int i, double baseChance){
		return getEnhanceChance(getFSIncrement(i, baseChance), baseChance);
	}
	
	public static double getEnhanceChance(int fs, double baseChance){
		return baseChance * (1 + (0.1 * fs));
	}
	
	public static int getFSIncrement(int i, double baseChance)
	{
		return getFSIncrement(i, (float)baseChance);
	}
	
	public static int getFSIncrement(int i, float baseChance)
	{
		int increment = 1;
		
		if(baseChance <= 1){
			increment = 10;
		}else if(baseChance <= 15){
			increment = 5;
		}
		
		return i * increment;
	}
	
	public static String getEnhancePrefix(BDOItemData itemData, int enhance_level)
	{
		return getEnhancePrefix(enhance_level, itemData.enhancementLevels.length - 1);
	}
	
	public static String getEnhancePrefix(int enhance_level, int maxEnhance)
	{
		String enhancePrefix = "";
		
		if(maxEnhance <= 5 && enhance_level > 0){
			enhance_level += 20 - maxEnhance;
		}
		
		if (enhance_level > 0) {
			if (enhance_level <= 15) {
				enhancePrefix = "+" + enhance_level + " ";
			} else {
				String prefix =
						enhance_level == 16 ? "PRI"
						: enhance_level == 17 ? "DUO"
						: enhance_level == 18 ? "TRI"
						: enhance_level == 19 ? "TET"
						: enhance_level == 20 ? "PEN" : "";
				
				if (!prefix.isBlank()) {
					enhancePrefix = prefix + ": ";
				}
			}
		}
		
		return enhancePrefix;
	}
	
	public static int getEnhanceLevel(String search){
		int enhance_level = 0;
		Matcher matcher = Pattern.compile("(PEN|TET|TRI|DUO|PRI|\\+[0-9]+)\\b", Pattern.CASE_INSENSITIVE).matcher(search);
		
		if (matcher.find()) {
			String match = matcher.group(0);
			
			if(match.equalsIgnoreCase("pen")) enhance_level = 20;
			else if(match.equalsIgnoreCase("tet")) enhance_level = 19;
			else if(match.equalsIgnoreCase("tri")) enhance_level = 18;
			else if(match.equalsIgnoreCase("duo")) enhance_level = 17;
			else if(match.equalsIgnoreCase("pri")) enhance_level = 16;
			
			else{
				String t = match.replace("+", "");
				
				if(Utils.isInteger(t)){
					enhance_level = Math.max(0, Math.min(15, Integer.parseInt(t)));
				}
			}
		}
		
		return enhance_level;
	}
}
