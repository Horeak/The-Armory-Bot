package Core.Commands.Voice.Commands.StartStop;

import Core.CommandSystem.ChatUtils;
import Core.CommandSystem.CommandObjects.SlashCommands.SlashCommandChannel;
import Core.CommandSystem.CommandObjects.SlashCommands.SlashCommandMessage;
import Core.Commands.Voice.MusicCommand;
import Core.Commands.Voice.MusicCommand.MusicInfoCommand;
import Core.Objects.Annotation.Commands.SubCommand;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

@SubCommand( parent = MusicInfoCommand.class )
public class Resume extends MusicCommand
{
	@Override
	public String getDescription()
	{
		return "Resumes the currently paused music queue";
	}
	
	@Override
	public String getSlashName()
	{
		return "resume";
	}
	
	@Override
	public void slashCommandExecuted(SlashCommandInteractionEvent slashEvent, Guild guild, User author, SlashCommandChannel channel, SlashCommandMessage message)
	{
		if (!MusicCommand.getGuildAudioPlayer(guild).getPlayer().isPaused()) {
			ChatUtils.sendEmbed(channel, author.getAsMention() + " Music is not paused!");
			return;
		}
		
		MusicCommand.getGuildAudioPlayer(guild).getPlayer().setPaused(false);
		ChatUtils.sendEmbed(channel, author.getAsMention() + " Music is now resumed!");
	}
}
