package Core.Commands.Warframe.NotificationObjects.Commands;

import Core.CommandSystem.ChatUtils;
import Core.CommandSystem.CommandObjects.SlashCommands.SlashCommandChannel;
import Core.CommandSystem.CommandObjects.SlashCommands.SlashCommandMessage;
import Core.Commands.Warframe.NotificationObjects.NotificationSystem;
import Core.Commands.Warframe.Objects.InfoObject;
import Core.Commands.Warframe.Objects.PostObject;
import Core.Commands.Warframe.Warframe;
import Core.Objects.Annotation.Commands.SlashArgument;
import Core.Objects.Annotation.Commands.SubCommand;
import Core.Objects.Annotation.Debug;
import Core.Objects.Annotation.Method.Startup.Init;
import Core.Objects.Interfaces.Commands.ISlashCommand;
import Core.Util.ReflectionUtils;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.concurrent.CopyOnWriteArrayList;

//TODO If infoobject has activation date, queue the post until it is active
@Debug
@SubCommand( parent = Warframe.WarframeNotifications.class)
public class NotificationsCommand implements ISlashCommand
{
	public static CopyOnWriteArrayList<InfoObject> staticInfoObjects = new CopyOnWriteArrayList<>();
	
	@Init
	public static void init(){
		staticInfoObjects.addAll(ReflectionUtils.getSubTypes(InfoObject.class));
	}
	
	public static InfoObject getStaticInstance(String name){
		if(staticInfoObjects != null) {
			for (InfoObject object : staticInfoObjects) {
				if (object.getName().equalsIgnoreCase(name) || object.getType().equalsIgnoreCase(name)) {
					return object;
				}
			}
		}
		
		return null;
	}
	
	@Override
	public String getSlashName()
	{
		return "setup-notifications";
	}
	
	@SlashArgument( key = "mention", text = "Which mention to use for the notification, multiple mentions can be used with commas" )
	public String mention;
	
	@SlashArgument( key = "platform", text = "Which platform to use. Defaults to pc", choices = {"all", "pc", "xb1", "ps4", "switch"})
	public String platform = "pc";
	
	@SlashArgument( key = "type", text = "Which type of alert you want notifications for", required = true, choices = {"all", "alerts", "fissures", "invasions", "sortie"})
	public String type;
	
	@SlashArgument( key = "filter", text = "Filter what notifications you wish to see, multiple filters can be used with commas" )
	public String filter;
	
	@SlashArgument( key = "channel", text = "Which channel to post the notifications in", required = true)
	public TextChannel channel;
	
	@Override
	public void slashCommandExecuted(SlashCommandInteractionEvent slashEvent, Guild guild, User author, SlashCommandChannel channel, SlashCommandMessage message)
	{
		PostObject object = new PostObject(type, platform, channel.getIdLong(), mention != null ? new ArrayList<>(Arrays.asList(mention.split(","))) : new ArrayList(), filter != null ? new ArrayList<>(Arrays.asList(filter.split(","))): new ArrayList());
		NotificationSystem.postObjects.add(object);

		ChatUtils.sendEmbed(channel, "Great, i will now start posting notifications in " + channel.getAsMention() + " ");
	}

	@Override
	public EnumSet<Permission> getRequiredPermissions()
	{
		return EnumSet.of(Permission.ADMINISTRATOR);
	}
	
	@Override
	public String getDescription()
	{
		return "Setup a new warframe notification to get events posted automatically.";
	}
}