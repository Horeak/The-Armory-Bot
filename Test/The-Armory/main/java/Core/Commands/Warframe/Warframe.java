package Core.Commands.Warframe;

import Core.CommandSystem.CommandObjects.SlashCommands.SlashCommandChannel;
import Core.CommandSystem.CommandObjects.SlashCommands.SlashCommandMessage;
import Core.Main.Logging;
import Core.Objects.Annotation.Commands.Command;
import Core.Objects.Annotation.Commands.CommandGroup;
import Core.Objects.Annotation.Method.Startup.Init;
import Core.Objects.Annotation.Method.Startup.PostInit;
import Core.Objects.Interfaces.Commands.IBaseSlashCommand;
import Core.Objects.Interfaces.Commands.ISlashCommand;
import Core.Util.ConnectionUtils;
import Core.Util.Utils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.text.WordUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Command
public class Warframe implements ISlashCommand
{
	public static ConcurrentHashMap<String, String> platformNames = new ConcurrentHashMap<>();
	public static ConcurrentHashMap<String, String> platformIcons = new ConcurrentHashMap<>();
	
	@PostInit
	public static void initPlatforms(){
		platformNames.put("xb1", "Xbox One");
		platformIcons.put("xb1", "https://upload.wikimedia.org/wikipedia/commons/thumb/8/84/Xbox_logo_2012_cropped.svg/152px-Xbox_logo_2012_cropped.svg.png");
		
		platformNames.put("ps4", "Playstation 4");
		platformIcons.put("ps4", "https://i.pinimg.com/originals/ff/e1/8b/ffe18bc07f4987eed129481171683dad.jpg");
		
		platformNames.put("pc", "PC");
		platformIcons.put("pc", "https://upload.wikimedia.org/wikipedia/commons/thumb/d/d7/Desktop_computer_clipart_-_Yellow_theme.svg/281px-Desktop_computer_clipart_-_Yellow_theme.svg.png");
		
		platformNames.put("swi", "Nintendo Switch");
		platformIcons.put("swi", "https://upload.wikimedia.org/wikipedia/commons/thumb/5/5d/Nintendo_Switch_Logo.svg/600px-Nintendo_Switch_Logo.svg.png");
	}
	
	public static String[] getPlatforms(){
		return Collections.list(platformNames.keys()).toArray(new String[0]);
	}
	
	public static String getPlatformName(String platform){
		return platformNames.get(platform);
	}
	
	public static String getPlatformIcon(String platform){
		return platformIcons.get(platform);
	}

	@CommandGroup(parent = Warframe.class)
	public static class WarframeNotifications implements IBaseSlashCommand{
		
		@Override
		public String getSlashName()
		{
			return "notifications";
		}
	}
	
	@CommandGroup(parent = Warframe.class)
	public static class WarframeInfo implements IBaseSlashCommand{
		
		@Override
		public String getSlashName()
		{
			return "info";
		}
	}
	
	@CommandGroup(parent = Warframe.class)
	public static class WarframeOther implements IBaseSlashCommand{
		
		@Override
		public String getSlashName()
		{
			return "other";
		}
	}
	
	
	//TODO Add sorties
	
	
	//TODO Add events command similar to notificationsSystem, eventsFormat.png
	public static final String credits_icon = "<:credits:467736288909590531>";
	public static final String ducats_icon = "<:ducats:473137287371161610>";
	public static final String platinum_icon = "<:platinum:721080654896431146>";
	
	public static final String nightmare_icon = "<:nigthmare:467747767981441034>";
	public static final String archwing_icon = "<:archwing:467757694644649994>";
	
	//Trading slots
	public static final String trading_icon_allow = "<:tradingAllowed:469245993875931137>";
	public static final String trading_icon_not_allowed = "<:tradingNotAllowed:469246864458252289>";
	
	//Riven slots
	public static final String riven_empty_icon = "<:rivenEmpty:474247018404118528>";
	public static final String riven_filled_icon = "<:rivenFilled:474247032031281152>";
	
	//Polarities
	public static final String madurai_icon = "<:madurai:474235765841199115>";
	public static final String vazarin_icon = "<:vazarin:474236033102249984>";
	public static final String naramon_icon = "<:Naramon:474236180716847105>";
	public static final String zenurik_icon = "<:zenurik:474236335662563352>";
	public static final String unairu_icon = "<:Unairu:474236487123206145>";
	public static final String penjaga_icon = "<:penjaga:474236643583459356>";
	public static final String umbra_icon = "<:umbra:474236763968372766>";
	
	public static final String baseUrl = "https://api.warframestat.us/";
	public static final String wikiUrl = "https://warframe.fandom.com/wiki/";
	
	public static ConcurrentHashMap<String, String> thumbnails = new ConcurrentHashMap<>();
	public static ConcurrentHashMap<String, String> wikiLinks = new ConcurrentHashMap<>();
	
	public static final ConcurrentHashMap<String, JSONObject> jsonObjects = new ConcurrentHashMap<>();
	public static final ConcurrentHashMap<String, Long> lastUpdateJson = new ConcurrentHashMap<>();
	public static final ConcurrentHashMap<String, Document> documents = new ConcurrentHashMap<>();
	public static final ConcurrentHashMap<String, Long> lastUpdate = new ConcurrentHashMap<>();
	public static final HashMap<String, String> specificNames = new HashMap<>();
	
	public static final SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US);
	
	@Init
	public static void init()
	{
		format.setTimeZone(TimeZone.getTimeZone("UTC"));
		
		specificNames.put("EMP Aura", "EMP Aura");
		specificNames.put("(?i).* syandana", "Syandana");
		specificNames.put("(?i).*specter.*", "Specter");
		specificNames.put("(?i)Sands Of Inaros", "Sands of Inaros");
		
		specificNames.put("(?i).*(armor set|spurs|mask|chest plate|shoulder plates)", "Armor_(Cosmetic)");
		
		specificNames.put("(?i)mantis prisma skin", "mantis");
	}
	
	public static String getPlatform(String platform){
		if(platform.equals("PC")) return "pc";
		if(platform.equals("Playstation 4")) return "ps4";
		if(platform.equals("Xbox one")) return "xb1";
		if(platform.equals("Nintendo Switch")) return "swi";
		
		return platform;
	}
	
	public static JSONArray getArray(String platform, String type)
	{
		platform = getPlatform(platform);
		JSONObject object = getJsonObject(platform);
		
		if (object != null) {
			if (object.has(type.toLowerCase())) {
				JSONArray array = object.optJSONArray(type);
				
				if(array != null) {
					for (Object tk : array) {
						JSONObject ob = (JSONObject)tk;
						ob.put("platform", platform);
					}
				}else{
					JSONObject object1 = object.optJSONObject(type);
					
					if(object1 != null){
						object1.put("platform", platform);
						
						JSONArray array1 = new JSONArray();
						array1.put(object1);
						
						return array1;
					}
				}
				
				return array;
			}
		}
		
		return null;
	}
	
	public static JSONObject getJsonObject(String platform)
	{
		platform = getPlatform(platform);
		
		boolean update = false, updateJson = false;
		
		if (!documents.containsKey(platform)) {
			updateDocument(platform);
			update = true;
		}
		
		if (!update) {
			if (lastUpdate.containsKey(platform)) {
				long t = System.currentTimeMillis() - lastUpdate.get(platform);
				
				if (t >= TimeUnit.MILLISECONDS.convert(2, TimeUnit.MINUTES)) {
					updateDocument(platform);
				}
			}
		}
		
		if (!jsonObjects.containsKey(platform)) {
			updateJson(platform);
			updateJson = true;
		}
		
		
		if (!updateJson) {
			if (lastUpdateJson.containsKey(platform)) {
				long t = System.currentTimeMillis() - lastUpdateJson.get(platform);
				
				if (t >= TimeUnit.MILLISECONDS.convert(2, TimeUnit.MINUTES)) {
					updateJson(platform);
				}
			}
		}
		
		return jsonObjects.get(platform);
	}
	
	public static void updateDocument(String platform)
	{
		platform = getPlatform(platform);
		
		
		lastUpdate.put(platform, System.currentTimeMillis());
		
		try {
			Document doc = ConnectionUtils.getDocument(baseUrl + platform);
			
			if(doc != null) {
				documents.put(platform, doc);
			}
		} catch (IOException e) {}
	}
	
	public static void updateJson(String platform)
	{
		platform = getPlatform(platform);
		
		Document doc1 = documents.get(platform);
		
		if(doc1 == null || doc1.body() == null || doc1.body().text() == null) return;
		
		String t = doc1.body().text();
		Object tg = new JSONTokener(t).nextValue();
		
		if(tg instanceof JSONObject) {
			JSONObject object = (JSONObject)tg;
			
			jsonObjects.put(platform, object);
			lastUpdateJson.put(platform, System.currentTimeMillis());
		}
	}
	
	public static JSONObject getObject(String platform, String type)
	{
		platform = getPlatform(platform);
		JSONObject object = getJsonObject(platform);
		
		if (object != null) {
			if (object.has(type)) {
				JSONObject object1 = object.getJSONObject(type);
				object1.put("platform", platform);
				
				return object1;
			}
		}
		
		return null;
	}
	
	public static String getWikiLink(String tk)
	{
		return getWikiLink(tk, true);
	}
	
	//TODO Find a better way to be able to get the correct link, try to find how the wiki is redirecting names
	public static String getWikiLink(String tk, boolean modifyName)
	{
		tk = processName(tk);
		String url = wikiUrl + tk;
		
		if (checkUrl(url)) {
			wikiLinks.put(tk, url);
		}
		
		if (wikiLinks.containsKey(tk)) {
			return wikiLinks.get(tk);
		}
		
		int tries1 = 0;
		String tkj1 = tk.trim();
		
		if (modifyName) {
			while (tkj1.indexOf("_") > 0 && tries1 <= 5) {
				String[] tkl = tkj1.split("_");
				url = wikiUrl + tkj1;
				
				if (tkl.length > 1) {
					tkj1 = tkj1.replace(tkl[tkl.length - 1], "");
				}
				
				tries1++;
				
				if (wikiLinks.containsKey(tkj1)) {
					return wikiLinks.get(tkj1);
				}
				
				if (checkUrl(url)) {
					wikiLinks.put(tk, url);
					return url;
				}
			}
		}
		
		return null;
	}
	
	
	public static String processName(String tk)
	{
		String jg = tk;
		boolean caseChange = false;
		
		for (Map.Entry<String, String> ent : specificNames.entrySet()) {
			String tt1 = ent.getKey();
			String tt2 = ent.getValue();
			
			tt1 = tt1.strip();
			tt2 = tt2.strip();
			tk = tk.strip();
			
			if (tk.equalsIgnoreCase(tt1)) {
				tk = tt2;
				caseChange = true;
				break;
			} else if (tk.equalsIgnoreCase(tt1.replace(" ", "_"))) {
				tk = tt2.replace(" ", "_");
				caseChange = true;
				break;
			}
			
			if (tk.matches(tt1)) {
				tk = tt2;
				caseChange = true;
				break;
			}
		}
		
		for (String o : jg.split(" ")) {
			if (o.toLowerCase().endsWith("x") && (Utils.isInteger(o.toLowerCase().substring(0, o.length() - 1)))) {
				o = o.substring(0, o.length() - 1);
				tk = tk.replaceFirst("(?i)x", "");
			}
			
			if (o.toLowerCase().endsWith("%")) {
				tk = tk.replace(o, "");
			}
			
			if (Utils.isInteger(o) || Utils.isNumber(o) || Utils.isFloat(o) || Utils.isDouble(o)) {
				tk = tk.replace(o, "");
			}
		}
		
		if (!caseChange) {
			tk = WordUtils.capitalize(tk);
		}
		
		
		tk = tk.replace("[", "").replace("]", "");
		
		tk = tk.strip();
		tk = tk.replace(" ", "_");
		
		return tk;
	}
	
	private static boolean checkUrl(String url)
	{
		try {
			URL uri = new URL(url);
			
			try {
				Document doc1 = ConnectionUtils.getDocument(url);
				
				if (doc1 != null) {
					Elements esx1 = doc1.select("div.noarticletext");
					return !doc1.hasClass("div.noarticletext") && (esx1 == null || esx1.size() <= 0);
				}
				
			} catch (IOException e) {
				Logging.exception(e);
			}
			
		} catch (MalformedURLException e) {
		}
		
		return false;
	}
	
	
	public static String getThumbnail(String tk)
	{
		return getThumbnail(tk, true);
	}
	
	public static String getThumbnail(String tk, boolean modifyName)
	{
		tk = processName(tk);
		String uj = tk;
		
		if (thumbnails.containsKey(tk)) {
			if (thumbnails.get(tk) == null || thumbnails.get(tk).isEmpty()) {
				thumbnails.remove(tk);
			} else {
				return thumbnails.get(tk);
			}
		}
		
		String tg = null;
		
		try {
			Document doc1 = ConnectionUtils.getDocument(getWikiLink(tk, modifyName));
			
			if (doc1 == null) {
				return null;
			}
			
			Elements e1 = doc1.getElementsByClass("image-thumbnail");
			
			if (e1 != null) {
				if (e1.hasAttr("href")) {
					tg = e1.attr("href");
				} else if (e1.hasAttr("src")) {
					tg = e1.attr("src");
				}
			}
			
			if (tg != null && !tg.isEmpty() && !tg.startsWith("http")) {
				e1 = doc1.getElementsByClass("image image-thumbnail");
				
				if (e1 != null) {
					if (e1.hasAttr("href")) {
						tg = e1.attr("href");
					} else if (e1.hasAttr("src")) {
						tg = e1.attr("src");
					}
				}
			}
			
			if (tg == null) {
				Elements e = doc1.select("meta[property=og:image]");
				
				if (e != null) {
					tg = e.attr("content");
				}
			}
			
			if (tg != null) {
				if (tg.contains("/revision")) {
					tg = tg.substring(0, tg.indexOf("/revision"));
				}
			}
		} catch (Exception ignored) {
		}
		
		if (tg != null && !tg.isEmpty()) {
			if (!thumbnails.containsKey(uj)) {
				thumbnails.put(uj, tg);
			}
		}
		
		return tg;
	}
	
	public static String getPolarityIcon(String name)
	{
		if (name.equalsIgnoreCase("madurai")) {
			return madurai_icon;
		}
		
		if (name.equalsIgnoreCase("vazarin")) {
			return vazarin_icon;
		}
		
		if (name.equalsIgnoreCase("naramon")) {
			return naramon_icon;
		}
		
		if (name.equalsIgnoreCase("zenurik")) {
			return zenurik_icon;
		}
		
		if (name.equalsIgnoreCase("unairu")) {
			return unairu_icon;
		}
		
		if (name.equalsIgnoreCase("penjaga")) {
			return penjaga_icon;
		}
		
		if (name.equalsIgnoreCase("umbra")) {
			return umbra_icon;
		}
		
		return null;
	}
	private boolean shouldAdd(String input, String check){
		double dif1 = Utils.compareStrings(input, check);
		
		if(dif1 <= 4 && (check.length() != dif1 && input.length() != dif1)) {
			String stringDif = StringUtils.difference(input, check);
			int dif2 = stringDif.length();
			
			if (stringDif.equalsIgnoreCase(check)) {
				return false;
			}
			
			return dif2 <= 6;
		}
		
		return false;
	}
	
	protected void AddMethod(ArrayList<String> list, EmbedBuilder builder, String name, String input)
	{
		if (list.size() > 0) {
			list.sort(Comparator.comparingDouble(c -> Utils.compareStrings(c, input)));
			list.sort(Comparator.comparingInt(c -> StringUtils.difference(c, input).length()));
			
			if (list.size() > 3) {
				list = new ArrayList<>(list.subList(0, 3));
			}
			
			StringBuilder builder1 = new StringBuilder();
			
			for (String t : list) {
				String ent = StringUtils.capitalize(t);
				boolean hasWikiPage = Warframe.getWikiLink(ent) != null;
				String wikiName = hasWikiPage ? "[" + ent + "](" + Warframe.getWikiLink(ent) + ")" : ent;
				builder1.append("- " + wikiName + "\n");
			}
			
			builder.addField(name, builder1.toString(), false);
		}
	}
	
	@Override
	public String getSlashName()
	{
		return "warframe";
	}
	
	@Override
	public String getDescription()
	{
		return "Allows searching for specific items and warframes and will attempt to show recipes and sources for the results";
	}
	
	@Override
	public void slashCommandExecuted(SlashCommandInteractionEvent slashEvent, Guild guild, User author, SlashCommandChannel channel, SlashCommandMessage message) { }
}
