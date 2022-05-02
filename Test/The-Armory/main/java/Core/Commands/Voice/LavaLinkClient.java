package Core.Commands.Voice;

import Core.Commands.Voice.Objects.TrackObject;
import Core.Main.Logging;
import Core.Main.Startup;
import Core.Objects.Annotation.Fields.VariableState;
import Core.Objects.Annotation.Method.EventListener;
import Core.Objects.Annotation.Method.Startup.PreInit;
import Core.Objects.Events.BotCloseEvent;
import Core.Util.JsonUtils;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.mashape.unirest.request.GetRequest;
import lavalink.client.io.jda.JdaLavalink;
import lavalink.client.io.jda.JdaLink;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.VoiceChannel;
import org.json.JSONArray;
import org.json.JSONObject;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class LavaLinkClient
{
	public static JdaLavalink lavalink;
	
	private static String IP;
	private static String password;
	
	public static boolean init = false;
	
	//TODO Use the GetTracks function as a search function, will allow searching soundcloud for example
	
	private static final ResourceBundle properties = ResourceBundle.getBundle("lavalink",
	                                                                          new ResourceBundle.Control() {
		                                                                          @Override
		                                                                          public List<Locale> getCandidateLocales(String name,
				                                                                          Locale locale) {
			                                                                          return Collections.singletonList(Locale.ROOT);
		                                                                          }
	                                                                          });
	
	@VariableState( variable_class = "Core.Main.Startup", variable_name = "USE_LAVA_LINK" )
	@PreInit
	public static void initLavaLink()
	{
		if(!Startup.USE_LAVA_LINK) return;
		
		IP = "http://" + properties.getString("lava_link_ip");
		password = properties.getString("lava_link_password");
		
		
		if(IP == null || IP.isBlank() || password == null || password.isBlank()){
			System.err.println("Unable to init LavaLink connection!");
			return;
		}
		
		lavalink = new JdaLavalink(Startup.debug ? "206178161023516672" : "188361942098771969", 1, shardId -> Startup.discordClient);
		
		try {
			
			URL url = new URL(IP);
			lavalink.addNode(url.toURI(), password);
			init = true;
			
		} catch (URISyntaxException | MalformedURLException e) {
			Logging.exception(e);
		}
	}
	
	@VariableState( variable_class = "Core.Main.Startup", variable_name = "USE_LAVA_LINK" )
	@EventListener
	public static void handle(BotCloseEvent event){
		lavalink.shutdown();
	}
	
	public static JdaLink getLink(Guild guild){
		return lavalink.getLink(guild);
	}
	
	
	//TODO Improve this
	public static ArrayList<TrackObject> getTrackObject(String identifier) {
		ArrayList<TrackObject> list = new ArrayList<>();
		
		if(identifier == null || identifier.isBlank()){
			return list;
		}
		
		try {
			GetRequest t = Unirest.get(IP + "/loadtracks?identifier=" + URLEncoder.encode(identifier, StandardCharsets.UTF_8)).header("Authorization", password);
			JSONArray trackData = t.asJson().getBody().getArray();
			
			trackData.forEach(o -> {
				JSONObject object = ((JSONObject) o);
				TrackObject obj = JsonUtils.getGson_pretty().fromJson(object.toString(), TrackObject.class);
				
				if(obj != null){
					list.add(obj);
				}
			});
			
		} catch (UnirestException e) {
			Logging.exception(e);
		}
		
		return list;
	}
	
	public static boolean connectToChannel(VoiceChannel channel){
		Guild guild = channel.getGuild();
		JdaLink link = LavaLinkClient.getLink(guild);
		
		if(link != null) {
			link.connect(channel);
			
			MusicCommand.getGuildAudioPlayer(guild).getPlayer().setPaused(false);
			return true;
		}
		
		return false;
	}
	
	public static boolean disconnectFromChannel(Guild guild){
		JdaLink link = LavaLinkClient.getLink(guild);
		MusicCommand.getGuildAudioPlayer(guild).getPlayer().setPaused(true);
		
		if(link != null) {
			link.disconnect();
			return true;
		}
		
		return false;
	}
}

