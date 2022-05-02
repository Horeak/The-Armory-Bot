package Core.Commands.Warframe.NotificationObjects.Commands;

import Core.CommandSystem.CommandObjects.SlashCommands.SlashCommandChannel;
import Core.CommandSystem.CommandObjects.SlashCommands.SlashCommandMessage;
import Core.Objects.Interfaces.Commands.ISlashCommand;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

@Deprecated
public class ListNotificationsCommand implements ISlashCommand
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
	@Override
	public String commandPrefix()
	{
		return "list";
	}
	
	@Override
	public EnumSet<Permission> getRequiredPermissions()
	{
		return EnumSet.of(Permission.ADMINISTRATOR);
	}
	
	@Override
	public String getDescription(DiscordCommand sourceCommand, Message callerMessage)
	{
		return "Shows a list of all warframe notifications setup for the current server.";
	}
	
	@Override
	public String getShortDescription(DiscordCommand sourceCommand, Message callerMessage)
	{
		return "Shows a list of all warframe notifications.";
	}
	
	@Override
	public List<String> getTextObjects(
			Guild guild, BotChannel channel, User author, Message message, String[] args)
	{
		ArrayList<PostObject> object = new ArrayList<>();
		ArrayList<String> list = new ArrayList<>();
		
		for(PostObject object1 : NotificationSystem.postObjects){
			TextChannel tCh = message.getGuild().getTextChannelById(object1.channelId);
			
			if(tCh != null){
				object.add(object1);
			}
		}
		
		for(PostObject post : object){
			StringJoiner builder = new StringJoiner("\n");
			TextChannel tCh = message.getGuild().getTextChannelById(post.channelId);
			
			StringJoiner filters = new StringJoiner(", ");
			if(post.filters != null && post.filters.size() > 0) post.filters.forEach((s) -> filters.add("`" + s + "`"));
			
			builder.add("ID: `" + post.id + "`");
			builder.add("Channel: " + tCh.getAsMention());
			builder.add("Type: **" + post.postType + "**");
			builder.add("Platform: **" + post.platform + "**");
			if(post.mentions != null && post.mentions.size() > 0 && !String.join(", ", post.mentions).isBlank()) builder.add("Mentions: **" + String.join(", ", post.mentions) + "**");
			if(filters.toString() != null && !filters.toString().isBlank()) builder.add("Filters: **" + filters.toString() + "**");
			
			list.add(builder.toString());
		}
		
		return list;
	}
	
	@Override
	public int getObjectsPerPage()
	{
		return 5;
	}
	
	@Override
	public boolean canBeUsedPrivate()
	{
		return false;
	}
	 */
}
