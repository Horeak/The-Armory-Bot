package Core.Commands.Voice.Objects;

import Core.Objects.BotChannel;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;

public class TrackData
{
	public String url;
	public Guild guild;
	public BotChannel channel;
	public User user;
	
	public String channelId;
	public String title;
	
	public TrackData(Guild guild, BotChannel channel, User user, String channelId, String title)
	{
		this.guild = guild;
		this.channel = channel;
		this.user = user;
		this.channelId = channelId;
		this.title = title;
	}
}
