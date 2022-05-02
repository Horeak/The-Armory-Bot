package Core.Commands.Voice.Commands;

import Core.CommandSystem.ChatUtils;
import Core.CommandSystem.CommandObjects.SlashCommands.SlashCommandChannel;
import Core.CommandSystem.CommandObjects.SlashCommands.SlashCommandMessage;
import Core.Commands.Voice.MusicCommand;
import Core.Objects.Annotation.Commands.Command;
import Core.Objects.Annotation.Commands.SlashArgument;
import Core.Objects.Annotation.Commands.SubCommand;
import Core.Objects.Interfaces.Commands.ISlashCommand;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import java.util.EnumSet;

@Command
public class MusicChannel extends MusicCommand
{
	@Override
	public String getSlashName()
	{
		return "music-channel";
	}
	@Override
	public void slashCommandExecuted(SlashCommandInteractionEvent slashEvent, Guild guild, User author, SlashCommandChannel channel, SlashCommandMessage message) { }
	
	@Override
	public EnumSet<Permission> getRequiredPermissions()
	{
		return EnumSet.of(Permission.ADMINISTRATOR);
	}
	
	@SubCommand( parent = MusicChannel.class)
	public static class addMusicChannel implements ISlashCommand{
		
		@SlashArgument( key = "channel", text = "Which channel you want to use for music notifications", required = true )
		public TextChannel targetMusicChannel;
		
		@Override
		public String getDescription()
		{
			return "Change what channel to post the currently playing message";
		}
		
		@Override
		public String getSlashName()
		{
			return "add";
		}
		
		@Override
		public void slashCommandExecuted(SlashCommandInteractionEvent slashEvent, Guild guild, User author, SlashCommandChannel channel, SlashCommandMessage message)
		{
			MusicCommand.musicChannel.put(guild.getIdLong(), targetMusicChannel.getIdLong());
			ChatUtils.sendEmbed(channel, channel.getAsMention() + " has now been set as the music channel for this server!");
		}
		
		@Override
		public EnumSet<Permission> getRequiredPermissions()
		{
			return EnumSet.of(Permission.ADMINISTRATOR);
		}
	}
	
	@SubCommand( parent = MusicChannel.class)
	public static class clearMusicChannel implements ISlashCommand{
		
		@Override
		public String getSlashName()
		{
			return "clear";
		}
		
		@Override
		public String getDescription()
		{
			return "Remove the currently set music notification channel";
		}
		
		@Override
		public void slashCommandExecuted(SlashCommandInteractionEvent slashEvent, Guild guild, User author, SlashCommandChannel channel, SlashCommandMessage message)
		{
			MusicCommand.musicChannel.remove(guild.getIdLong());
			ChatUtils.sendEmbed(channel, author.getAsMention() + " Music channel has now been reset!");
		}
		
		@Override
		public EnumSet<Permission> getRequiredPermissions()
		{
			return EnumSet.of(Permission.ADMINISTRATOR);
		}
	}
}
