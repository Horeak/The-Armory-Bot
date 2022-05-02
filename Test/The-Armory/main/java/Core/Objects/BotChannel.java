package Core.Objects;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.requests.RestAction;
import net.dv8tion.jda.api.requests.restaction.MessageAction;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;

public class BotChannel implements MessageChannel
{
	private final MessageChannel channel;
	
	public BotChannel(MessageChannel channel)
	{
		this.channel = channel;
	}
	
	@Override
	public long getLatestMessageIdLong()
	{
		return channel.getLatestMessageIdLong();
	}
	
	@Override
	public boolean canTalk()
	{
		return channel != null && channel.canTalk();
	}
	
	@Nonnull
	@Override
	public String getName()
	{
		return channel.getName();
	}
	
	@Nonnull
	@Override
	public ChannelType getType()
	{
		return channel.getType();
	}
	
	@Nonnull
	@Override
	public JDA getJDA()
	{
		return channel.getJDA();
	}
	
	@NotNull
	@Override
	public RestAction<Void> delete()
	{
		return null;
	}
	
	@Nonnull
	@Override
	public MessageAction sendMessage(@Nonnull CharSequence text)
	{
		if (getTextChannel() != null) {
			getTextChannel().sendMessage(text);
		}
		
		return channel.sendMessage(text);
	}
	
	
	@NotNull
	@Override
	public MessageAction sendMessageEmbeds(@NotNull MessageEmbed embed, @NotNull MessageEmbed... other)
	{
		if (getTextChannel() != null) {
			getTextChannel().sendMessageEmbeds(embed);
		}
		
		return channel.sendMessageEmbeds(embed);
	}
	
	@Override
	public MessageAction sendMessage(@Nonnull Message msg)
	{
		if (getTextChannel() != null) {
			getTextChannel().sendMessage(msg);
		}
		
		return channel.sendMessage(msg);
	}
	
	
	public TextChannel getTextChannel()
	{
		if (channel instanceof TextChannel) {
			return (TextChannel)channel;
		}
		
		return null;
	}
	
	public String getAsMention()
	{
		if (isPrivate()) {
			return null;
		}
		
		return "<#" + getIdLong() + '>';
	}
	
	public boolean isPrivate()
	{
		return getGuild() == null;
	}
	
	public Guild getGuild()
	{
		if (channel instanceof GuildChannel) {
			return ((GuildChannel)channel).getGuild();
		}
		
		if(channel instanceof TextChannel){
			return ((TextChannel)channel).getGuild();
		}
		
		return null;
	}
	
	@Override
	public long getIdLong()
	{
		return channel.getIdLong();
	}
}
