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
public class InviteLinkCommand implements ISlashCommand
{
	@Override
	public String getDescription()
	{
		return "Gets the bot invite link";
	}
	
	@Override
	public String getSlashName()
	{
		return "invite";
	}
	
	@Override
	public void slashCommandExecuted(SlashCommandInteractionEvent slashEvent, Guild guild, User author, SlashCommandChannel channel, SlashCommandMessage message)
	{
		EmbedBuilder builder = new EmbedBuilder();
		builder.setAuthor(Startup.discordClient.getSelfUser().getName(), null, null);
		builder.setThumbnail(Startup.discordClient.getSelfUser().getAvatarUrl());
		
		if (Startup.botInviteLink == null || Startup.botInviteLink.isEmpty()) {
			Startup.botInviteLink = Startup.discordClient.getInviteUrl(Startup.BOT_PERMISSIONS);
		}
		
		builder.setDescription("Here is the link to invite the bot to a new server! Remember to take a look at the top.gg post for help using the bot!\n\nYou can also come join the official server to discuss anything from feature requests to issues with the bot.");
		
		SlashMessageBuilder slashBuilder = ChatUtils.createSlashMessage(author, channel);
		slashBuilder.withEmbed(builder);
		slashBuilder.addAction(Button.link(Startup.botInviteLink, "Add the bot"));
		slashBuilder.addAction(Button.link(BotListApi.BOT_LINK, "Top.gg post"));
		slashBuilder.addAction(Button.link(Startup.serverInviteLink, "Official server"));
		slashBuilder.send();
	}
}
