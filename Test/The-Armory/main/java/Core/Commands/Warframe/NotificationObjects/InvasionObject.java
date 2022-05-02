package Core.Commands.Warframe.NotificationObjects;

import Core.Commands.Warframe.Objects.InfoObject;
import Core.Commands.Warframe.Objects.RewardObject;
import Core.Commands.Warframe.Warframe;
import net.dv8tion.jda.api.EmbedBuilder;
import org.json.JSONArray;
import org.json.JSONObject;

import java.awt.Color;
import java.util.StringJoiner;

public class InvasionObject extends InfoObject
{
	public RewardObject attackerReward;
	public RewardObject defenderReward;
	public String attackingFaction;
	public String defendingFaction;
	public boolean isInfested;
	
	public String description;
	
	public InvasionObject(){}
	
	public InvasionObject(
			String platform, String id, String node, String planet, String eta, RewardObject attackerReward,
			RewardObject defenderReward, String attackingFaction, String defendingFaction, boolean isInfested,
			String description)
	{
		super(platform, id, node, planet, null, null, eta);
		this.attackerReward = attackerReward;
		this.defenderReward = defenderReward;
		this.attackingFaction = attackingFaction;
		this.defendingFaction = defendingFaction;
		this.isInfested = isInfested;
		this.description = description;
	}
	
	@Override
	public String getName()
	{
		return "Invasions";
	}
	
	@Override
	public String getType()
	{
		return "invasions";
	}
	
	public InvasionObject loadObject(JSONObject ob)
	{
		String platform = ob.getString("platform");
		String eta = ob.getString("eta");
		
		boolean expired = ob.getBoolean("completed");
		
		if (expired) {
			return null;
		}
		
		JSONObject attackerReward = ob.has("attackerReward") ? ob.getJSONObject("attackerReward") : null;
		JSONObject defenderReward = ob.has("defenderReward") ? ob.getJSONObject("defenderReward") : null;
		
		String attackingFaction = ob.has("attackingFaction") ? ob.getString("attackingFaction") : null;
		String defendingFaction = ob.has("defendingFaction") ? ob.getString("defendingFaction") : null;
		
		boolean isInfested = ob.has("vsInfestation") & ob.getBoolean("vsInfestation");
		
		if (attackingFaction == null && isInfested) {
			attackingFaction = "Infested";
		}
		
		String id = ob.getString("id");
		String node = ob.getString("node");
		
		String description = null;
		
		if (ob.has("desc")) {
			description = ob.getString("desc");
		}
		
		RewardObject attackerRewardObj = null;
		RewardObject defenderRewardObj = null;
		
		if (attackerReward != null) {
			JSONArray items = attackerReward.getJSONArray("countedItems");
			
			for (Object k : items) {
				JSONObject kl = (JSONObject)k;
				attackerRewardObj = new RewardObject(kl.getString("type"), kl.getInt("count"));
			}
		}
		
		if (defenderReward != null) {
			JSONArray items = defenderReward.getJSONArray("countedItems");
			
			for (Object k : items) {
				JSONObject kl = (JSONObject)k;
				defenderRewardObj = new RewardObject(kl.getString("type"), kl.getInt("count"));
				break;
			}
		}
		
		String nodeName = node.substring(0, node.indexOf("(") - 1);
		String planet = node.substring(node.indexOf("(") + 1, node.indexOf(")"));
		InvasionObject object = new InvasionObject(platform, id, nodeName, planet, eta, attackerRewardObj,
		                                           defenderRewardObj, attackingFaction, defendingFaction, isInfested,
		                                           description);
		
		if (description != null) {
			object.description = description;
		}
		
		object.keys.add(nodeName);
		object.keys.add(planet);
		object.startTime = ob.getString("activation");
		
		if (attackerRewardObj != null) {
			object.keys.add(attackerRewardObj.name);
		}
		if (defenderRewardObj != null) {
			object.keys.add(defenderRewardObj.name);
		}
		
		if (attackingFaction != null) {
			object.keys.add(attackingFaction);
		}
		if (defendingFaction != null) {
			object.keys.add(defendingFaction);
		}
		
		object.done();
		return object;
	}
	
	@Override
	public String getInfo()
	{
		StringJoiner joiner = new StringJoiner("\n");
		
		String attacking = getAttackingReward();
		String defending = getDefendingReward();
		
		String aFaction = attackingFaction != null && !attackingFaction.isBlank() ? attackingFaction : "None";
		String dFaction = defendingFaction != null && !defendingFaction.isBlank() ? defendingFaction : "None";
		
		if(attacking == null || attacking.isBlank()) attacking = "None";
		if(defending == null || defending.isBlank()) defending = "None";
		
		String dest = (planet != null ? "**" + planet + "**" + " " : "") + (nodeName != null ? (planet != null ? "- " : "") + "**" + nodeName + "**" : "");
		
		joiner.add("- Destination: " + dest);
		
		StringJoiner factionJoiner = new StringJoiner(" vs ");
		StringJoiner rewardJoiner = new StringJoiner(" vs ");
		
		factionJoiner.add("**" + aFaction + "**");
		factionJoiner.add("**" + dFaction + "**");
		
		joiner.add("\t- Factions: " + factionJoiner.toString());
		
		rewardJoiner.add("**" + attacking + "**");
		rewardJoiner.add("**" + defending + "**");
		
		joiner.add("- Rewards: " + rewardJoiner.toString());
		joiner.add("- Expires in: " + expiresInText);
		
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
		
		builder.setColor(Color.blue);
		
		if (description != null && !description.isEmpty()) {
			builder.setDescription("*" + description + "*");
		}
		
		String attackerUrl = attackerReward != null ? Warframe.getWikiLink(attackerReward.name) : null;
		String defenderUrl = defenderReward != null ? Warframe.getWikiLink(defenderReward.name) : null;
		
		String TattackerReward = attackerReward != null ? (attackerReward.amount + "x " + (attackerUrl != null ? "[" + attackerReward.name + "](" + attackerUrl + ")" : attackerReward.name)) : null;
		String TdefenderReward = defenderReward != null ? (defenderReward.amount + "x " + (defenderUrl != null ? "[" + defenderReward.name + "](" + defenderUrl + ")" : defenderReward.name)) : null;
		
		StringJoiner joiner = new StringJoiner(" or ");
		
		if (TattackerReward != null) {
			joiner.add(TattackerReward);
		}
		if (TdefenderReward != null) {
			joiner.add(TdefenderReward);
		}
		
		builder.addField("Location", nodeName + " (" + planet + ")", true);
		
		if (!expiresInText.toLowerCase().contains("infinity")) {
			builder.addField("Done in", expiresInText, true);
		}
		
		builder.addField("Factions", attackingFaction + " vs " + defendingFaction, false);
		builder.addField("Rewards", joiner.toString(), false);
		
		return builder;
	}
	
	
	public String getAttackingReward()
	{
		String url = attackerReward != null ? Warframe.getWikiLink(attackerReward.name) : null;
		return attackerReward != null ? (attackerReward.amount + "x " + (url != null ? "[" + attackerReward.name + "](" + url + ")" : attackerReward.name)) : "";
	}
	
	public String getDefendingReward()
	{
		String url = defenderReward != null ? Warframe.getWikiLink(defenderReward.name) : null;
		return defenderReward != null ? (defenderReward.amount + "x " + (url != null ? "[" + defenderReward.name + "](" + url + ")" : defenderReward.name)) : "";
	}
}
