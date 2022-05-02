package Core.CommandSystem.CommandObjects.SlashCommands;

import Core.Objects.BotChannel;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.callbacks.IDeferrableCallback;
import net.dv8tion.jda.api.managers.channel.concrete.TextChannelManager;
import net.dv8tion.jda.api.requests.RestAction;
import net.dv8tion.jda.api.requests.restaction.*;
import net.dv8tion.jda.api.requests.restaction.pagination.ThreadChannelPaginationAction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;

public class SlashCommandChannel extends BotChannel implements TextChannel
{
	public IDeferrableCallback event;
	public SlashCommandChannel(IDeferrableCallback  event)
	{
		super(event instanceof SlashCommandInteractionEvent ? ((SlashCommandInteractionEvent)event).getChannel() : event instanceof ButtonInteractionEvent ? (((ButtonInteractionEvent)event).getChannel()) : null);
		this.event = event;
	}
	
	public TextChannel getChannel(){
		if(event.getChannel() instanceof TextChannel){
			return (TextChannel)event.getChannel();
		}
		
		return null;
	}
	
	@Nullable
	@Override
	public String getTopic()
	{
		return getChannel() != null ? getChannel().getTopic() : null;
	}
	
	@Override
	public boolean isNSFW()
	{
		return getChannel() != null && getChannel().isNSFW();
	}

	@Override
	public int getSlowmode()
	{
		return getChannel() != null ? getChannel().getSlowmode() : 0;
	}
	
	@NotNull
	@Override
	public List<Member> getMembers()
	{
		return getChannel() != null ? getChannel().getMembers() : null;
	}
	
	@Override
	public int getPosition()
	{
		return getChannel() != null ? getChannel().getPosition() : 0;
	}
	
	@Override
	public int getPositionRaw()
	{
		return getChannel() != null ? getChannel().getPositionRaw() : 0;
	}
	
	@Nullable
	@Override
	public PermissionOverride getPermissionOverride(
			@NotNull IPermissionHolder permissionHolder)
	{
		return getChannel() != null ? getChannel().getPermissionOverride(permissionHolder) : null;
	}
	
	@NotNull
	@Override
	public List<PermissionOverride> getPermissionOverrides()
	{
		return getChannel() != null ? getChannel().getPermissionOverrides() : null;
	}
	
	@NotNull
	@Override
	public List<PermissionOverride> getMemberPermissionOverrides()
	{
		return getChannel() != null ? getChannel().getMemberPermissionOverrides() : null;
	}
	
	@NotNull
	@Override
	public List<PermissionOverride> getRolePermissionOverrides()
	{
		return getChannel() != null ? getChannel().getRolePermissionOverrides() : null;
	}
	
	@Override
	public boolean isSynced()
	{
		return getChannel() != null && getChannel().isSynced();
	}
	
	@NotNull
	@Override
	public ChannelAction<TextChannel> createCopy(
			@NotNull Guild guild)
	{
		return getChannel() != null ? getChannel().createCopy(guild) : null;
	}
	
	@NotNull
	@Override
	public ChannelAction<TextChannel> createCopy()
	{
		return getChannel() != null ? getChannel().createCopy() : null;
	}
	
	@Override
	public TextChannelManager getManager()
	{
		return getChannel() != null ? getChannel().getManager() : null;
	}
	
	@Override
	public long getParentCategoryIdLong()
	{
		return 0;
	}
	
	@NotNull
	@Override
	public AuditableRestAction<Void> delete()
	{
		return getChannel() != null ? getChannel().delete() : null;
	}
	
	@Override
	public IPermissionContainer getPermissionContainer()
	{
		return null;
	}
	
	@NotNull
	@Override
	public PermissionOverrideAction createPermissionOverride(
			@NotNull IPermissionHolder permissionHolder)
	{
		return getChannel() != null ? getChannel().createPermissionOverride(permissionHolder) : null;
	}
	
	@NotNull
	@Override
	public PermissionOverrideAction putPermissionOverride(
			@NotNull IPermissionHolder permissionHolder)
	{
		return getChannel() != null ? getChannel().putPermissionOverride(permissionHolder) : null;
	}
	
	@NotNull
	@Override
	public InviteAction createInvite()
	{
		return getChannel() != null ? getChannel().createInvite() : null;
	}
	
	@NotNull
	@Override
	public RestAction<List<Invite>> retrieveInvites()
	{
		return getChannel() != null ? getChannel().retrieveInvites() : null;
	}
	
	@NotNull
	@Override
	public RestAction<List<Webhook>> retrieveWebhooks()
	{
		return getChannel() != null ? getChannel().retrieveWebhooks() : null;
	}
	
	@NotNull
	@Override
	public WebhookAction createWebhook(@NotNull String name)
	{
		return getChannel() != null ? getChannel().createWebhook(name) : null;
	}
	
	@NotNull
	@Override
	public RestAction<Void> deleteMessages(@NotNull Collection<Message> messages)
	{
		return getChannel() != null ? getChannel().deleteMessages(messages) : null;
	}
	
	@NotNull
	@Override
	public RestAction<Void> deleteMessagesByIds(@NotNull Collection<String> messageIds)
	{
		return getChannel() != null ? getChannel().deleteMessagesByIds(messageIds) : null;
	}
	
	@NotNull
	@Override
	public AuditableRestAction<Void> deleteWebhookById(@NotNull String id)
	{
		return getChannel() != null ? getChannel().deleteWebhookById(id) : null;
	}
	
	@NotNull
	@Override
	public RestAction<Void> clearReactionsById(@NotNull String messageId)
	{
		return getChannel() != null ? getChannel().clearReactionsById(messageId) : null;
	}
	
	@NotNull
	@Override
	public RestAction<Void> clearReactionsById(@NotNull String messageId, @NotNull String unicode)
	{
		return getChannel() != null ? getChannel().clearReactionsById(messageId, unicode) : null;
	}
	
	@NotNull
	@Override
	public RestAction<Void> clearReactionsById(@NotNull String messageId, @NotNull Emote emote)
	{
		return getChannel() != null ? getChannel().clearReactionsById(messageId, emote) : null;
	}
	
	@NotNull
	@Override
	public RestAction<Void> removeReactionById(
			@NotNull String messageId, @NotNull String unicode, @NotNull User user)
	{
		return getChannel() != null ? getChannel().removeReactionById(messageId, unicode, user) : null;
	}
	
	@Override
	public boolean canTalk()
	{
		return getChannel() != null && getChannel().canTalk();
	}
	
	@Override
	public boolean canTalk(@NotNull Member member)
	{
		return getChannel() != null && getChannel().canTalk(member);
	}
	
	@Override
	public int compareTo(@NotNull GuildChannel o)
	{
		return getChannel() != null ? getChannel().compareTo(o) : 0;
	}
	
	@NotNull
	@Override
	public ThreadChannelAction createThreadChannel(String s, boolean b)
	{
		return null;
	}
	
	@NotNull
	@Override
	public ThreadChannelAction createThreadChannel(String s, long l)
	{
		return null;
	}
	
	@NotNull
	@Override
	public ThreadChannelPaginationAction retrieveArchivedPublicThreadChannels()
	{
		return null;
	}
	
	@NotNull
	@Override
	public ThreadChannelPaginationAction retrieveArchivedPrivateThreadChannels()
	{
		return null;
	}
	
	@NotNull
	@Override
	public ThreadChannelPaginationAction retrieveArchivedPrivateJoinedThreadChannels()
	{
		return null;
	}
}
