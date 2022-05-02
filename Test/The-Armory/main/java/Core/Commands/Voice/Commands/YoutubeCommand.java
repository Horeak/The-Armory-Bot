package Core.Commands.Voice.Commands;

import Core.CommandSystem.ChatUtils;
import Core.CommandSystem.CommandObjects.SlashCommands.SlashCommandChannel;
import Core.CommandSystem.CommandObjects.SlashCommands.SlashCommandMessage;
import Core.Main.Logging;
import Core.Objects.Annotation.Commands.Command;
import Core.Objects.Annotation.Commands.SlashArgument;
import Core.Objects.Annotation.Method.Startup.PostInit;
import Core.Objects.Interfaces.Commands.ISlashCommand;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.YouTube.Videos;
import com.google.api.services.youtube.YouTubeRequestInitializer;
import com.google.api.services.youtube.model.*;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.jsoup.Jsoup;

import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import static Core.Commands.Voice.Commands.Queue.CurrentQueue.getTimeString;

@Command
public class YoutubeCommand implements ISlashCommand
{
	public static final HttpTransport HTTP_TRANSPORT = new NetHttpTransport();
	public static final JsonFactory JSON_FACTORY = new JacksonFactory();
	private static final String API_KEY = "AIzaSyBuY4cdCFmMUi5D8nYbKp7ScifF7YYUY9g";
	private static final String BASE_VIDEO_URL = "https://www.youtube.com/watch?v=";
	//https://developers.google.com/youtube/v3/docs/search/list
	private static final String ORDER = "relevance"; //relevance, rating
	private static final Long MAX_RESULTS = 10L; //TODO This was changed from 10 to 5. Keep or not?
	private static final String REGION = "US";
	private static YouTube youTube;
	
	@PostInit
	public static void postInit()
	{
		youTube = new YouTube.Builder(HTTP_TRANSPORT, JSON_FACTORY, request -> {}).setApplicationName(
				"The-Armory-Music-Lookup").setYouTubeRequestInitializer
				(new YouTubeRequestInitializer(API_KEY)).build();
	}
	
	
	@SlashArgument( key = "search", text = "The search phrase to use to find your video", required = true )
	public String search;
	
	@Override
	public String getSlashName()
	{
		return "youtube-search";
	}
	
	@Override
	public void slashCommandExecuted(SlashCommandInteractionEvent slashEvent, Guild guild, User author, SlashCommandChannel channel, SlashCommandMessage message)
	{
		//Add "&order=rating" to the url if it should prioritize more popular videos
		ArrayList<YoutubeResult> urls = getYoutubeResults(search);
		
		if (urls.size() > 10) {
			urls = new ArrayList<>(urls.subList(0, 10));
		}
		
		if (urls.size() > 0) {
			EmbedBuilder builder = new EmbedBuilder();
			StringBuilder st = new StringBuilder();
			
			int i = 1;
			for (YoutubeResult ur : urls) {
				String name = ur.title;
				
				if (name.length() > 70) {
					name = name.substring(0, 70) + "...";
				}
				
				if (name.contains("[") && !name.contains("]")) {
					name += "]";
				}
				
				String tTime = ur.length != null ? "(" + getTimeString(ur.length) + ")" : "";
				st.append("**").append(i).append("**").append(". ").append(urls.size() >= 10 && i == 1 ? " " : "").append(urls.size() >= 10 && i < 10 ? "  " : "").append("[**").append(name).append("**](").append(ur.url).append(")").append(" " + tTime).append("\n");
				i++;
			}
			
			builder.setDescription(st.toString());
			
			ChatUtils.sendMessage(channel, author.getAsMention(), builder.build());
			
		} else {
			ChatUtils.sendEmbed(channel, author.getAsMention() + " Found no video with that search word!");
		}
	}
	
	@Override
	public String getDescription()
	{
		return "Allows searching for youtube videos to get the url";
	}
	
	
	public static ArrayList<YoutubeResult> getYoutubeResults(String keyword)
	{
		return getYoutubeResults(keyword, MAX_RESULTS);
	}
	
	//TODO Use lavalink instead?
	//TODO THIS NEEDS CACHING!
	public static ArrayList<YoutubeResult> getYoutubeResults(String keyword, Long max)
	{
		ArrayList<YoutubeResult> urls = new ArrayList<>();
		
		try {
			YouTube.Search.List search = youTube.search().list(Collections.singletonList("id"));
			search.setQ(keyword);
			search.setType(Collections.singletonList("video")); //TODO I could add support for playlists by adding ",playlist" it can also have channels with "channel"
			//If i need description for anything it needs to be added to the fields here with "snippet/description"
			search.setFields("items(id/videoId)");
			
			search.setMaxResults(max);
			search.setOrder(ORDER);
			search.setRegionCode(REGION);
			
			SearchListResponse searchResponse = search.execute();
			ArrayList<SearchResult> searchResultList = new ArrayList<>(searchResponse.getItems());
			
			if (searchResultList != null) {
				for (SearchResult searchT : searchResultList) {
					Videos.List videoRequest = youTube.videos().list(new ArrayList<>(Arrays.asList("snippet", "contentDetails")));
					videoRequest.setId(Collections.singletonList(searchT.getId().getVideoId()));
					videoRequest.setFields("items(snippet/title,snippet/channelId,contentDetails/duration)");
					
					VideoListResponse videoResponse = videoRequest.execute();
					
					for (Video t : videoResponse.getItems()) {
						if (t != null) {
							String title = null;
							String url;
							String channelId = null;
							Long duration = null;
							
							url = BASE_VIDEO_URL + searchT.getId().getVideoId();
							
							if (t.getSnippet() != null) {
								VideoSnippet snip = t.getSnippet();
								if (snip.getChannelId() != null) {
									channelId = snip.getChannelId();
								}
								
								if (snip.getTitle() != null) {
									title = Jsoup.parse(snip.getTitle()).text();
								}
							}
							
							if(t.getContentDetails() != null){
								VideoContentDetails details = t.getContentDetails();
								
								if(details.getDuration() != null){
									duration = Duration.parse(details.getDuration()).toMillis();
								}
							}
							
							if (url == null || url.isEmpty() || title == null || title.isEmpty()) {
								continue;
							}
							
							urls.add(new YoutubeResult(url, title, channelId, duration));
						}
					}
				}
			}
			
		} catch (IOException e) {
			Logging.exception(e);
		}
		
		return urls;
	}
	
	
	public static class YoutubeResult
	{
		public String url;
		public String title;
		public String channelId;
		public Long length;
		
		public YoutubeResult(String url, String title, String channelId, Long duration)
		{
			this.url = url;
			this.title = title;
			this.channelId = channelId;
			this.length = duration;
		}
	}
	
	
}
