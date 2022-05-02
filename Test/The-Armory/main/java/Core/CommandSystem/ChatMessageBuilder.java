package Core.CommandSystem;

import Core.CommandSystem.CommandObjects.SlashCommands.SlashCommandChannel;
import Core.Main.Startup;
import Core.Objects.BotChannel;
import Core.Objects.CustomEntry;
import Core.Objects.Interfaces.MessageRunnable;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.ItemComponent;
import net.dv8tion.jda.api.requests.restaction.MessageAction;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;

public class ChatMessageBuilder
{
	protected User author;
	protected Guild guild;
	protected BotChannel channel;
	private MessageEmbed embed;
	private CustomEntry<File, String> attachment;
	private String content;
	
	private boolean singleRow = true;
	private ArrayList<ActionRow> actionRows = new ArrayList<>();
	private ArrayList<MessageRunnable> runnables = new ArrayList<>();
	
	public ChatMessageBuilder(User author, BotChannel channel)
	{
		this.author = author;
		this.guild = channel.getGuild();
		this.channel = channel;
	}
	
	public ChatMessageBuilder withEmbed(EmbedBuilder embed)
	{
		this.embed = embed.build();
		return this;
	}
	
	public ChatMessageBuilder withEmbed(MessageEmbed embed)
	{
		this.embed = embed;
		return this;
	}
	
	public ChatMessageBuilder withEmbed(String content){
		this.embed = ChatUtils.makeEmbed(author, guild, channel, content).build();
		return this;
	}
	
	public ChatMessageBuilder withContent(String content)
	{
		this.content = content;
		return this;
	}
	
	public ChatMessageBuilder withRunnables(MessageRunnable... runnables){
		this.runnables.addAll(Arrays.asList(runnables));
		return this;
	}
	
	public ChatMessageBuilder addRunnable(MessageRunnable runnable){
		runnables.add(runnable);
		return this;
	}
	
	public ChatMessageBuilder setMultiRow()
	{
		this.singleRow = false;
		return this;
	}
	
	public ChatMessageBuilder withActions(ArrayList<ItemComponent> actions)
	{
		if (singleRow) {
			actionRows.add(ActionRow.of(actions));
		} else {
			for (ItemComponent act : actions) {
				actionRows.add(ActionRow.of(act));
			}
		}
		
		return this;
	}
	
	public ChatMessageBuilder addAction(ItemComponent row)
	{
		if (!singleRow || actionRows.size() <= 0) {
			actionRows.add(ActionRow.of(row));
		} else {
			if (actionRows.size() > 0) {
				actionRows.get(0).getComponents().add(row);
			}
		}
		return this;
	}
	
	public ChatMessageBuilder withActionRows(ArrayList<ActionRow> rows)
	{
		actionRows = rows;
		return this;
	}
	
	public ChatMessageBuilder addActionRow(ActionRow row)
	{
		actionRows.add(row);
		return this;
	}
	
	public ChatMessageBuilder withAttachment(File file, String fileName)
	{
		attachment = new CustomEntry<>(file, fileName);
		return this;
	}
	
	public ArrayList<ActionRow> getActionRows()
	{
		return actionRows;
	}
	
	public User getAuthor()
	{
		return author;
	}
	
	public Guild getGuild()
	{
		return guild;
	}
	
	public BotChannel getChannel()
	{
		return channel;
	}
	
	public MessageEmbed getEmbed()
	{
		return embed;
	}
	
	public CustomEntry<File, String> getAttachment()
	{
		return attachment;
	}
	
	public String getContent()
	{
		return content;
	}
	
	public boolean isSingleRow()
	{
		return singleRow;
	}
	
	public void send()
	{
		if (!PermissionsUtils.botHasPermission(channel, EnumSet.of(Permission.MESSAGE_SEND))
		    || (embed != null && !PermissionsUtils.botHasPermission(channel, EnumSet.of(Permission.MESSAGE_EMBED_LINKS)))
		       || (attachment != null && !PermissionsUtils.botHasPermission(channel, EnumSet.of(Permission.MESSAGE_ATTACH_FILES)))) {
			
			if(author != null && author != Startup.discordClient.getSelfUser()) {
				author.openPrivateChannel().queue((ch) -> {
					if(ch != null) handle();
				});
			}
			
			return;
		}
		
		handle();
	}
	
	
	protected void prepare(){
		if(getEmbed() != null) {
			MessageEmbed object = getEmbed();
			
			if (object.getColor() == null || object.getFooter() == null || object.getFooter().getText() == null || object.getFooter().getText().isBlank()) {
				User author = getAuthor();
				Guild guild = getGuild();
				
				if(author == null || guild == null){
					if(channel instanceof SlashCommandChannel) {
						SlashCommandChannel ch = (SlashCommandChannel)channel;
						
						if (author == null) {
							author = ch.event.getUser();
						}
						
						if (guild == null) {
							guild = ch.event.getGuild();
						}
						
					}
				}
				
				EmbedBuilder builder = new EmbedBuilder(object);
				
				if (object.getColor() == null) builder.setColor(ChatUtils.getEmbedColor(guild, author));
				
				if (object.getFooter() == null || object.getFooter().getText() == null || object.getFooter().getText().isBlank()) {
					ChatUtils.setFooter(builder, author, channel);
				}
				
				withEmbed(builder);
			}
		}
	}
	
	protected void handle()
	{
		prepare();
		
		MessageAction action = content != null ? channel.sendMessage(content) : embed != null ? channel.sendMessageEmbeds(embed) : null;
		
		if (action != null) {
			if (content != null && embed != null) {
				action.setEmbeds(embed);
			}
			
			if(actionRows != null && actionRows.size() > 0){
				action.setActionRows(actionRows);
			}
			
			if (attachment != null && attachment.getKey() != null && attachment.getValue() != null) {
				action.addFile(attachment.getKey(), attachment.getValue());
			}
			
			action.queue((mes) -> {
				if (runnables != null) {
					for (MessageRunnable runnable1 : runnables) {
						runnable1.run(mes, null);
					}
				}
			}, (T) -> {
				if (runnables != null) {
					for (MessageRunnable runnable1 : runnables) {
						runnable1.run(null, T);
					}
				}
			});
		}
	}
}
