package Core.Commands.Warframe.Objects;

import java.util.ArrayList;
import java.util.UUID;

public class PostObject
{
	public UUID id;
	public String postType;
	public String platform;
	public Long channelId;
	public ArrayList<String> mentions;
	public ArrayList<String> filters;
	
	public PostObject(
			String postType, String platform, Long channelId, ArrayList<String> mentions, ArrayList<String> filters)
	{
		this.id = UUID.randomUUID();
		this.postType = postType;
		this.platform = platform;
		this.channelId = channelId;
		this.mentions = mentions;
		this.filters = filters;
	}
}
