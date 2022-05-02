package Core.Commands.Warframe.NotificationObjects;

import Core.CommandSystem.ChatUtils;
import Core.Commands.Warframe.NotificationObjects.Commands.NotificationsCommand;
import Core.Commands.Warframe.Objects.InfoObject;
import Core.Commands.Warframe.Objects.PostObject;
import Core.Commands.Warframe.SubSystem.SubscribeCommand;
import Core.Commands.Warframe.Warframe;
import Core.Main.Logging;
import Core.Main.Startup;
import Core.Objects.Annotation.Fields.DataObject;
import Core.Objects.Annotation.Method.Interval;
import Core.Objects.BotChannel;
import Core.Objects.CustomEntry;
import Core.Util.Utils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.User;
import org.json.JSONArray;
import org.json.JSONObject;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class NotificationSystem
{
	public static final int MAX_STORE = 20;
	
	@DataObject( file_path = "warframe/notifications.json", name = "postObjects" )
	public static CopyOnWriteArrayList<PostObject> postObjects = new CopyOnWriteArrayList<>();
	
	@DataObject( file_path = "warframe/notifications.json", name = "postedIds" )
	public static ConcurrentHashMap<String, ConcurrentHashMap<String, CopyOnWriteArrayList<String>>> postedIds = new ConcurrentHashMap<>();
	
	@DataObject( file_path = "warframe/notifications.json", name = "postedSubIds" )
	public static ConcurrentHashMap<String, ConcurrentHashMap<String, CopyOnWriteArrayList<String>>> postedSubIds = new ConcurrentHashMap<>();
	
	@Interval( time_interval = 2, initial_delay = 1)
	public static void runNameSub(){
		if(NotificationsCommand.staticInfoObjects != null) {
			for (InfoObject instanceOb : NotificationsCommand.staticInfoObjects) {
				
				try {
					for (String pm : Warframe.getPlatforms()) {
						
						if (!SubscribeCommand.lastHit.containsKey(pm)) {
							SubscribeCommand.lastHit.put(pm, new ConcurrentHashMap<>());
						}
						
						if (SubscribeCommand.subscribedWords.containsKey(pm)) {
							CopyOnWriteArrayList<InfoObject> objects = new CopyOnWriteArrayList<>(getFilteredSubList(instanceOb.getType(), pm));
							
							if (objects.size() > 0) {
								for (Map.Entry<Long, CopyOnWriteArrayList<String>> ent : SubscribeCommand.subscribedWords.get(
										pm).entrySet()) {
									try {
										runSub(instanceOb.getType(), ent.getKey(), ent.getValue(), objects);
									} catch (Exception e) {
										Logging.exception(e);
									}
								}
							}
						}
					}
				} catch (Exception e) {
					Logging.exception(e);
				}
			}
		}
	}
	
	@Interval( time_interval = 2, initial_delay = 1)
	public static void run(){
		//Map is Post Type, Platform -> List<PostObject>
		ConcurrentHashMap<String, ConcurrentHashMap<String, CopyOnWriteArrayList<PostObject>>> sortedMap = new ConcurrentHashMap<>();
		
		for(PostObject ob : postObjects){
			sortedMap.computeIfAbsent(ob.postType, (e) -> new ConcurrentHashMap<>()).computeIfAbsent(ob.platform, (e) -> new CopyOnWriteArrayList<>()).add(ob);
		}
		
		for(Entry<String, ConcurrentHashMap<String, CopyOnWriteArrayList<PostObject>>> ent1 : sortedMap.entrySet()){
			for(Map.Entry<String, CopyOnWriteArrayList<PostObject>> ent2 : ent1.getValue().entrySet()){
				ArrayList<InfoObject> list = getFilteredList(ent1.getKey(), ent2.getKey());
				CopyOnWriteArrayList<InfoObject> objects = new CopyOnWriteArrayList<>(list);
				
				if(objects.size() > 0){
					for(InfoObject infoObject : objects){
						checkSubs(infoObject, ent2.getKey());
					}
					
					for(PostObject object : ent2.getValue()){
						MessageChannel channel = Startup.discordClient.getTextChannelById(object.channelId);
						
						if (channel != null) {
							try {
								run(ent1.getKey(), object, new BotChannel(channel), objects);
							} catch (Exception e) {
								Logging.exception(e);
							}
						}
					}
				}
			}
		}
	}
	
	public static void checkSubs(InfoObject infoObject, String key2)
	{
		for (String key : infoObject.keys) {
			if (SubscribeCommand.lastHit.get(key2).containsKey(key)) {
				CustomEntry<String, Long> ent = SubscribeCommand.lastHit.get(key2).get(key);
				if (ent.getKey().equals(infoObject.id)) {
					continue;
				}
			}
			
			try {
				Date dt = Warframe.format.parse(infoObject.startTime);
				
				if (dt != null) {
					SubscribeCommand.lastHit.get(key2).put(key, new CustomEntry<>(infoObject.id, dt.getTime()));
				}
			} catch (ParseException e) {
				Logging.exception(e);
			}
		}
	}
	
	protected static void run(
			String name, PostObject ob, BotChannel channel,
			CopyOnWriteArrayList<InfoObject> objects)
	{
		if (ob.filters != null && ob.filters.size() > 0) {
			for (InfoObject oj : objects) {
				boolean has = false;
				
				for (String key : oj.keys) {
					for (String tk : ob.filters) {
						if (tk.matches("(?i)" + key.replace(" ", "."))) {
							has = true;
							break;
						}
					}
				}
				
				if (!has) {
					objects.remove(oj);
				}
			}
		}
		
		for (InfoObject al : objects) {
			EmbedBuilder eo = al.genEmbed();
			
			if (eo != null) {
				if (ob.mentions != null && !ob.mentions.isEmpty()) {
					ChatUtils.sendMessage(channel, String.join(" ", ob.mentions), eo.build());
				} else {
					ChatUtils.sendMessage(channel, eo.build());
				}
				
				if (!hasBeenPosted(name, al)) {
					addPostId(name, al);
				}
			}
		}
	}
	
	private static void runSub(
			String name, Long userId, CopyOnWriteArrayList<String> filters,
			CopyOnWriteArrayList<InfoObject> objects)
	{
		User user = Utils.getUser(userId);
		
		
		if (user != null) {
			if (filters.size() > 0) {
				
				for (InfoObject oj : objects) {
					boolean has = false;
					
					for (String key : oj.keys) {
						for (String tk : filters) {
							//TODO Need to find a better way to match this
							if (tk.matches("(?i)" + key.replace(" ", ".").replace("*", ".*"))) {
								has = true;
								break;
							}
						}
					}
					
					if (!has) {
						objects.remove(oj);
					}
				}
				
				for (InfoObject al : objects) {
					EmbedBuilder eo = al.genEmbed();
					
					if (eo != null) {
						user.openPrivateChannel().queue(
								chan -> ChatUtils.sendMessage(chan, "A match was found for one of your subscriptions!",
								                              eo.build()));
						
						if (!hasSubBeenPosted(name, al)) {
							addSubPostId(name, al);
						}
					}
				}
			}
		}
	}
	
	public static ArrayList<InfoObject> getFilteredList(String name, String platform)
	{
		ArrayList<InfoObject> list = new ArrayList<>();
		InfoObject staticInstance = NotificationsCommand.getStaticInstance(name);
		
		if(staticInstance != null) {
			if(platform.equalsIgnoreCase("all")){
				for(String plat : Warframe.getPlatforms()){
					list.addAll(getFilteredList(name, plat));
				}
				
			}else {
				JSONArray array = Warframe.getArray(platform, staticInstance.getType());
				
				if (array != null) {
					for (Object ob : array) {
						JSONObject object = (JSONObject)ob;
						InfoObject al = staticInstance.loadObject(object);
						
						if (al == null) {
							continue;
						}
						
						al.keys.replaceAll((e) -> {
							if (e != null) {
								return e.replace(" ", ".").toLowerCase();
							}
							
							return e;
						});
						
						if (!hasBeenPosted(name, al)) {
							list.add(al);
						}
					}
				}
				
			}
			return list;
		}
		
		return list;
	}
	
	public static ArrayList<InfoObject> getFilteredSubList(String name, String platform)
	{
		ArrayList<InfoObject> list = new ArrayList<>();
		InfoObject staticInstance = NotificationsCommand.getStaticInstance(name);
		
		if(staticInstance != null) {
			JSONArray array = Warframe.getArray(platform, staticInstance.getType());
			
			for (Object ob : array) {
				JSONObject object = (JSONObject)ob;
				InfoObject al = staticInstance.loadObject(object);
				
				if (al == null) {
					continue;
				}
				
				al.keys.replaceAll((e) -> {
					if (e != null) {
						return e.replace(" ", ".").toLowerCase();
					}
					
					return e;
				});
				
				if (!hasSubBeenPosted(name, al)) {
					list.add(al);
				}
			}
			
			return list;
		}
		
		return null;
	}
	
	public static boolean hasBeenPosted(String name, InfoObject ob)
	{
		return postedIds.computeIfAbsent(name, (e) -> new ConcurrentHashMap<>()).computeIfAbsent(ob.platform,(e1) -> new CopyOnWriteArrayList<>()).contains(ob.id);
	}
	
	public static void addPostId(String name, InfoObject ob)
	{
		postedIds.computeIfAbsent(name, (e) -> new ConcurrentHashMap<>()).computeIfAbsent(ob.platform,(e1) -> new CopyOnWriteArrayList<>()).add(ob.id);
		
		if (postedIds.get(name).get(ob.platform).size() >= MAX_STORE) {
			postedIds.get(name).get(ob.platform).remove(0);
		}
	}
	
	public static boolean hasSubBeenPosted(String name, InfoObject ob)
	{
		return postedSubIds.computeIfAbsent(name, (e) -> new ConcurrentHashMap<>()).computeIfAbsent(ob.platform,(e1) -> new CopyOnWriteArrayList<>()).contains(ob.id);
	}
	
	public static void addSubPostId(String name, InfoObject ob)
	{
		postedSubIds.computeIfAbsent(name, (e) -> new ConcurrentHashMap<>()).computeIfAbsent(ob.platform,(e1) -> new CopyOnWriteArrayList<>()).add(ob.id);
		
		if (postedSubIds.get(name).get(ob.platform).size() >= MAX_STORE) {
			postedSubIds.get(name).get(ob.platform).remove(0);
		}
	}
}
