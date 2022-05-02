package Core.Commands.Warframe.NotificationObjects;

import Core.Commands.Warframe.Objects.InfoObject;
import Core.Commands.Warframe.Objects.RewardObject;
import Core.Commands.Warframe.Warframe;
import Core.Main.Logging;
import Core.Util.Utils;
import net.dv8tion.jda.api.EmbedBuilder;
import org.json.JSONArray;
import org.json.JSONObject;

import java.awt.Color;
import java.text.ParseException;
import java.time.Instant;
import java.util.*;

public class AlertObject extends InfoObject
{
	public String levelText;
	public ArrayList<RewardObject> rewards = new ArrayList<>();
	public int minLevel;
	public int maxLevel;
	public int missionLevel;
	public int credits;
	
	public String description;
	public String thumbnail;
	public int color;
	public boolean nightmare, archwing;
	
	public AlertObject(){ }
	
	public AlertObject(
			String platform, String id, String node, String planet, String type, String faction, String eta,
			int minLevel, int maxLevel, int missionLevel)
	{
		super(platform, id, node, planet, type, faction, eta);
		this.minLevel = minLevel;
		this.maxLevel = maxLevel;
		this.missionLevel = missionLevel;
		
		this.levelText = minLevel + "-" + maxLevel;
	}
	
	@Override
	public String getName()
	{
		return "Alerts";
	}
	
	@Override
	public String getType()
	{
		return "alerts";
	}
	
	public AlertObject loadObject(JSONObject ob)
	{
		String platform = ob.getString("platform");
		String eta = ob.getString("eta");
		
		boolean expired = ob.has("expired") && ob.getBoolean("expired");
		
		if (expired) {
			return null;
		}
		
		JSONObject object = ob.getJSONObject("mission");
		JSONObject rewards = object.getJSONObject("reward");
		
		String id = ob.getString("id");
		
		String node = object.getString("node");
		String type = object.getString("type");
		String faction = object.getString("faction");
		
		String reward = rewards.getString("itemString");
		
		String description = null;
		
		if (object.has("description")) {
			description = object.getString("description");
		}
		
		ArrayList<RewardObject> rewardObjects = new ArrayList<>();
		JSONArray items = rewards.getJSONArray("countedItems");
		
		int missionLevel = object.has("maxWaveNum") ? object.getInt("maxWaveNum") : -1;
		
		
		for (Object k : items) {
			JSONObject kl = (JSONObject)k;
			rewardObjects.add(new RewardObject(kl.getString("type"), kl.getInt("count")));
		}
		
		int amount = 1;
		String name = reward;
		
		for (String tt : reward.split(" ")) {
			if (Utils.isInteger(tt)) {
				amount = Integer.parseInt(tt);
				name = reward.replaceFirst(tt + " ", "");
			}
		}
		
		if (name != null && !name.isEmpty()) {
			boolean has = false;
			
			for (RewardObject object1 : rewardObjects) {
				if (name.equalsIgnoreCase(object1.name)) {
					has = true;
				}
			}
			
			if (!has) {
				rewardObjects.add(new RewardObject(name, amount));
			}
		}
		
		String thumbnail = null;
		
		if (rewards.has("thumbnail")) {
			thumbnail = rewards.getString("thumbnail");
		}
		
		int minLevel = object.getInt("minEnemyLevel");
		int maxLevel = object.getInt("maxEnemyLevel");
		
		boolean nightmare = object.getBoolean("nightmare");
		boolean archwing = object.getBoolean("archwingRequired");
		
		int nodeIndex = node.indexOf("(");
		int nodeIndex2 = node.indexOf(")");
		
		String nodeName = nodeIndex >= 1 && nodeIndex < node.length() ? node.substring(0, (nodeIndex >= 1 ? nodeIndex  - 1 : node.length())) : node;
		String planet = nodeIndex >= 1 && nodeIndex < node.length() && nodeIndex2 >= 1 && nodeIndex2 < node.length() ? node.substring(nodeIndex + 1, nodeIndex2) : node;
		
		
		AlertObject alert = new AlertObject(platform, id, nodeName, planet, type, faction, eta, minLevel, maxLevel,
		                                    missionLevel);
		
		if (description != null) {
			alert.description = description;
		}
		
		alert.keys.add(nodeName);
		alert.keys.add(planet);
		
		alert.archwing = archwing;
		alert.nightmare = nightmare;
		
		alert.credits = rewards.getInt("credits");
		alert.color = rewards.getInt("color");
		
		alert.endTime = ob.getString("expiry");
		alert.startTime = ob.getString("activation");
		
		alert.rewards.addAll(rewardObjects);
		
		for (RewardObject to : rewardObjects) {
			alert.keys.add(to.name);
		}
		
		alert.keys.add(type.toLowerCase());
		alert.keys.add(faction.toLowerCase());
		
		if (nightmare) {
			alert.keys.add("nightmare");
		}
		if (archwing) {
			alert.keys.add("archwing");
		}
		
		if (thumbnail != null) {
			alert.thumbnail = thumbnail;
		}
		
		alert.done();
		return alert;
	}
	
	
	@Override
	public String getInfo()
	{
		StringJoiner joiner = new StringJoiner("\n");
		String dest = (planet != null ? "**" + planet + "**" + " " : "") + (nodeName != null ? (planet != null ? "- " : "") + "**" + nodeName + "**" : "");
		
		joiner.add("- Destination: " + dest);
		joiner.add("- Type: **" + missionType + "**");
		joiner.add("- Faction: **" + factionName + "**");
		joiner.add("- Level: **" + levelText + "**");
		joiner.add("- Expires in: **" + expiresInText + "**");
		joiner.add("- Rewards: **" + getRewardString() + "**");
		
		return joiner.toString();
	}
	
	@Override
	public EmbedBuilder genEmbed()
	{
		EmbedBuilder builder = new EmbedBuilder();
		
		if (platform != null) {
			String name = Warframe.getPlatformName(platform);
			String icon = Warframe.getPlatformIcon(platform);
			
			if (name != null) {
				builder.setAuthor(name, null, icon);
			}
		}
		
		if (thumbnail != null) {
			if(EmbedBuilder.URL_PATTERN.matcher(thumbnail).matches()) {
				builder.setThumbnail(thumbnail);
			}
		}
		
		Color c = new Color(color);
		builder.setColor(c);
		
		String rewardsS = "";
		
		if (nightmare) {
			missionType += " " + Warframe.nightmare_icon;
		}
		
		if (archwing) {
			missionType += " " + Warframe.archwing_icon;
		}
		
		String rew = getRewardString();
		
		if(rew != null && !rew.isBlank()) {
			String tk = Warframe.getThumbnail(getRewardString());
			
			if (tk != null && !tk.isEmpty()) {
				if (tk.startsWith("http")) {
					builder.setThumbnail(tk);
				}
			}
		}
		
		if (rewards.size() <= 0) {
			rewards.add(new RewardObject("Credits", credits));
		}
		
		if (rewards.size() > 0) {
			StringJoiner joiner = new StringJoiner(", ");
			
			for (RewardObject ob : rewards) {
				if (ob.name.toLowerCase().contains("credits")) {
					joiner.add(Warframe.credits_icon + " " + ob.amount + " " + ob.name);
					break;
				}
				
				String link = Warframe.getWikiLink(ob.name);
				
				if (link != null && !link.isEmpty()) {
					joiner.add(ob.amount + "x " + ("[" + ob.name + "](" + link + ")"));
				} else {
					joiner.add(ob.amount + "x " + ob.name);
				}
			}
			
			rewardsS = joiner.toString();
		}
		
		try {
			Date dt = Warframe.format.parse(endTime);
			
			if (dt != null) {
				builder.setFooter("Expires: ", null);
				builder.setTimestamp(Instant.ofEpochMilli(dt.getTime()));
			}
			
		} catch (ParseException e) {
			Logging.exception(e);
		}
		
		if (description != null && !description.isEmpty()) {
			builder.setDescription("*" + description + "*");
		}
		
		builder.addField("Location", nodeName + " (" + planet + ")", true);
		builder.addField("Mission type", missionType, true);
		
		if (missionLevel != -1) {
			String missionText;
			String suffix = "";
			String value = Integer.toString(missionLevel);
			
			if (missionType.equalsIgnoreCase("survival")) {
				missionText = "Required time";
			} else if (missionType.equalsIgnoreCase("interception")) {
				missionText = "Required rounds";
			} else if (missionType.equalsIgnoreCase("spy")) {
				missionText = "Required data vaults";
			} else {
				missionText = "Required waves";
			}
			
			if (missionType.equalsIgnoreCase("survival")) {
				suffix = " min";
			} else if (missionType.equalsIgnoreCase("spy")) {
				suffix = "/3 vaults";
			}
			
			builder.addField(missionText, value + suffix, true);
		}
		
		builder.addField("Faction", factionName, true);
		builder.addField("Level", levelText, true);
		builder.addField("Expires in", expiresInText, false);
		
		if (rewardsS != null && !rewardsS.isEmpty()) {
			builder.addField("Rewards", rewardsS, true);
		}
		
		if (!rewardsS.toLowerCase().contains("credits")) {
			builder.addField("Credits", Warframe.credits_icon + " " + credits, true);
		}
		
		return builder;
	}
	
	public String getRewardString()
	{
		StringJoiner joiner = new StringJoiner(", ");
		
		for (RewardObject object : rewards) {
			String url = object.name != null ? Warframe.getWikiLink(object.name) : null;
			
			joiner.add(object.amount + "x " + (url != null ? "[" + object.name + "](" + url + ")" : object.name));
		}
		
		if (joiner.length() <= 0) {
			joiner.add(credits + " Credits");
		}
		
		return joiner.toString();
	}
	
	public void sort(List<InfoObject> objects)
	{
		objects.sort(Comparator.comparing(o -> o.expiresIn));
		objects.sort(Comparator.comparing(o -> (((AlertObject)o).minLevel) + ((AlertObject)o).maxLevel));
	}
}
