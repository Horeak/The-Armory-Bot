package Core.Commands.Warframe.SubSystem;

import Core.CommandSystem.ChatUtils;
import Core.CommandSystem.CommandObjects.SlashCommands.SlashCommandChannel;
import Core.CommandSystem.CommandObjects.SlashCommands.SlashCommandMessage;
import Core.Commands.Warframe.Objects.WikiItemObject;
import Core.Commands.Warframe.Warframe;
import Core.Commands.Warframe.WarframeWikiCommand;
import Core.Objects.Annotation.Commands.SlashArgument;
import Core.Objects.Annotation.Commands.SubCommand;
import Core.Objects.Annotation.Fields.DataObject;
import Core.Objects.CustomEntry;
import Core.Objects.Interfaces.Commands.ISlashCommand;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.StringJoiner;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@SubCommand( parent = Warframe.WarframeNotifications.class )
public class SubscribeCommand implements ISlashCommand
{
	@DataObject( file_path = "warframe/subscriptions.json", name = "subscribedWords" )
	public static ConcurrentHashMap<String, ConcurrentHashMap<Long, CopyOnWriteArrayList<String>>> subscribedWords = new ConcurrentHashMap<>();
	
	@DataObject( file_path = "warframe/subscriptions.json", name = "lastHit" )
	public static ConcurrentHashMap<String, ConcurrentHashMap<String, CustomEntry<String, Long>>> lastHit = new ConcurrentHashMap<>();
	
	@SlashArgument( key = "platform", text = "Which platform to check", choices = {"PC", "Playstation 4", "Xbox one", "Nintendo Switch"})
	public String platform = "PC";
	
	@SlashArgument( key = "search", text = "The search phrase or item you wish to subscribe to", required = true)
	public String key;
	
	@Override
	public String getSlashName()
	{
		return "subscribe";
	}
	
	@Override
	public void slashCommandExecuted(SlashCommandInteractionEvent slashEvent, Guild guild, User author, SlashCommandChannel channel, SlashCommandMessage message)
	{
		if (!subscribedWords.containsKey(platform)) {
			subscribedWords.put(platform, new ConcurrentHashMap<>());
		}
		
		if (!subscribedWords.get(platform).containsKey(author.getIdLong())) {
			subscribedWords.get(platform).put(author.getIdLong(), new CopyOnWriteArrayList<>());
		}
		
		CopyOnWriteArrayList<String> keys = new CopyOnWriteArrayList<>();
		WikiItemObject item = WarframeWikiCommand.getWikiItemObject(key);
		
		if (item != null) {
			boolean alerts = false;
			boolean hasData = false;
			
			for (Map.Entry<String, HashMap<String, String>> ent : item.info.entrySet()) {
				for (Map.Entry<String, String> ent1 : ent.getValue().entrySet()) {
					if (ent1.getKey().equalsIgnoreCase("source") || ent1.getKey().equalsIgnoreCase("location")) {
						hasData = true;
						
						if (ent1.getValue().toLowerCase().contains("alerts")) {
							alerts = true;
						}
					}
				}
				
				if (hasData && !alerts) {
					ChatUtils.sendEmbed(channel,
					                    author.getAsMention() + " `" + item.name + "` Is not obtainable from alerts!");
					return;
				}
			}
			keys.add(item.name.toLowerCase().replace(" ", "."));
		} else {
			keys.add(key.toLowerCase());
		}
		
		for (String tk : subscribedWords.get(platform).get(author.getIdLong())) {
			for (String tt : keys) {
				if (tt.isEmpty()) {
					continue;
				}
				if (tt.equalsIgnoreCase(tk)) {
					ChatUtils.sendEmbed(channel,
					                    author.getAsMention() + " You have already subscribed to `" + tt + "`");
					keys.remove(tt);
				}
			}
		}
		
		subscribedWords.get(platform).get(author.getIdLong()).addAll(keys);
		
		StringJoiner joiner = new StringJoiner(", ");
		keys.forEach((t) -> joiner.add("`" + t + "`"));
		
		if (keys.size() > 0) {
			ChatUtils.sendEmbed(channel,
			                    author.getAsMention() + " You have now subscribed to the following key words: " + joiner.toString());
		}
	}
}
