package Core.Commands.Warframe;

import Core.CommandSystem.ChatUtils;
import Core.CommandSystem.CommandObjects.SlashCommands.SlashCommandChannel;
import Core.CommandSystem.CommandObjects.SlashCommands.SlashCommandMessage;
import Core.Main.Startup;
import Core.Objects.Annotation.Commands.CommandGroup;
import Core.Objects.Annotation.Commands.SlashArgument;
import Core.Objects.Annotation.Commands.SubCommand;
import Core.Objects.Annotation.Fields.DataObject;
import Core.Objects.Annotation.Method.Interval;
import Core.Objects.Interfaces.Commands.ISlashCommand;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.json.JSONObject;

import java.awt.Color;
import java.util.EnumSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@CommandGroup( parent = Warframe.class)
public class WarframeTime implements ISlashCommand
{
	//TODO Make this system nicer. Make the embed look nicer aswell
	
	@DataObject( file_path = "warframe/notifications.json", name = "lastCycle" )
	public static ConcurrentHashMap<String, String> lastId = new ConcurrentHashMap<>();
	
	@DataObject( file_path = "warframe/notifications.json", name = "timeChannels" )
	public static ConcurrentHashMap<String, CopyOnWriteArrayList<Long>> channels = new ConcurrentHashMap<>();
	
	@Interval(time_interval = 2)
	public static void update(){
		for (String platform : Warframe.getPlatforms()) {
			JSONObject ob1 = Warframe.getObject(platform, "cetusCycle");
			
			if(!ob1.has("id") || !ob1.has("shortString")) continue;
			
			String id = ob1.getString("id");
			String text = ob1.getString("shortString");
			
			if(text != null){
				text = text.strip();
			}
			
			if (lastId.containsKey(platform)) {
				if (lastId.get(platform).equalsIgnoreCase(id)) {
					continue;
				}
			}
			
			if (!text.startsWith("to")) {
				EmbedBuilder builder = new EmbedBuilder();
				boolean isDay = ob1.getBoolean("isDay");
				builder.setTitle("*It is now " + (isDay ? "Daytime" : "Nighttime") + " on cetus!*");
				builder.setDescription("*" + text + "*");
				builder.setColor(new Color(255, 100, 0));
				
				if (channels.containsKey(platform)) {
					for (Long t : channels.get(platform)) {
						MessageChannel channel = (MessageChannel)Startup.discordClient.getGuildChannelById(t);
						
						if (channel != null) {
							ChatUtils.sendMessage(channel, builder.build());
						}
					}
				}
			}
			
			lastId.put(platform, id);
		}
	}
	
	@Override
	public String getSlashName()
	{
		return "time";
	}
	
	@Override
	public void slashCommandExecuted(SlashCommandInteractionEvent slashEvent, Guild guild, User author, SlashCommandChannel channel, SlashCommandMessage message)
	{
	
	}
	@SubCommand(parent = WarframeTime.class)
	public static class currentTime implements ISlashCommand{
		
		@Override
		public String getSlashName()
		{
			return "current";
		}
		
		@SlashArgument( key = "platform", text = "Which platform to check", choices = {"PC", "Playstation 4", "Xbox one", "Nintendo Switch"})
		public String platform = "PC";
		
		@Override
		public void slashCommandExecuted(SlashCommandInteractionEvent slashEvent, Guild guild, User author, SlashCommandChannel channel, SlashCommandMessage message)
		{
			EmbedBuilder builder = new EmbedBuilder();
			JSONObject ob1 = Warframe.getObject(platform, "cetusCycle");
			
			boolean isDay = ob1.getBoolean("isDay");
			String text = ob1.getString("shortString");
			
			builder.setTitle("Cetus time");
			builder.setDescription("*Currently: " + (isDay ? "**Day**" : "**Night**") + "*\n\n\n*" + text + "*");
			builder.setColor(new Color(255, 100, 0));
			builder.setThumbnail("https://vignette.wikia.nocookie.net/warframe/images/3/32/OstronSyndicateFlag.png");
			
			ChatUtils.sendMessage(channel, builder.build());
		}
	}
	
	@SubCommand(parent = WarframeTime.class)
	public static class addChannel implements ISlashCommand
	{
		@SlashArgument( key = "platform", text = "Which platform to use", choices = {"PC", "Playstation 4", "Xbox one", "Nintendo Switch"})
		public String platform = "PC";
		
		@SlashArgument(key = "channel", text = "Which channel you want the time update to be posted", required = true)
		public TextChannel targetChannel;
		
		@Override
		public EnumSet<Permission> getRequiredPermissions()
		{
			return EnumSet.of(Permission.ADMINISTRATOR);
		}
		
		@Override
		public void slashCommandExecuted(SlashCommandInteractionEvent slashEvent, Guild guild, User author, SlashCommandChannel channel, SlashCommandMessage message)
		{
			if (!channels.containsKey(platform)) {
				channels.put(platform, new CopyOnWriteArrayList<>());
			}
			
			channels.get(platform).add(targetChannel.getIdLong());
			ChatUtils.sendEmbed(channel,channel.getAsMention() + " will now receive the cetus time alerts!");
		}
		
		@Override
		public String getSlashName()
		{
			return "add-channel";
		}
	}
	
	@SubCommand(parent = WarframeTime.class)
	public static class removeChannel implements ISlashCommand
	{
		@SlashArgument( key = "platform", text = "Which platform to use", choices = {"PC", "Playstation 4", "Xbox one", "Nintendo Switch"})
		public String platform = "PC";
		
		@SlashArgument(key = "channel", text = "Which channel you want to remove the time updates from", required = true)
		public TextChannel targetChannel;
		
		@Override
		public EnumSet<Permission> getRequiredPermissions()
		{
			return EnumSet.of(Permission.ADMINISTRATOR);
		}
		
		@Override
		public void slashCommandExecuted(SlashCommandInteractionEvent slashEvent, Guild guild, User author, SlashCommandChannel channel, SlashCommandMessage message)
		{
			if (!channels.containsKey(platform)) {
				ChatUtils.sendEmbed(channel, "Found no channels to disable cetus alerts for!");
				return;
			}
			
			channels.get(platform).remove(targetChannel.getIdLong());
			ChatUtils.sendEmbed(channel, targetChannel.getAsMention() + " will no longer post the cetus time alerts!");
		}
		
		@Override
		public String getSlashName()
		{
			return "remove-channel";
		}
	}
	
	
}
