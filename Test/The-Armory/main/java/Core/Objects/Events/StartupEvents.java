package Core.Objects.Events;

import Core.Main.Logging;
import Core.Main.Startup;
import Core.Objects.Annotation.Method.EventListener;
import Core.Objects.Annotation.Method.Startup.Init;
import Core.Objects.Annotation.Method.Startup.PostInit;
import Core.Util.ReflectionUtils;
import Core.Util.Utils;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import javax.annotation.Nonnull;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Callable;

public class StartupEvents
{
	static final HashMap<Class, ArrayList<Method>> eventListeners = new HashMap<>();
	
	@PostInit
	public static void startup()
	{
		System.out.println("Started \"" + Startup.discordClient.getSelfUser().getName() + "\" successfully!");
		System.out.println("Version \"" + Startup.getVersion() + "\"");
		//		System.out.println("Running on " + Startup.discordClient.getShardInfo().getShardTotal() + " shard(s)");
		System.out.println("Found " + Startup.discordClient.getGuilds().size() + " server(s)");
		System.out.println("Command prefix is set to '" + Startup.getCommandSign() + "'");
		
		if (Startup.BOT_PERMISSIONS != null) {
			Startup.botInviteLink = Startup.discordClient.getInviteUrl(Startup.BOT_PERMISSIONS);
			System.out.println("Invite link: " + Startup.botInviteLink);
		}
		
		Runtime.getRuntime().addShutdownHook(new Thread(Startup::onBotClose));
		
		System.out.println("Startup init done.");
		System.out.println("System startup took: " + Utils.getUpTime());
	}
	
	@Init
	public static void initListeners()
	{
		System.out.println("Start listener register");
		int i = 0;
		
		if (!Startup.initListeners) {
			List<Method> listeners = ReflectionUtils.getMethods(EventListener.class);
			
			for (Method method : listeners) {
				Class[] cc = method.getParameterTypes();
				
				if (cc != null && cc.length == 1) {
					if (!eventListeners.containsKey(cc[0])) {
						eventListeners.put(cc[0], new ArrayList<>());
					}
					
					eventListeners.get(cc[0]).add(method);
					i++;
				}
			}
			
			Startup.initListeners = true;
		}
		
		Startup.discordClient.getEventManager().register(new ListenerAdapter()
		{
			@Override
			public void onGenericEvent(@Nonnull GenericEvent e)
			{
				Startup.executor.submit(new EventTask(e));
			}
		});
		
		System.out.println("End listener register, found " + i + " listeners");
	}
	
	
	public static class EventTask implements Callable{
		GenericEvent e;
		
		public EventTask(GenericEvent e)
		{
			this.e = e;
		}
		
		@Override
		public Object call()
		{
			boolean found = false;
			if (eventListeners.containsKey(e.getClass())) {
				for (Method method : eventListeners.get(e.getClass())) {
					try {
						method.invoke(method.getDeclaringClass(), e);
						found = true;
					} catch (IllegalAccessException | InvocationTargetException e1) {
						if (e1 instanceof InvocationTargetException) {
							InvocationTargetException e2 = (InvocationTargetException)e1;
							
							if (e2 != null && e2.getCause() != null) {
								Logging.exception(e2.getCause());
							}
						} else {
							Logging.exception(e1);
						}
					}
				}
			}
			
			return found;
		}
	}
}
