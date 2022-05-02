package Core.Commands.Generic.Status;

import Core.CommandSystem.ChatUtils;
import Core.CommandSystem.CommandObjects.SlashCommands.SlashCommandChannel;
import Core.CommandSystem.CommandObjects.SlashCommands.SlashCommandMessage;
import Core.Main.Startup;
import Core.Objects.Annotation.Commands.Command;
import Core.Objects.Annotation.Debug;
import Core.Objects.Interfaces.Commands.ISlashCommand;
import Core.Util.Utils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import java.text.SimpleDateFormat;

@Debug
@Command
public class BotStatusCommand implements ISlashCommand
{
	private static final SimpleDateFormat format = new SimpleDateFormat("dd/MM/YYYY");

	@Override
	public String getSlashName()
	{
		return "status";
	}
	
	@Override
	public void slashCommandExecuted(SlashCommandInteractionEvent slashEvent, Guild guild, User author, SlashCommandChannel channel, SlashCommandMessage message)
	{
		EmbedBuilder builder = new EmbedBuilder();
		
		builder.setTitle("Status");
		builder.setDescription("Current status of `" + Startup.discordClient.getSelfUser().getName() + "`");
		
		builder.addField("Version", Startup.getVersion(), true);
		if (Startup.getBuildDate() != null) {
			builder.addField("Version date", format.format(Startup.getBuildDate()), true);
		}
		
		builder.addField("Uptime", Utils.getUpTime(), true);
		builder.addField("Ping", Utils.getPing(message) + "ms", true);
		builder.addField("Gateway Ping", Startup.discordClient.getGatewayPing() + "ms", true);
		builder.addField("Rest Ping", Startup.discordClient.getRestPing().complete() + "ms", true);
		
		builder.addField("Servers", Long.toString(Startup.discordClient.getGuilds().size()), true);
		
		builder.setThumbnail(Startup.discordClient.getSelfUser().getAvatarUrl());
		
		ChatUtils.sendMessage(channel, author.getAsMention(), builder.build());
	}
}
