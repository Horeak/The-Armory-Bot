package Core.Commands.Warframe;

import Core.CommandSystem.ChatUtils;
import Core.CommandSystem.CommandObjects.SlashCommands.SlashCommandChannel;
import Core.CommandSystem.CommandObjects.SlashCommands.SlashCommandMessage;
import Core.Commands.Warframe.Objects.VoidTraderObject;
import Core.Objects.Annotation.Commands.SlashArgument;
import Core.Objects.Annotation.Commands.SubCommand;
import Core.Objects.Interfaces.Commands.ISlashCommand;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.StringJoiner;

@SubCommand( parent = Warframe.WarframeInfo.class )
public class WarframeVoidTrader implements ISlashCommand
{
	//TODO Make this look nicer aswell as maybe have a notification system for it?
	
	@Override
	public String getDescription()
	{
		return "Shows which wares the void trader is selling";
	}
	
	@SlashArgument(key = "platform", text = "Which platform to check", choices = {"PC", "Playstation 4", "Xbox one", "Nintendo Switch"})
	public String platform = "PC";
	
	@Override
	public String getSlashName()
	{
		return "voidtrader";
	}
	
	@Override
	public void slashCommandExecuted(SlashCommandInteractionEvent slashEvent, Guild guild, User author, SlashCommandChannel channel, SlashCommandMessage message)
	{
		JSONObject object = Warframe.getObject(platform, "voidTrader");
		EmbedBuilder builder = new EmbedBuilder();
		
		if (object != null) {
			String character = object.getString("character");
			String location = object.getString("location");
			
			boolean isActive = object.getBoolean("active");
			
			String start = object.getString("startString");
			String end = object.getString("endString");
			
			JSONArray items = object.getJSONArray("inventory");
			ArrayList<VoidTraderObject> traderObjects = new ArrayList<>();
			
			for (Object tt : items) {
				JSONObject in = (JSONObject)tt;
				
				String item = in.getString("item");
				int ducats = in.getInt("ducats");
				int credits = in.getInt("credits");
				
				traderObjects.add(new VoidTraderObject(item, ducats, credits));
			}
			
			ArrayList<StringJoiner> joiners = new ArrayList<>();
			int num = -1;
			
			
			for (VoidTraderObject ob : traderObjects) {
				String url = Warframe.getWikiLink(ob.item);
				String itemName = url == null ? ob.item : "[" + ob.item + "](" + url.replace(")", "\\)") + ")";
				String t = itemName + " - " + Warframe.credits_icon + " " + ob.credits + " | " + Warframe.ducats_icon + " " + ob.ducats;
				
				if (joiners.size() <= 0 || (joiners.get(
						num).length() + t.length()) >= (MessageEmbed.VALUE_MAX_LENGTH - 10)) {
					num++;
					joiners.add(new StringJoiner("\n"));
				}
				
				joiners.get(num).add(t);
			}
			
			builder.setTitle("Void trader, " + character);
			builder.setThumbnail("https://pbs.twimg.com/profile_images/630768883255889920/LmE_Tas-_400x400.jpg");
			
			if (isActive) {
				builder.addField("Location", location, true);
				builder.setDescription("*Ends in*: **" + end + "**");
				
				for (StringJoiner joiner : joiners) {
					builder.addField("Items", joiner.toString(), false);
				}
			} else {
				builder.setDescription("*Starts in*: **" + start + "**");
			}
			
			ChatUtils.sendMessage(channel, builder.build());
			return;
		}
		
		ChatUtils.sendEmbed(channel, author.getAsMention() + " Found no info!");
	}
}
