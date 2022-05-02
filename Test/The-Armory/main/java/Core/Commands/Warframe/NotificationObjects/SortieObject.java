package Core.Commands.Warframe.NotificationObjects;

import Core.Commands.Warframe.Objects.InfoObject;
import Core.Commands.Warframe.Warframe;
import net.dv8tion.jda.api.EmbedBuilder;
import org.json.JSONArray;
import org.json.JSONObject;

import java.awt.Color;
import java.util.ArrayList;
import java.util.StringJoiner;

public class SortieObject extends InfoObject
{
	public static class variant{
		public String boss;
		public String planet;
		public String missionType;
		public String modifierDescription;
		public String node;
		
		public variant(String boss, String planet, String missionType, String modifierDescription, String node)
		{
			this.boss = boss;
			this.planet = planet;
			this.missionType = missionType;
			this.modifierDescription = modifierDescription;
			this.node = node;
		}
	}
	
	public String rewardPool;
	public variant[] variants;
	public String boss;
	
	public SortieObject(){ }
	
	public SortieObject(
			String platform, String id, String node, String planet, String type, String faction, String eta,
			String rewardPool, variant[] variants, String boss)
	{
		super(platform, id, node, planet, type, faction, eta);
		this.rewardPool = rewardPool;
		this.variants = variants;
		this.boss = boss;
	}
	
	@Override
	public String getName()
	{
		return "Sortie";
	}
	
	@Override
	public String getType()
	{
		return "sortie";
	}
	
	public SortieObject loadObject(JSONObject ob)
	{
		String platform = ob.getString("platform");
		String eta = ob.getString("eta");
		
		String id = ob.getString("id");
		String rewardPool = ob.has("rewardPool") ? ob.getString("rewardPool") : null;
		
		String boss = ob.has("boss") ? ob.getString("boss") : null;
		String faction = ob.has("faction") ? ob.getString("faction") : null;
		
		ArrayList<variant> list = new ArrayList<variant>();
		
		if(ob.has("variants")){
			JSONArray array = ob.getJSONArray("variants");
			
			for (Object ojb : array) {
				if (ojb instanceof JSONObject) {
					JSONObject var_obj = (JSONObject)ojb;
					
					String var_boss = var_obj.has("boss") ? var_obj.getString("boss") : null;
					String var_planet = var_obj.has("planet") ? var_obj.getString("planet") : null;
					String var_missionType = var_obj.has("missionType") ? var_obj.getString("missionType") : null;
					String var_modifierDescription = var_obj.has("modifierDescription") ? var_obj.getString("modifierDescription") : null;
					String var_node = var_obj.has("node") ? var_obj.getString("node") : null;
					
					variant var = new variant(var_boss, var_planet, var_missionType, var_modifierDescription, var_node);
					list.add(var);
				}
			}
		}
		
		SortieObject object = new SortieObject(platform, id, null, null, null, faction, eta, rewardPool, list.toArray(new variant[0]), boss);
		
		object.startTime = ob.getString("activation");
		
		object.done();
		return object;
	}
	
	@Override
	public String getInfo()
	{
		StringJoiner joiner = new StringJoiner("\n");
		joiner.add("- Faction: **" + factionName + "**");
		joiner.add("- Boss: **" + boss + "**");
		
		joiner.add("- Expires in: **" + expiresInText + "**");
		
		if(variants != null && variants.length > 0) {
			joiner.add("");
			int i = 1;
			for (variant var : variants) {
				joiner.add("\t**Stage " + i + "**: ");
				joiner.add("\t\t- Destination: **" + var.node + "**");
				joiner.add("\t\t- Mission type: **" + var.missionType + "**");
				
				joiner.add("\t\t- Modifier: **" + var.modifierDescription + "**\n");
				
				i++;
			}
		}
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
		
			builder.setColor(Color.red.darker());
			
			if (!expiresInText.toLowerCase().contains("infinity")) {
				builder.addField("Done in", expiresInText, true);
			}
			
			builder.addField("Boss", boss, false);
			builder.addField("Faction", factionName, false);
			builder.addField("Rewards", rewardPool, false);
			
			int i = 1;
			if(variants != null){
				for(variant var : variants){
					StringBuilder builder1 = new StringBuilder();
					
					if(var.boss != null && !var.boss.equalsIgnoreCase("Deprecated")){
						builder1.append("Boss: **" + var.boss + "**\n");
					}
					
					if(var.node != null && !var.node.equalsIgnoreCase("Deprecated")){
						builder1.append("Destination: **" + var.node + "**\n");
					}
					
					if(var.missionType != null && !var.missionType.equalsIgnoreCase("Deprecated")){
						builder1.append("Mission type: **" + var.missionType + "**\n");
					}
					
					builder1.append("Modifier: **" + var.modifierDescription + "**");
					
					builder.addField("Stage " + i, builder1.toString(), false);
					i++;
				}
			}
			
		return builder;
	}
}
