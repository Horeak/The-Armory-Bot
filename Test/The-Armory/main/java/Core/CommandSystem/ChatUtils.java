package Core.CommandSystem;

import Core.CommandSystem.CommandObjects.SlashCommands.SlashCommandChannel;
import Core.Main.BotListApi;
import Core.Main.Logging;
import Core.Main.Startup;
import Core.Objects.BotChannel;
import Core.Objects.Interfaces.MessageDeletedRunnable;
import Core.Objects.Interfaces.MessageRunnable;
import Core.Util.Utils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.requests.restaction.MessageAction;
import org.discordbots.api.client.entity.VotingMultiplier;

import java.awt.Color;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.Random;

@SuppressWarnings( {"unused", "SameParameterValue"} )
public class ChatUtils
{
	private static final Random RANDOM = new Random();
	
	public static void setFooter(EmbedBuilder builder, User author, MessageChannel channel){
		if(RANDOM.nextBoolean()){
			VotingMultiplier votingMultiplier = null;
			boolean hasVoted = false;
			
			if (author != null) {
				//TODO This will slow down the command threads.
				if (BotListApi.VOTING_ENABLED) {
					try {
						votingMultiplier = BotListApi.api.getVotingMultiplier().toCompletableFuture().get();
						hasVoted = BotListApi.api.hasVoted(author.getId()).toCompletableFuture().get();
					} catch (Exception e) {
						Logging.exception(e);
					}
				}
			}
			
			String text = BotListApi.getVotingText(hasVoted, votingMultiplier);
			
			if (text != null && !text.isBlank()) {
				builder.setFooter(text);
			}
		}else{
			TextChannel ch = Startup.discordClient.getTextChannelById(channel.getIdLong());
			ArrayList<String> text = new ArrayList<>();

			if(ch != null){
				text.add("Found an issue? Use \"/bugreport\"");
			}
			
			text.add("Have a suggestion or issue? " + Startup.serverInviteLink);
			
			if(builder.getDescriptionBuilder() != null && builder.getDescriptionBuilder().toString().length() > 0) {
				text.removeIf((s) -> s.length() > builder.getDescriptionBuilder().toString().length());
			}
			
			//TODO Add more random messages. Similar to the status or maybe even replace the currently playing status with this
			//TODO Weight different text options so that voting is higher chance specially if it is weekend
			
			if(text.size() > 0) {
				builder.setFooter(text.get(RANDOM.nextInt(text.size())));
			}
		}
		
	}
	
	
	//TODO Add functions to add and remove reactions to a message
	
	/* Edit messages */
	public static void editMessage(Message messageId, String title, MessageEmbed object, MessageRunnable... runnable){
		MessageAction action = null;
		
		if(messageId == null) return;
		
		if(title != null && !title.isBlank()){
			action = messageId.editMessage(title);
		}
		
		if(action == null && object != null){
			action = messageId.editMessageEmbeds(object);
		}
		
		if(action == null){
			System.err.println("Unable to edit message. No title or embed!");
			return;
		}
		
		if(messageId.getChannel() != null) {
			action.queue((mes) -> {
				if (runnable != null) {
					for (MessageRunnable runnable1 : runnable) {
						runnable1.run(mes, null);
					}
				}
			}, (T) -> {
				if (runnable != null) {
					for (MessageRunnable runnable1 : runnable) {
						runnable1.run(null, T);
					}
				}
			});
		}
	}
	
	public static void editMessage(Message messageId, String message, MessageRunnable... runnable) {
		editMessage(messageId, message, null, runnable);
	}
	
	public static void editMessage(Message messageId, MessageEmbed object, MessageRunnable... runnable) {
		editMessage(messageId, null, object, runnable);
	}
	
	/* Send Message */
	public static void sendMessage(MessageChannel channel, String title, MessageEmbed object, MessageRunnable... runnable)
	{
		ChatMessageBuilder builder = getCorrectBuilder(null, getChannel(channel));
		builder.withContent(title);
		builder.withEmbed(object);
		builder.withRunnables(runnable);
		builder.send();
	}
	
	public static void sendMessage(MessageChannel channel, String title, MessageRunnable... runnable)
	{
		ChatMessageBuilder builder = getCorrectBuilder(null, getChannel(channel));
		builder.withContent(title);
		builder.withRunnables(runnable);
		builder.send();
	}
	
	public static void sendMessage(MessageChannel channel, MessageEmbed object, MessageRunnable... runnable)
	{
		ChatMessageBuilder builder = getCorrectBuilder(null, getChannel(channel));
		builder.withEmbed(object);
		builder.withRunnables(runnable);
		builder.send();
	}
	
	public static void sendEmbed(MessageChannel chat, String message, MessageRunnable... runnable) {
		ChatMessageBuilder builder = getCorrectBuilder(null, getChannel(chat));
		builder.withEmbed(message);
		builder.withRunnables(runnable);
		builder.send();
	}
	
	private static BotChannel getChannel(MessageChannel channel){
		if(channel instanceof BotChannel) return (BotChannel)channel;
		
		return new BotChannel(channel);
	}
	
	public static void deleteMessage(Message mes, MessageDeletedRunnable... runnable){
		if(mes == null || mes.getGuild() == null) return;
		
		if(PermissionsUtils.botHasPermission(mes.getGuild(), EnumSet.of(Permission.MESSAGE_MANAGE))){
			//TODO This gives a Unknown message error
			mes.delete().queue((T) -> {
				if(runnable != null){
					for(MessageDeletedRunnable runnable1 : runnable){
						runnable1.run(true, null);
					}
				}
			}, (T) -> {
				if(runnable != null){
					for(MessageDeletedRunnable runnable1 : runnable){
						runnable1.run(false, T);
					}
				}
			});
		}else{
			System.err.println("The bot was unable to delete a message because missing permissions!");
		}
	}
	
	public static VoiceChannel getConnectedBotChannel(Guild guild)
	{
		GuildVoiceState state = guild.getSelfMember().getVoiceState();
		
		if (state != null) {
			if (state.getChannel() != null) {
				return (VoiceChannel)state.getChannel();
			}
		}
		
		return null;
	}
	
	public static VoiceChannel getVoiceChannelFromUser(User user, Guild guild)
	{
		Member mem = Utils.getMember(guild, user);
		
		if(mem != null) {
			GuildVoiceState state = mem.getVoiceState();
		
			if (state != null && state.getChannel() != null) {
				return (VoiceChannel)state.getChannel();
			}
		}
		return null;
	}
	
	public static boolean isPrivate(MessageChannel channel)
	{
		if(channel == null || channel.getType() == null) return false;
		
		return (channel.getType() == ChannelType.PRIVATE || channel.getType() == ChannelType.GROUP);
	}
	
	public static final Color DEFAULT_EMBED_COLOR = new Color(13, 129, 104);
	
	public static Color getEmbedColor(Guild guild, User author){
		Member member = Utils.getMember(guild, author);
		return getEmbedColor(member);
	}
	
	public static Color getEmbedColor(Member member)
	{
		Color c = DEFAULT_EMBED_COLOR;
		
		if(member != null && member.getColor() != null) {
			c = member.getColor();
		}
		
		return c;
	}
	
	public static void setEmbedColor(Guild guild, User author, EmbedBuilder builder)
	{
		if(guild == null || author == null || builder == null) return;
		Member member = Utils.getMember(guild, author);
		setEmbedColor(member, builder);
	}
	
	public static void setEmbedColor(Member member, EmbedBuilder builder)
	{
		builder.setColor(getEmbedColor(member));
	}
	
	public static EmbedBuilder makeEmbed(User user, Guild guild, BotChannel channel, String text){
		EmbedBuilder builder = new EmbedBuilder().setDescription(text);
		setEmbedColor(guild, user, builder);
		setFooter(builder, user, channel);
		
		return builder;
	}
	
	public static ChatMessageBuilder getCorrectBuilder(User user, MessageChannel channel){
		if(user == null){
			if(channel instanceof SlashCommandChannel) {
				SlashCommandChannel ch = (SlashCommandChannel)channel;
				user = ch.event.getUser();
			}
		}
		
		channel = getChannel(channel);
		
		if(channel instanceof SlashCommandChannel){
			return createSlashMessage(user, channel);
		}
		
		return createMessage(user, channel);
	}
	
	public static ChatMessageBuilder createMessage(User user, MessageChannel channel){
		ChatMessageBuilder builder = new ChatMessageBuilder(user, getChannel(channel));
		return builder;
	}
	
	public static SlashMessageBuilder createSlashMessage(User user, MessageChannel channel){
		SlashMessageBuilder builder = new SlashMessageBuilder(user, getChannel(channel));
		return builder;
	}
}


