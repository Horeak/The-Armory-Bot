package Core.Commands.Warframe.NotificationObjects.Commands;

import Core.CommandSystem.ChatUtils;
import Core.CommandSystem.CommandObjects.SlashCommands.SlashCommandChannel;
import Core.CommandSystem.CommandObjects.SlashCommands.SlashCommandMessage;
import Core.Commands.Warframe.NotificationObjects.NotificationSystem;
import Core.Commands.Warframe.Objects.PostObject;
import Core.Commands.Warframe.Warframe;
import Core.Objects.Annotation.Commands.SlashArgument;
import Core.Objects.Annotation.Commands.SubCommand;
import Core.Objects.Interfaces.Commands.ISlashCommand;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import java.util.EnumSet;
import java.util.UUID;

@SubCommand( parent = Warframe.WarframeNotifications.class )
public class RemoveNotificationCommand implements ISlashCommand
{
	@SlashArgument( key = "id", text = "The notification id to remove", required = true)
	public String notfId;
	
	@Override
	public EnumSet<Permission> getRequiredPermissions()
	{
		return EnumSet.of(Permission.ADMINISTRATOR);
	}
	
	@Override
	public void slashCommandExecuted(SlashCommandInteractionEvent slashEvent, Guild guild, User author, SlashCommandChannel channel, SlashCommandMessage message)
	{
		for(PostObject object1 : NotificationSystem.postObjects){
			try {
				if (object1.id.equals(UUID.fromString(notfId))) {
					TextChannel tCh = message.getGuild().getTextChannelById(object1.channelId);
					
					if(tCh != null){
						NotificationSystem.postObjects.remove(object1);
						ChatUtils.sendEmbed(channel, author.getAsMention() + " The notification has now been removed!");
						break;
					}
				}
			}catch (IllegalArgumentException e){
				ChatUtils.sendEmbed(channel, author.getAsMention() + " Please input a valid id!" );
				return;
			}
		}
	}
	
	@Override
	public String getSlashName()
	{
		return "remove";
	}
	
	@Override
	public String getDescription()
	{
		return "Removes a specific warframe notification. The command requires the id of which notification you wish to disable.";
	}
}
