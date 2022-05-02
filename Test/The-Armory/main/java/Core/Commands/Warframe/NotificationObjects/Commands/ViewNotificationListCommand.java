package Core.Commands.Warframe.NotificationObjects.Commands;

import Core.CommandSystem.CommandObjects.SlashCommands.SlashCommandChannel;
import Core.CommandSystem.CommandObjects.SlashCommands.SlashCommandMessage;
import Core.Objects.Interfaces.Commands.ISlashCommand;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

@Deprecated
public class ViewNotificationListCommand implements ISlashCommand
{
	@Override
	public String getSlashName()
	{
		return null;
	}
	
	@Override
	public void slashCommandExecuted(SlashCommandInteractionEvent slashEvent, Guild guild, User author, SlashCommandChannel channel, SlashCommandMessage message)
	{
	
	}
	/*
	@PostInit
	public static void init(){
		if(NotificationsCommand.staticInfoObjects != null) {
			for (InfoObject ob : NotificationsCommand.staticInfoObjects) {
				DiscordCommand wCommand = CommandUtils.getBaseCommand(Warframe.class);
				ViewNotificationListCommand command = new ViewNotificationListCommand(ob.getName(), ob.getType());
				
				if (wCommand != null) {
					command.baseCommand = wCommand;
					wCommand.subCommands.add(command);
				}
				
				//				if (!CommandUtils.subCommands.contains(wCommand)) {
				//					CommandUtils.subCommands.add(wCommand);
				//				}
			}
			
			for (InfoObject ob : NotificationsCommand.staticInfoObjects) {
				List<PlatformCommand> list = ReflectionUtils.getSubTypes(PlatformCommand.class);
				
				for (PlatformCommand c : list) {
					DiscordCommand wCommand = CommandUtils.getBaseCommand(c.getClass());
					ViewNotificationListCommand command = new ViewNotificationListCommand(ob.getName(), ob.getType());
					
					if (wCommand != null) {
						command.baseCommand = wCommand;
						wCommand.subCommands.add(command);
					}
					
					//					if (!CommandUtils.subCommands.contains(wCommand)) {
					//						CommandUtils.subCommands.add(wCommand);
					//					}
				}
			}
		}
	}
	
	public String name;
	public String type;
	
	@Override
	public String commandPrefix()
	{
		return name;
	}
	
	public ViewNotificationListCommand(String name, String type)
	{
		this.name = name;
		this.type = type;
	}
	
	@Override
	public List<String> getTextObjects(
			Guild guild, BotChannel channel, User author, Message message, String[] args)
	{
		String platform = PlatformCommand.getCurPlatform(this, "pc");
		InfoObject staticObject = NotificationsCommand.getStaticInstance(type);
		ArrayList<InfoObject> objects = new ArrayList<>(NotificationSystem.getFilteredSubList(type, platform));
		
		if(staticObject != null){
			staticObject.sort(objects);
		}
		
		ArrayList<String> list = new ArrayList<>();
		
		for(InfoObject object : objects){
			list.add(object.getInfo());
		}
		
		return list;
	}
	
	@Override
	public String getTitle()
	{
		String platform = PlatformCommand.getFriendlyPlatformName(PlatformCommand.getCurPlatform(this, "pc"));
		return name + " for " + platform + ": ";
	}
	
	@Override
	public int getObjectsPerPage()
	{
		return 3;
	}
	
	 */
}
