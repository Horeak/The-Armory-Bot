package Core.Main;

import Core.Objects.Annotation.Method.Interval;
import Core.Objects.Annotation.Method.Startup.Init;
import Core.Util.ReflectionUtils;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;

import java.util.List;

public class BotStatusTextSystem
{
	public static int current = 0;
	private static List<playingObject> objectList;
	
	@Init
	public static void init()
	{
		objectList = ReflectionUtils.getSubTypes(playingObject.class);
	}
	
	@Interval(time_interval = 10)
	private static void update()
	{
		if(objectList.size() > 0 && !Startup.debug) {
			if (current >= objectList.size()) {
				current = 0;
			}
			
			playingObject object = objectList.get(current);
			
			if (object != null) {
				if (!object.isEnabled()) {
					current++;
					update();
				} else {
					object.onChange();
					String text = object.getPlayingText().replace("-$pre", Startup.getCommandSign());
					Startup.discordClient.getPresence().setPresence(OnlineStatus.ONLINE, object.playingUrl() != null ? Activity.streaming(
							text, object.playingUrl()) : Activity.playing(text));
					current++;
				}
			}
		}else{
			Startup.discordClient.getPresence().setPresence(OnlineStatus.ONLINE, Activity.playing("Version: " + Startup.getVersion()));
		}
	}
	
	public static class serverText extends playingObject
	{
		@Override
		public String getPlayingText()
		{
			return "Currently on " + Startup.discordClient.getGuilds().size() + " servers!";
		}
		
		//This is so it doesnt show currently on 1 server for example when first starting up
		@Override
		public boolean isEnabled()
		{
			return Startup.discordClient.getGuilds().size() > 20;
		}
	}
	
	public static class inviteLink extends playingObject
	{
		@Override
		public String getPlayingText()
		{
			return "Use /invite to add the bot to your own server.";
		}
	}
	
	public static class bugReportText extends playingObject
	{
		@Override
		public String getPlayingText()
		{
			return "Found an issue? Use /bugReport to report it!";
		}
	}
	
	public static abstract class playingObject
	{
		public playingObject() {}
		public boolean isEnabled()
		{
			return !Startup.debug;
		}
		public abstract String getPlayingText();
		public String playingUrl() {return null;}
		public void onChange(){}
	}
}
