package Core.CommandSystem.CommandObjects.ResponseSystem;

import Core.CommandSystem.ChatUtils;
import Core.CommandSystem.CommandObjects.ArgumentMessageRunnable;
import Core.Main.Logging;
import Core.Objects.Annotation.Method.EventListener;
import Core.Objects.Annotation.Method.Interval;
import Core.Objects.BotChannel;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class ResponseCommand
{
	public static final Long timeout = TimeUnit.MILLISECONDS.convert(5, TimeUnit.MINUTES);
	//BotChannel, User -> FPObject
	private static final ConcurrentHashMap<Long, ConcurrentHashMap<Long, ResponseObject>> objects = new ConcurrentHashMap<>();
	
	@Interval(time_interval = 1, initial_delay = 1)
	public static void run(){
		for (Map.Entry<Long, ConcurrentHashMap<Long, ResponseObject>> ent1 : objects.entrySet()) {
			if (ent1.getValue() != null) {
				for (Map.Entry<Long, ResponseObject> ent2 : ent1.getValue().entrySet()) {
					if (ent2.getValue() != null) {
						Long timeSince = System.currentTimeMillis() - ent2.getValue().time;
						
						if (timeSince >= timeout) {
							ent2.getValue().object.timeout();
							
							objects.get(ent1.getKey()).remove(ent2.getKey());
							
							if (objects.get(ent1.getKey()).isEmpty()) {
								objects.remove(ent1.getKey());
							}
						}
					}
				}
			}
		}
	}
	
	@EventListener
	public static void event(MessageReceivedEvent event)
	{
		BotChannel channel = new BotChannel(event.getMessage().getChannel());
		
		if (event.getMessage().getAuthor() != null) {
			User user = event.getMessage().getAuthor();
			
			if (objects.containsKey(channel.getIdLong())) {
				if (objects.get(channel.getIdLong()).containsKey(user.getIdLong())) {
					ResponseObject object = objects.get(channel.getIdLong()).get(user.getIdLong());
					
					Message issuedMessage = object.issuedMessage;
					ResponseAction c = object.object;
					
					String[] args = event.getMessage().getContentDisplay().split(" ");
					
					if (c.isValidInput(event.getMessage(), args)) {
						
						try {
							c.execute(event.getMessage(), args);
						}catch (Exception e){
							ChatUtils.sendEmbed(channel, "There was an error trying to perform the command! If this persists please report it using the built in bugreport command.");
							Logging.exception(e);
						}
						
						if(objects.containsKey(channel.getIdLong()) && user != null) {
							objects.get(channel.getIdLong()).remove(user.getIdLong());
							
							if (objects.get(channel.getIdLong()).isEmpty()) {
								objects.remove(channel.getIdLong());
							}
						}
					}
				}
			}
		}
	}
	
	public static void scheduleSimpleResponse(Message inputMessage, Message mes, BotChannel channel, User user, String[] valid, ArgumentMessageRunnable runnable){
			scheduleSimpleResponse(inputMessage, mes, channel, user, valid, runnable, true);
	}
	
	public static void scheduleSimpleResponse(Message inputMessage, Message mes, BotChannel channel, User user, String[] valid, ArgumentMessageRunnable runnable, boolean delete){
		ResponseCommand.scheduleResponse(inputMessage, channel, user, new ResponseAction()
		{
			@Override
			public boolean isValidInput(Message message, String[] args)
			{
				String key = String.join(" ", args);
				
				if(valid == null){
					return true;
				}else{
					for(String t : valid){
						if(t.equalsIgnoreCase(key)){
							return true;
						}
					}
				}
				
				return false;
			}
			
			@Override
			public void execute( Message message, String[] args)
			{
				if(runnable != null) {
					runnable.run(message, args);
				}
				
				if (message != null) {
					ChatUtils.deleteMessage(message);
				}
				
				if(delete) {
					if (mes != null) {
						ChatUtils.deleteMessage(mes);
					}
				}
			}
			
			@Override
			public void timeout()
			{
				ChatUtils.deleteMessage(mes);
			}
		});
	}
	
	public static void scheduleResponse(Message issuedMessage, BotChannel channel, User user, ResponseAction command)
	{
		ResponseObject object = new ResponseObject(issuedMessage, command);
		
		if (!objects.containsKey(channel.getIdLong())) {
			objects.put(channel.getIdLong(), new ConcurrentHashMap<>());
		}
		
		if (objects.get(channel.getIdLong()).containsKey(user.getIdLong())) {
			objects.get(channel.getIdLong()).get(user.getIdLong()).object.timeout();
		}
		
		objects.get(channel.getIdLong()).put(user.getIdLong(), object);
	}
	
}
