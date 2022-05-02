package Core.Commands.Generic.Reminder;

import Core.CommandSystem.ChatUtils;
import Core.CommandSystem.CommandObjects.SlashCommands.SlashCommandChannel;
import Core.CommandSystem.CommandObjects.SlashCommands.SlashCommandMessage;
import Core.Main.Startup;
import Core.Objects.Annotation.Commands.Command;
import Core.Objects.Annotation.Commands.SlashArgument;
import Core.Objects.Annotation.Fields.DataObject;
import Core.Objects.Annotation.Method.Startup.Init;
import Core.Objects.Interfaces.Commands.ISlashCommand;
import Core.Util.Time.TimeParserUtil;
import Core.Util.Utils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.utils.TimeFormat;

import java.time.Instant;
import java.util.Date;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

@Command
public class ReminderCommand implements ISlashCommand
{
	@DataObject( file_path = "reminders.json", name = "reminders", pretty = false )
	public static ConcurrentHashMap<UUID, remindObject> objects = new ConcurrentHashMap<>();
	public static ConcurrentHashMap<UUID, ScheduledFuture> tasks = new ConcurrentHashMap<>();
	
	@SlashArgument( key = "time", text = "When you want to be reminded.", required = true )
	public String timeArg;
	
	@SlashArgument( key = "text", text = "The message you want to be reminded about.", required = true )
	public String text;
	
	@Override
	public void slashCommandExecuted(SlashCommandInteractionEvent slashEvent, Guild guild, User author, SlashCommandChannel channel, SlashCommandMessage message)
	{
		long time = TimeParserUtil.getTime(timeArg);
		
		if(time == 0){
			ChatUtils.sendEmbed(channel, author.getAsMention() + " Please specify a time in which you want to be reminded in!");
			return;
		}
		
		String timeText = TimeFormat.RELATIVE.format(System.currentTimeMillis() + time);
		
		Long timeToExcecute = System.currentTimeMillis() + time;
		UUID id = UUID.randomUUID();
		remindObject object = new remindObject(System.currentTimeMillis(), timeToExcecute, text.trim(), author.getIdLong(), message.getIdLong(), channel.getIdLong(), id);
		objects.put(id, object);
		tasks.put(id, Startup.scheduledExecutor.schedule(() -> handle(object), time, TimeUnit.MILLISECONDS));
		
		ChatUtils.sendEmbed(channel, author.getAsMention() + " A reminder has now been set for " + (text != null ? "**" + text + "** " + timeText : timeText));
	}
	
	@Override
	public String getSlashName()
	{
		return "reminder";
	}
	
	@Override
	public String getDescription()
	{
		return "Allows setting up custom reminders";
	}
	
	
	@Init
	public static void init(){
		for(Entry<UUID, remindObject> objectEntry : objects.entrySet()){
			boolean done = objectEntry.getValue().interval == -1 && objectEntry.getValue().timeToRemind < System.currentTimeMillis();
			
			if(objectEntry.getValue().endTime != -1 && System.currentTimeMillis() >= objectEntry.getValue().endTime){
				objects.remove(objectEntry.getValue().id);
				return;
			}
			
			if(done){
				handle(objectEntry.getValue(), true);
				continue;
			}
			
			if(objectEntry.getValue().interval == -1) {
				tasks.put(objectEntry.getKey(), Startup.scheduledExecutor.schedule(() -> handle(objectEntry.getValue()), objectEntry.getValue().timeToRemind - System.currentTimeMillis(), TimeUnit.MILLISECONDS));
			}else{
				Long nextProc = (objectEntry.getValue().lastProc + objectEntry.getValue().interval);
				Long timeTil = nextProc - System.currentTimeMillis();
				
				tasks.put(objectEntry.getKey(), Startup.scheduledExecutor.scheduleAtFixedRate(() -> handle(objectEntry.getValue()), timeTil, objectEntry.getValue().interval, TimeUnit.MILLISECONDS));
			}
		}
	}
	
	public static void handle(remindObject ob){
		handle(ob, false);
		tasks.remove(ob.id);
	}
	
	public static void handle(remindObject ob, boolean late){
		if(ob.userId != null) {
			User user = Utils.getUser(ob.userId);
			PrivateChannel sendChannel = user.openPrivateChannel().complete();
			
			String timeSinceText = null;
			String origMessageLink = null;
			
			
			if(ob.channelId != null && ob.messageId != null) {
				TextChannel origChannel = Startup.discordClient.getTextChannelById(ob.channelId);
				
				if(origChannel != null) {
					Message origMessage = Utils.getMessage(origChannel, ob.messageId);
					
					if (origMessage != null) {
						Instant time = origMessage.getTimeCreated().toInstant();
						
						timeSinceText = TimeParserUtil.timeFormat.formatDuration(new Date(time.toEpochMilli()));
						origMessageLink = origMessage.getJumpUrl();
					}
				}
			}
			
			String timeAgoString = timeSinceText != null && !timeSinceText.isBlank() ? timeSinceText + " ago" : "";
			String aboutString = (ob.text != null && !ob.text.isBlank()) ? "about **" + ob.text.trim() + "**. " : "";
			
			if(ob.interval != -1){
				timeAgoString = " every " + TimeParserUtil.getTimeText(ob.interval);
			}
			
			EmbedBuilder builder = new EmbedBuilder();
			
			builder.setTitle("Here is your reminder!");
			builder.setDescription("You asked to be reminded " + (aboutString) + timeAgoString);
			
			if(late){
				builder.appendDescription("\n\n**The reminder was supposed to go off " + TimeParserUtil.getTime(ob.timeToRemind) + "**");
			}
			
			if(origMessageLink != null && !origMessageLink.isBlank()){
				builder.appendDescription("\n\n*Original message can be found [Here](" + origMessageLink + ")*");
			}
			
			ChatUtils.sendMessage(sendChannel, builder.build());
		}
		
		if(ob.interval != -1){
			while(ob.lastProc + ob.interval < System.currentTimeMillis()){
				ob.lastProc += ob.interval;
			}
		}else {
			ob.lastProc = System.currentTimeMillis();
		}
		
		
		if(ob.interval == -1 || ob.endTime != -1 && System.currentTimeMillis() > ob.endTime) {
			objects.remove(ob.id);
		}
	}
	

	public static class remindObject{
		public Long timeToRemind;
		
		public Long interval = -1L;
		public Long endTime = -1L;
		public Long lastProc = System.currentTimeMillis();
		
		public Long setAt = -1L;
		public String text;
		
		public Long userId;
		public Long messageId;
		public Long channelId;
		
		public UUID id;
		
		public remindObject(Long setAt, Long timeToRemind, String text, Long userId, Long messageId, Long channelId, UUID id)
		{
			this.setAt = setAt;
			this.timeToRemind = timeToRemind;
			this.text = text;
			this.userId = userId;
			this.messageId = messageId;
			this.channelId = channelId;
			this.id = id;
		}
	}
}
