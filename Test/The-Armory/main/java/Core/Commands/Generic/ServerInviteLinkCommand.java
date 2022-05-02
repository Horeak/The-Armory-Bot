package Core.Commands.Generic;

import Core.CommandSystem.ChatUtils;
import Core.CommandSystem.CommandObjects.SlashCommands.SlashCommandChannel;
import Core.CommandSystem.CommandObjects.SlashCommands.SlashCommandMessage;
import Core.CommandSystem.SlashMessageBuilder;
import Core.Main.Startup;
import Core.Objects.Annotation.Commands.Command;
import Core.Objects.Interfaces.Commands.ISlashCommand;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;

@Command
public class ServerInviteLinkCommand implements ISlashCommand
{
	@Override
	public String getDescription()
	{
		return "Invite link for the official bot server";
	}
	
	@Override
	public String getSlashName()
	{
		return "server_invite";
	}
	
	@Override
	public void slashCommandExecuted(SlashCommandInteractionEvent slashEvent, Guild guild, User author, SlashCommandChannel channel, SlashCommandMessage message)
	{
		EmbedBuilder builder = new EmbedBuilder();
		builder.setAuthor(Startup.discordClient.getSelfUser().getName(), null, null);
		builder.setThumbnail(Startup.discordClient.getSelfUser().getAvatarUrl());
		builder.setDescription("Here is the link to join the official bot server!\nFeel free to join to discuss anything from feature requests to issues with the bot.");
		
		SlashMessageBuilder slashBuilder = ChatUtils.createSlashMessage(author, channel);
		slashBuilder.withEmbed(builder);
		slashBuilder.addAction(Button.link(Startup.serverInviteLink, "Official server"));
		slashBuilder.send();
	}
}
