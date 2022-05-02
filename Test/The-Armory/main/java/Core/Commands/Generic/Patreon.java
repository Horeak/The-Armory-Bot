package Core.Commands.Generic;

import Core.CommandSystem.ChatUtils;
import Core.CommandSystem.CommandObjects.SlashCommands.SlashCommandChannel;
import Core.CommandSystem.CommandObjects.SlashCommands.SlashCommandMessage;
import Core.CommandSystem.SlashMessageBuilder;
import Core.Main.Startup;
import Core.Objects.Annotation.Commands.Command;
import Core.Objects.Annotation.Debug;
import Core.Objects.Interfaces.Commands.ISlashCommand;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;

@Debug
@Command
public class Patreon  implements ISlashCommand
{
	public static final String PATREON_URL = "https://www.patreon.com/HoreakArmory";
	
	@Override
	public String getDescription()
	{
		return "Patreon link if you wish to help support the bot";
	}

	@Override
	public String getSlashName()
	{
		return "patreon";
	}
	
	@Override
	public void slashCommandExecuted(SlashCommandInteractionEvent slashEvent, Guild guild, User author, SlashCommandChannel channel, SlashCommandMessage message)
	{
		EmbedBuilder builder = new EmbedBuilder();
		builder.setAuthor(Startup.discordClient.getSelfUser().getName(), null, null);
		builder.setThumbnail(Startup.discordClient.getSelfUser().getAvatarUrl());
		
		builder.setDescription("Here is the link to the Patreon for the bot! Remember that support is not required for any features of the bot but greatly helps in its development.\n\nYou can also come join the official server to discuss anything from feature requests to issues with the bot.");
		
		SlashMessageBuilder slashBuilder = ChatUtils.createSlashMessage(author, channel);
		slashBuilder.withEmbed(builder);
		slashBuilder.addAction(Button.link(PATREON_URL, "Patreon"));
		slashBuilder.addAction(Button.link(Startup.serverInviteLink, "Official server"));
		slashBuilder.send();
	}
}
