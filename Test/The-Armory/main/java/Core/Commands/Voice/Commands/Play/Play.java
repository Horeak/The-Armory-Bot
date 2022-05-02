package Core.Commands.Voice.Commands.Play;

import Core.CommandSystem.ChatUtils;
import Core.CommandSystem.CommandObjects.ComponentSystem.ComponentResponseSystem;
import Core.CommandSystem.CommandObjects.SlashCommands.SlashCommandChannel;
import Core.CommandSystem.CommandObjects.SlashCommands.SlashCommandMessage;
import Core.CommandSystem.SlashMessageBuilder;
import Core.Commands.Voice.Commands.Queue.CurrentQueue;
import Core.Commands.Voice.Commands.YoutubeCommand;
import Core.Commands.Voice.Commands.YoutubeCommand.YoutubeResult;
import Core.Commands.Voice.MusicCommand;
import Core.Commands.Voice.MusicCommand.MusicInfoCommand;
import Core.Commands.Voice.Objects.TrackObject;
import Core.Commands.Voice.Objects.TrackScheduler;
import Core.Objects.Annotation.Commands.SlashArgument;
import Core.Objects.Annotation.Commands.SubCommand;
import Core.Objects.BotChannel;
import Core.Util.ConnectionUtils;
import Core.Util.Utils;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.SelectMenuInteractionEvent;
import net.dv8tion.jda.api.interactions.components.ItemComponent;
import net.dv8tion.jda.api.interactions.components.selections.SelectMenu;
import net.dv8tion.jda.api.interactions.components.selections.SelectOption;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@SubCommand( parent = MusicInfoCommand.class)
public class Play extends MusicCommand
{
	@SlashArgument( key = "search", text = "The search phrase to use to find music to add", required = true)
	public String searchPhrase;
	
	
	@Override
	public String getSlashName()
	{
		return "play";
	}
	
	@Override
	public String getDescription()
	{
		return "Adds songs to the current queue from youtube links and similar";
	}
	
	@Override
	public void slashCommandExecuted(SlashCommandInteractionEvent slashEvent, Guild guild, User author, SlashCommandChannel channel, SlashCommandMessage message)
	{
		ArrayList<YoutubeResult> urls = YoutubeCommand.getYoutubeResults(searchPhrase);
		
		if (urls.size() > 10) {
			urls = new ArrayList<>(urls.subList(0, 10));
		}
		
		if (urls.size() > 0) {
			EmbedBuilder builder = new EmbedBuilder();
			StringBuilder st = new StringBuilder();
			
			int i = 1;
			for (YoutubeCommand.YoutubeResult ur : urls) {
				String name = ur.title;
				
				if (name.length() > 70) {
					name = name.substring(0, 70) + "...";
				}
				
				if (name.contains("[") && !name.contains("]")) {
					name += "]";
				}
				
				st.append("**").append(i).append("**").append(") ").append(urls.size() >= 10 && i == 1 ? " " : "").append(urls.size() >= 10 && i < 10 ? "  " : "").append("[**").append(name).append("**](").append(ur.url).append(")").append("\n");
				
				i++;
			}
			
			builder.setDescription(st.toString());
			
			final ArrayList<YoutubeCommand.YoutubeResult> urls1 = new ArrayList<>(urls);
			
			ArrayList<ItemComponent> actions = new ArrayList<>();
			
			UUID menuID = UUID.randomUUID();
			SelectMenu.Builder menuBuilder = SelectMenu.create(menuID.toString()).setPlaceholder("Select song to play").setRequiredRange(1, 25);
			
			for (int g = 0; g < (Math.min(urls1.size(), 10)); g++) {
				menuBuilder.addOptions(SelectOption.of("Nr. " + (g + 1), urls1.get(g).url).withDescription(Utils.limitString(urls1.get(g).title, 50)));
			}
			
			actions.add(ComponentResponseSystem.addComponent(menuID, author, slashEvent, menuBuilder.build(), (e) -> {
				SelectMenuInteractionEvent event = (SelectMenuInteractionEvent)e;
				
				for (String val : event.getValues()) {
					TrackScheduler musicManager = getGuildAudioPlayer(guild);
					queueUrl(author, guild, channel, musicManager, val);
				}
			}));
			
			builder.setTitle("Please select a song out of the following result using the below selection menu.");
			
			SlashMessageBuilder slashBuilder = ChatUtils.createSlashMessage(author, channel);
			slashBuilder.withEmbed(builder);
			slashBuilder.withActions(actions);
			slashBuilder.send();
			
			return;
		} else {
			ChatUtils.sendEmbed(channel, author.getAsMention() + " Found no search results for search word `" + searchPhrase + "`");
		}
		
		TrackScheduler musicManager = getGuildAudioPlayer(guild);
		
		List<String> strings = ConnectionUtils.extractUrls(searchPhrase);
		urls.forEach((url) -> strings.removeIf((url1)  -> url1.equals(url.url)));
		
		for (String url : strings) {
			queueUrl(author, guild, channel, musicManager, url);
		}
	}
	
	public static void queueUrl(User author, Guild guild, BotChannel channel, TrackScheduler musicManager, String url)
	{
		ArrayList<AudioTrack> tracks = new ArrayList<>();
		
		queueSong(guild, channel, author, url, trackObject -> {
			if(trackObject.isPlayList()){
				EmbedBuilder builder = new EmbedBuilder();
				
				builder.setTitle("Adding playlist to queue: ");
				builder.setDescription("**Playlist `" + trackObject.playlistInfo.name + "`**");
				
				int i = 1;
				StringBuilder bd = new StringBuilder();
				
				for (TrackObject.Track track : trackObject.tracks) {
					if (i > 10) {
						break;
					}
					
					String name = track.info.title;
					
					StringBuilder bd1 = new StringBuilder();
					
					bd1.append("**").append(i).append("**. ").append(
							trackObject.tracks.length >= 10 && i < 10 ? " " : "").append("[**").append(name).append(
							"**](").append(track.info.uri).append(")");
					bd1.append(" (**").append(CurrentQueue.getTimeString(track.info.length)).append("**)\n");
					
					if (bd.toString().length() + bd1.toString().length() >= (MessageEmbed.VALUE_MAX_LENGTH - 20)) {
						break;
					}
					
					bd.append(bd1.toString());
					i++;
				}
				
				if (trackObject.tracks.length > i) {
					int g = trackObject.tracks.length - i;
					
					if (g > 0) {
						bd.append("\n*And ").append(g).append(" more.*");
					}
				}
				
				long time = 0L;
				for (TrackObject.Track track : trackObject.tracks) {
					time += track.info.length;
				}
				
				
				if (trackObject.tracks.length > 0) {
					String t = bd.toString();
					
					if (t.length() >= MessageEmbed.VALUE_MAX_LENGTH - 5) {
						t = t.substring(0, MessageEmbed.VALUE_MAX_LENGTH - 5) + "...";
					}
					
					builder.addField("Tracks", t, false);
				}
				
				if (time > 0) {
					String t = "(**" + CurrentQueue.getTimeString(time) + "**)";
					
					if (t.length() >= MessageEmbed.VALUE_MAX_LENGTH - 5) {
						t = t.substring(0, MessageEmbed.VALUE_MAX_LENGTH - 5) + "...";
					}
					
					builder.addField("Total duration", t, false);
				}
				
				ChatUtils.sendMessage(channel, builder.build());
			}else {
				boolean skipped = false;
				for(TrackObject.Track track : trackObject.tracks) {
					if(MusicCommand.getGuildAudioPlayer(guild).getQueueSize() > 1 && MusicCommand.getGuildAudioPlayer(guild).getPlayer().getPlayingTrack() == null){
						if(!skipped){
							skipped = true;
							continue;
						}
					}
					
					EmbedBuilder builder = new EmbedBuilder();
					builder.setTitle("Adding to queue: ");
					
					StringBuilder builder1 = new StringBuilder();
					
					String tTime = track.info.length != null && track.info.length < System.currentTimeMillis() ? CurrentQueue.getTimeString(track.info.length) : "Unknown duration";
					
					builder1.append("[**").append(track.info.title).append("**](").append(track.info.uri).append(")");
					builder1.append(" (**").append(tTime).append("**)");
					
					ChatUtils.setEmbedColor(guild, author, builder);
					ChatUtils.setFooter(builder, author, channel);
					
					if (musicManager.getPlayer().getPlayingTrack() != null) {
						builder1.append("\n\n*Num. **").append(musicManager.getQueueSize() + musicManager.urlQueue.size() + 1).append(
								"** in current queue!*");
					}
					
					builder.setDescription(builder1.toString());
					ChatUtils.sendMessage(channel, builder.build());
				}
			}
		});
		
		for (AudioTrack track1 : tracks) {
			MusicCommand.getGuildAudioPlayer(guild).queue(track1);
		}
	}
}
