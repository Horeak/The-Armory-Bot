package Core.Commands.Generic;

import Core.CommandSystem.ChatUtils;
import Core.CommandSystem.CommandObjects.SlashCommands.SlashCommandChannel;
import Core.CommandSystem.CommandObjects.SlashCommands.SlashCommandMessage;
import Core.CommandSystem.SlashMessageBuilder;
import Core.Main.BotListApi;
import Core.Main.Startup;
import Core.Objects.Annotation.Commands.Command;
import Core.Objects.Interfaces.Commands.ISlashCommand;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;

@Command
public class HelpCommand implements ISlashCommand
{
	@Override
	public String getSlashName()
	{
		return "help";
	}
	
	@Override
	public String getDescription()
	{
		return "Shows extended info about a specific command";
	}
	
	@Override
	public void slashCommandExecuted(SlashCommandInteractionEvent slashEvent, Guild guild, User author, SlashCommandChannel channel, SlashCommandMessage message)
	{
		EmbedBuilder builder = new EmbedBuilder();
		builder.setTitle("Help command");
		
		builder.appendDescription("\n\nIf you were looking for introduction to the bot it is recommended to read the top.gg post linked below.");
		builder.appendDescription("\nYou are also free to come join the official server if you have anything you wish to discuss from feature requests to bug reports.");
		
		SlashMessageBuilder slashBuilder = ChatUtils.createSlashMessage(author, channel);
		slashBuilder.withEmbed(builder);
		slashBuilder.addAction(Button.link(BotListApi.BOT_LINK, "Top.gg post"));
		slashBuilder.addAction(Button.link(Startup.serverInviteLink, "Official server"));
		slashBuilder.send();
	}
}
