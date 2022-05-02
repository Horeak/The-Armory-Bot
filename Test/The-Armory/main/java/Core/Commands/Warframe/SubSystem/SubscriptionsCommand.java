package Core.Commands.Warframe.SubSystem;

import Core.CommandSystem.CommandObjects.SlashCommands.SlashCommandChannel;
import Core.CommandSystem.CommandObjects.SlashCommands.SlashCommandMessage;
import Core.Objects.Interfaces.Commands.ISlashCommand;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

@Deprecated
public class SubscriptionsCommand implements ISlashCommand
{
	@Override
	public String getSlashName()
	{
		return null;
	}
	
	@Override
	public void slashCommandExecuted(SlashCommandInteractionEvent slashEvent, Guild guild, User author, SlashCommandChannel channel, SlashCommandMessage message)
	{
	
	}
	/*
	@Override
	public String commandPrefix()
	{
		return "subscriptions";
	}
	
	public String getEmptyInfo(Guild guild, BotChannel channel, User author, Message message, String[] args)
	{
		return "You have not setup any subscriptions";
	}
	
	
	@Override
	public List<String> getTextObjects(
			Guild guild, BotChannel channel, User author, Message message, String[] args)
	{
		ArrayList<String> list = new ArrayList<>();
		
		String platform = PlatformCommand.getCurPlatform(this, "pc");
		
		if (!SubscribeCommand.subscribedWords.containsKey(platform) || !SubscribeCommand.subscribedWords.get(platform).containsKey(author.getIdLong())) {
			return list;
		}
		
		ArrayList<String> subs = new ArrayList<>(SubscribeCommand.subscribedWords.get(platform).get(author.getIdLong()));
		
		for (String t : subs) {
			String date = "N/A";
			
			if (SubscribeCommand.lastHit.containsKey(platform)) {
				if (SubscribeCommand.lastHit.get(platform).containsKey(t)) {
					CustomEntry<String, Long> ent = SubscribeCommand.lastHit.get(platform).get(t);
					Date dt = new Date(ent.getValue());
					
					if (dt != null) {
						String date1 = TimeParserUtil.getTime(dt);
						
						if (date1 != null && !date1.isEmpty()) {
							date = date1;
						}
					}
				}
			}
			
			list.add("- Key: **" + t + "**\n" + "\t- Last Match: **" + date + "**\n\n");
		}
		
		return list;
	}
	
	@Override
	public int getObjectsPerPage()
	{
		return 5;
	}
	
	 */
}
