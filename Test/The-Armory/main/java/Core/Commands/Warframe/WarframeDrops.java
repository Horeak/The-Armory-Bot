package Core.Commands.Warframe;

import Core.CommandSystem.ChatUtils;
import Core.CommandSystem.CommandObjects.SlashCommands.SlashCommandChannel;
import Core.CommandSystem.CommandObjects.SlashCommands.SlashCommandMessage;
import Core.Objects.Annotation.Commands.SlashArgument;
import Core.Objects.Annotation.Commands.SubCommand;
import Core.Objects.CustomEntry;
import Core.Objects.Interfaces.Commands.ISlashCommand;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Locale;

@SubCommand( parent = Warframe.WarframeInfo.class)
public class WarframeDrops implements ISlashCommand
{
	@Override
	public String getDescription()
	{
		return "Shows all drop sources for specific items in the game";
	}
	
	@SlashArgument( key = "item", text = "Which item the drops for", required = true)
	public String itemName;
	
	@Override
	public String getSlashName()
	{
		return "drops";
	}
	
	@Override
	public void slashCommandExecuted(SlashCommandInteractionEvent slashEvent, Guild guild, User author, SlashCommandChannel channel, SlashCommandMessage message)
	{
		DecimalFormat formatter = (DecimalFormat) NumberFormat.getInstance(Locale.US);
		DecimalFormatSymbols symbols = formatter.getDecimalFormatSymbols();
		
		symbols.setGroupingSeparator('.');
		formatter.setDecimalFormatSymbols(symbols);
		
		JSONObject object = WarframeItemSystem.getItem(itemName);
		
		if(object == null){
			ChatUtils.sendEmbed(channel, author.getAsMention() + " Found no item with that name!");
			return;
		}
		
		if(object != null){
			EmbedBuilder builder = new EmbedBuilder();
			
			String name = object.getString("name");
			String url = object.has("wikiaUrl") ? object.getString("wikiaUrl") : null;
			
			if(url == null){
				url = Warframe.getWikiLink(name);
			}
			
			if(url != null){
				builder.setTitle(name, url);
			}else{
				builder.setTitle(name);
			}
			
			if(object.has("description")){
				builder.setDescription("*" + object.getString("description") + "*");
			}
			
			//Drops
			{
				ArrayList<JSONObject> dropObject = WarframeItemSystem.getDropItem(name);
				
				if(dropObject == null){
					ChatUtils.sendEmbed(channel, author.getAsMention() + " Found no item with that name!");
					return;
				}
				
				if(dropObject != null) {
					if (dropObject.size() > 0) {
						ArrayList<CustomEntry<Integer, JSONObject>> list = new ArrayList<>();
						
						for(JSONObject jsonObject : dropObject) {
							if(!jsonObject.has("drops")) continue;
							
							int itemCount = jsonObject.has("itemCount") ? jsonObject.getInt("itemCount") : 1;
							
							org.json.JSONArray array = jsonObject.getJSONArray("drops");
							
							for (Object obj : array) {
								if (obj instanceof JSONObject) {
									JSONObject jObject = (JSONObject)obj;
									
									if (jObject.has("chance")) {
										list.add(new CustomEntry<>(itemCount, jObject));
									}
								}
							}
						}
						
						list.sort(Comparator.comparingDouble(o -> o.getValue().has("chance") ? o.getValue().getDouble("chance") : 0));
						Collections.reverse(list);
						
						if(list.size() > 200) {
							list = new ArrayList<>(list.subList(0, 200));
						}
						
						StringBuilder dropsBuilder = new StringBuilder();
						
						for (CustomEntry<Integer, JSONObject> entry : list) {
							JSONObject object1 = entry.getValue();
							
							double chance = object1.has("chance") ? object1.getDouble("chance") : -1;
							String rotation = object1.has("rotation") ? object1.getString("rotation") : null;
							String location = object1.has("location") ? object1.getString("location") : null;
							String type = object1.has("type") ? object1.getString("type") : null;
							
							String locName = location;
							
							if(rotation != null &&
							   (!rotation.equalsIgnoreCase("rewards") && !rotation.equalsIgnoreCase("Sabotage"))){
								locName += ", (Rotation: " + rotation + ")";
							}
							
							if (chance != -1) locName += ", " +(Math.round((chance * 100) * 100.0) / 100.0) + "%";
							//						if(type != null) locName += ", " + type;
							
							if(dropsBuilder.toString().contains(locName)){
								continue;
							}
							
							if(dropsBuilder.toString().length() + locName.length() >= (MessageEmbed.VALUE_MAX_LENGTH - 20)){
								break;
							}
							
							dropsBuilder.append("`" + locName + "`\n");
						}
						
						builder.addField("Drops ", dropsBuilder.toString(), false);
					}
				}
			}
			
			ChatUtils.sendMessage(channel, builder.build());
		}
	}
}
