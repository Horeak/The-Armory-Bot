package Core.Commands.Warframe.NotificationObjects;

import Core.Commands.Warframe.Objects.InfoObject;
import Core.Commands.Warframe.Warframe;
import Core.Main.Logging;
import net.dv8tion.jda.api.EmbedBuilder;
import org.json.JSONObject;

import java.awt.Color;
import java.text.ParseException;
import java.time.Instant;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.StringJoiner;

public class FissureObject extends InfoObject
{
	public String tierName;
	public int tier;
	
	public FissureObject(){
	
	}
	
	public FissureObject(
			String platform, String id, String node, String planet, String type, String faction, String eta,
			String tierName, int tier)
	{
		super(platform, id, node, planet, type, faction, eta);
		
		this.tierName = tierName;
		this.tier = tier;
	}
	
	
	@Override
	public String getName()
	{
		return "Fissures";
	}
	
	@Override
	public String getType()
	{
		return "fissures";
	}
	
	
	public FissureObject loadObject(JSONObject ob)
	{
		String platform = ob.getString("platform");
		String id = ob.getString("id");
		String eta = ob.getString("eta");
		
		boolean expired = ob.getBoolean("expired");
		
		if (expired) {
			return null;
		}
		
		String node = ob.getString("node");
		String type = ob.getString("missionType");
		String faction = ob.getString("enemy");
		String tierName = ob.getString("tier");
		
		int tier = ob.getInt("tierNum");
		
		int nodeIndex = node.indexOf("(");
		int nodeIndex2 = node.indexOf(")");
		
		String nodeName = nodeIndex >= 1 && nodeIndex < node.length() ? node.substring(0, (nodeIndex >= 1 ? nodeIndex  - 1 : node.length())) : node;
		String planet = nodeIndex >= 1 && nodeIndex < node.length() && nodeIndex2 >= 1 && nodeIndex2 < node.length() ? node.substring(nodeIndex + 1, nodeIndex2) : node;
		
		FissureObject alert = new FissureObject(platform, id, nodeName, planet, type, faction, eta, tierName, tier);
		
		alert.keys.add(nodeName);
		alert.keys.add(planet);
		
		alert.startTime = ob.getString("activation");
		alert.endTime = ob.getString("expiry");
		
		alert.keys.add(type.toLowerCase());
		alert.keys.add(faction.toLowerCase());
		alert.keys.add(tierName.toLowerCase());
		
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
		joiner.add("- Tier: **" + tierName + "**");
		joiner.add("- Expires in: **" + expiresInText + "**");
		
		return joiner.toString();
	}
	
	
	@Override
	public EmbedBuilder genEmbed()
	{
		EmbedBuilder builder = new EmbedBuilder();
		
		builder.setThumbnail("http://i.imgur.com/EfIRu6v.png");
		builder.setColor(Color.CYAN);
		
		if (platform != null) {
			String name = Warframe.getPlatformName(platform);
			String icon = Warframe.getPlatformIcon(platform);
			
			if (name != null) {
				builder.setAuthor(name, null, icon);
			}
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
		
		builder.addField("Location", nodeName + " (" + planet + ")", true);
		builder.addField("Mission type", missionType, true);
		builder.addField("Faction", factionName, true);
		builder.addField("Tier", tierName, true);
		builder.addField("Expires in", expiresInText, false);
		
		return builder;
	}
	
	@Override
	public void sort(List<InfoObject> objects)
	{
		objects.sort(Comparator.comparing(o -> o.expiresIn));
		objects.sort(Comparator.comparing(o -> ((FissureObject)o).tier));
	}
}
