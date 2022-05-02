package Core.Commands.Warframe.SubSystem;

import Core.CommandSystem.ChatUtils;
import Core.CommandSystem.CommandObjects.SlashCommands.SlashCommandChannel;
import Core.CommandSystem.CommandObjects.SlashCommands.SlashCommandMessage;
import Core.Commands.Warframe.Warframe;
import Core.Objects.Annotation.Commands.SlashArgument;
import Core.Objects.Annotation.Commands.SubCommand;
import Core.Objects.Interfaces.Commands.ISlashCommand;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

@SubCommand(parent = Warframe.WarframeNotifications.class )
public class UnSubscribeCommand implements ISlashCommand
{
	@Override
	public String getSlashName()
	{
		return "unsubscribe";
	}
	
	@SlashArgument( key = "platform", text = "Which platform to check", choices = {"PC", "Playstation 4", "Xbox one", "Nintendo Switch"})
	public String platform = "PC";
	
	@SlashArgument( key = "search", text = "The search text subscription you wish to remove", required = true)
	public String key;
	
	@Override
	public void slashCommandExecuted(SlashCommandInteractionEvent slashEvent, Guild guild, User author, SlashCommandChannel channel, SlashCommandMessage message)
	{
		if (!SubscribeCommand.subscribedWords.get(platform).containsKey(author.getIdLong())) {
			ChatUtils.sendEmbed(channel, author.getAsMention() + " You have no subscriptions to remove!");
			return;
		}
		
		if (!SubscribeCommand.subscribedWords.get(platform).get(author.getIdLong()).contains(key)) {
			ChatUtils.sendEmbed(channel, author.getAsMention() + " You have not subscribed to `" + key + "`");
			return;
		}
		
		SubscribeCommand.subscribedWords.get(platform).get(author.getIdLong()).remove(key);
		ChatUtils.sendEmbed(channel,
		                    author.getAsMention() + " You have now unsubscribed from the following key words: `" + key + "`");
	}
}
