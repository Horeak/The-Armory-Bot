package Core.Main;

import Core.Objects.Annotation.Fields.VariableState;
import Core.Objects.Annotation.Method.Interval;
import Core.Objects.Annotation.Method.Startup.Init;
import Core.Objects.Annotation.Method.Startup.PostInit;
import org.discordbots.api.client.DiscordBotListAPI;
import org.discordbots.api.client.entity.VotingMultiplier;

import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.concurrent.TimeUnit;

public class BotListApi
{
	public static final boolean API_ENABLED = !Startup.debug;
	public static final boolean VOTING_ENABLED = API_ENABLED;
	
	public static String BOT_TOKEN;
	public static String BOT_ID;
	
	public static DiscordBotListAPI api;
	
	public static final String BOT_LINK = "https://top.gg/bot/188361942098771969";
	public static final String VOTE_URL = BOT_LINK + "/vote";
	
	private static final ResourceBundle properties = ResourceBundle.getBundle("botlist",
	                                                                          new ResourceBundle.Control() {
		                                                                          @Override
		                                                                          public List<Locale> getCandidateLocales(String name,
				                                                                          Locale locale) {
			                                                                          return Collections.singletonList(Locale.ROOT);
		                                                                          }
	                                                                          });
	
	@VariableState( variable_class = "Core.Main.BotListApi", variable_name = "API_ENABLED" )
	@Init
	public static void init(){
		BOT_TOKEN = properties.getString("botlist_token");
		BOT_ID = properties.getString("botlist_id");
	}
	
	@VariableState( variable_class = "Core.Main.BotListApi", variable_name = "API_ENABLED" )
	@PostInit
	public static void postInit(){
		api = new DiscordBotListAPI.Builder().token(BOT_TOKEN).botId(BOT_ID).build();
	}
	
	@VariableState( variable_class = "Core.Main.BotListApi", variable_name = "API_ENABLED" )
	@Interval( initial_delay = 2, time_interval = 6, time_unit = TimeUnit.HOURS)
	public static void setServers(){
		api.setStats(Startup.discordClient.getGuilds().size());
	}
	
	//TODO Randomize vote messages? Somehow include a message about the !Vote command
	public static String getVotingText(boolean userVoted, VotingMultiplier multiplier){
		if(userVoted) {
			return "Thank you for helping to support the bot by voting!";
		}
		
		if(multiplier != null && multiplier.isWeekend()){
			return "Votes are currently worth double! Remember to vote!";
		}
		
		return "Please remember to vote for the bot!";
	}
}
