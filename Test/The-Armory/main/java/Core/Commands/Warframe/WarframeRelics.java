package Core.Commands.Warframe;

import Core.CommandSystem.ChatUtils;
import Core.CommandSystem.CommandObjects.SlashCommands.SlashCommandChannel;
import Core.CommandSystem.CommandObjects.SlashCommands.SlashCommandMessage;
import Core.Commands.Warframe.Objects.DropObject;
import Core.Main.Logging;
import Core.Main.Startup;
import Core.Objects.Annotation.Commands.SlashArgument;
import Core.Objects.Annotation.Commands.SubCommand;
import Core.Objects.Annotation.Method.Startup.Init;
import Core.Objects.CustomEntry;
import Core.Objects.Interfaces.Commands.ISlashCommand;
import Core.Util.FileUtil;
import Core.Util.JsonUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.apache.commons.lang3.text.WordUtils;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Stream;

@SubCommand( parent = Warframe.WarframeInfo.class)
public class WarframeRelics implements ISlashCommand
{
	public static final ConcurrentHashMap<String, Integer[]> dropEntries = new ConcurrentHashMap<>();
	public static final ConcurrentHashMap<String, Integer[]> placeEntries = new ConcurrentHashMap<>();
	public static File entryFile;
	
	public static boolean contains(String name)
	{
		String itemNameClear = name.toLowerCase().contains("relic") ? name.substring(0, name.toLowerCase().indexOf("relic") - 1) : name;
		itemNameClear = itemNameClear + " Relic";
		
		
		if (placeEntries.containsKey(itemNameClear.toLowerCase())) {
			Integer[] entries = placeEntries.get(itemNameClear.toLowerCase());
			
			ArrayList<DropObject> objects = new ArrayList<>();
			getDropObjects(objects, entries);
			
			for (DropObject ob : objects) {
				if (ob.refinedState != null && !ob.refinedState.isEmpty()) {
					return true;
				}
			}
		}
		
		return false;
	}
	
	@Init
	public static void init()
	{
		entryFile = FileUtil.getFile(Startup.FilePath + "/warframe/temp.txt");
		
		try {
			entryFile.delete();
			entryFile.createNewFile();
		} catch (IOException e) {
			Logging.exception(e);
		}
	}
	
	protected static void getDropObjects(ArrayList<DropObject> objects, Integer[] ent)
	{
		for (Integer in : ent) {
			try (Stream<String> lines = Files.lines(entryFile.toPath())) {
				Optional<String> opt = lines.skip(in - 1).findFirst();
				
				if (opt != null && opt.isPresent()) {
					String line = opt.orElseGet(null);
					
					if (line != null) {
						DropObject object = JsonUtils.getGson_non_pretty().fromJson(line, DropObject.class);
						
						if (object != null) {
							objects.add(object);
						}
					}
				}
				
			} catch (IOException e) {
				Logging.exception(e);
			}
		}
	}
	
	public static ArrayList<DropObject> getDrops(String name)
	{
		String procName = name.toLowerCase().replace(" ", "_");
		
		ArrayList<DropObject> objects = new ArrayList<>();
		
		if (dropEntries.containsKey(procName)) {
			Integer[] entries = dropEntries.get(procName);
			getDropObjects(objects, entries);
		}
		
		return objects;
	}
	
	@Override
	public String getDescription()
	{
		return "Shows which items can be aquired from a specific relic aswell as where the relic can be found.";
	}
	
	@Override
	public String getSlashName()
	{
		return "relic";
	}
	
	@SlashArgument( key = "item", text = "Which relic you want to search for", required = true)
	public String itemName;
	
	@Override
	public void slashCommandExecuted(SlashCommandInteractionEvent slashEvent, Guild guild, User author, SlashCommandChannel channel, SlashCommandMessage message)
	{
		String itemNameClear = itemName.toLowerCase().contains("relic") ? itemName.substring(0, itemName.toLowerCase().indexOf("relic") - 1) : itemName;
		String itemSearch = itemName.contains("relic") ? itemName : itemName + " Relic";
		
		boolean found = false;
		
		ConcurrentHashMap<String, CopyOnWriteArrayList<CustomEntry<String, DropObject>>> drops = new ConcurrentHashMap<>();
		CopyOnWriteArrayList<DropObject> dropSources = new CopyOnWriteArrayList<>();
		
		dropSources.addAll(getDrops(itemNameClear));
		dropSources.addAll(getDrops(itemSearch));
		
		for (Map.Entry<String, Integer[]> ent : placeEntries.entrySet()) {
			if (ent.getKey().equalsIgnoreCase(itemSearch) || ent.getKey().equalsIgnoreCase(itemNameClear)) {
				
				ArrayList<DropObject> objects = new ArrayList<>();
				getDropObjects(objects, ent.getValue());
				
				for (DropObject ob : objects) {
					if (ob.place.equalsIgnoreCase(itemSearch) || ob.place.equalsIgnoreCase(itemNameClear)) {
						if (ob.refinedState != null && !ob.refinedState.isEmpty()) {
							if (!drops.containsKey(ob.refinedState)) {
								drops.put(ob.refinedState, new CopyOnWriteArrayList<>());
							}
							
							drops.get(ob.refinedState).add(new CustomEntry<>(ob.name, ob));
							itemName = ob.place;
							found = true;
						}
					}
				}
			}
		}
		
		dropSources.sort(Comparator.comparingDouble(e -> e.chance));
		Collections.reverse(dropSources);
		
		if (itemName == null || !found) {
			ChatUtils.sendEmbed(channel, author.getAsMention() + " Found no relics with that name!");
			return;
		}
		
		StringJoiner joiner = new StringJoiner("\n");
		StringJoiner title = new StringJoiner(" | ", "(", ")");
		
		ConcurrentHashMap<String, ConcurrentHashMap<String, Double>> map = new ConcurrentHashMap<>();
		for (Map.Entry<String, CopyOnWriteArrayList<CustomEntry<String, DropObject>>> ent : drops.entrySet()) {
			title.add(WordUtils.capitalize(ent.getKey()));
			
			for (CustomEntry<String, DropObject> object : ent.getValue()) {
				if (!map.containsKey(object.getKey())) {
					map.put(object.getKey(), new ConcurrentHashMap<>());
				}
				
				map.get(object.getKey()).put(ent.getKey(), object.getValue().chance);
			}
		}
		
		//TODO Sort by highest base chance?
		for (Map.Entry<String, ConcurrentHashMap<String, Double>> ent : map.entrySet()) {
			String wikiLink = Warframe.getWikiLink(ent.getKey());
			String item = WordUtils.capitalize(ent.getKey());
			
			if (wikiLink != null) {
				item = "[" + item + "](" + wikiLink + ")";
			}
			
			StringJoiner joiner1 = new StringJoiner(" | ");
			
			for (Map.Entry<String, Double> dd : ent.getValue().entrySet()) {
				joiner1.add("**" + ((int)(dd.getValue() * 1)) + "%**");
			}
			
			item += " (" + joiner1.toString() + ")";
			
			joiner.add(item);
		}
		
		EmbedBuilder builder = new EmbedBuilder();
		builder.setColor(Color.cyan);
		
		String wikiImage = Warframe.getThumbnail(itemName);
		String wikiLink = Warframe.getWikiLink(itemName);
		
		if (wikiImage != null && !wikiImage.isEmpty()) {
			builder.setThumbnail(wikiImage);
		}
		
		builder.setTitle(itemName);
		
		if (wikiLink != null && !wikiLink.isEmpty()) {
			builder.setTitle(itemName, wikiLink);
		}
		
		if (joiner.length() > 0) {
			builder.addField("Rewards " + title.toString(), joiner.toString(), false);
		}
		
		
		StringJoiner sources = new StringJoiner("\n");
		ConcurrentHashMap<String, CopyOnWriteArrayList<DropObject>> list = new ConcurrentHashMap<>();
		
		for (DropObject object : dropSources) {
			String key = object.rotation != null ? object.rotation : "";
			
			if (!list.containsKey(key)) {
				list.put(key, new CopyOnWriteArrayList<>());
			}
			
			list.get(key).add(object);
		}
		
		boolean moreFound = false;
		for (Map.Entry<String, CopyOnWriteArrayList<DropObject>> ent : list.entrySet()) {
			if (!ent.getKey().isEmpty()) {
				sources.add("\n*Rotation " + ent.getKey() + "*");
			}
			
			ArrayList<String> placeUsed = new ArrayList<>();
			
			int i = 0;
			for (DropObject tt : ent.getValue()) {
				String t = "``" + tt.place + "`` (**" + tt.chance + "%**)";
				
				if (placeUsed.contains(tt.place)) {
					continue;
				}
				
				if (sources.length() + t.length() >= (MessageEmbed.VALUE_MAX_LENGTH - 10) || i >= 5) {
					moreFound = true;
					break;
				} else {
					sources.add(t);
					placeUsed.add(tt.place);
					i++;
				}
			}
		}
		
		if (moreFound) {
			sources.add("\n*For more detailed info on sources use `/warframe info drops`*");
		}
		
		if (sources.length() > 0) {
			builder.addField("Sources", sources.toString(), false);
		}
		
		ChatUtils.sendMessage(channel, builder.build());
	}
}
