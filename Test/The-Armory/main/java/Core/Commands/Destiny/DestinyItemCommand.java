package Core.Commands.Destiny;

import Core.CommandSystem.ChatUtils;
import Core.CommandSystem.CommandObjects.ComponentSystem.ComponentResponseSystem;
import Core.CommandSystem.CommandObjects.SlashCommands.SlashCommandChannel;
import Core.CommandSystem.CommandObjects.SlashCommands.SlashCommandMessage;
import Core.CommandSystem.SlashMessageBuilder;
import Core.Commands.Destiny.DestinyDataBaseObjects.BaseObjects.BaseStatObject;
import Core.Commands.Destiny.DestinyDataBaseObjects.BaseObjects.DestinyBaseItemObject;
import Core.Commands.Destiny.DestinyDataBaseObjects.BaseObjects.SocketObject;
import Core.Commands.Destiny.DestinyDataBaseObjects.Destiny.Destiny1ItemSystem;
import Core.Commands.Destiny.DestinyDataBaseObjects.Destiny2.Destiny2ItemObject;
import Core.Commands.Destiny.DestinyDataBaseObjects.Destiny2.Destiny2ItemSystem;
import Core.Commands.Destiny.Objects.MasterworkObject;
import Core.Commands.Destiny.User.Register.Utils.Destiny2UserUtil;
import Core.Objects.Annotation.Commands.SlashArgument;
import Core.Objects.Annotation.Commands.SubCommand;
import Core.Objects.BotChannel;
import Core.Objects.Interfaces.Commands.ISlashCommand;
import com.google.common.base.Strings;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.components.ItemComponent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import org.apache.commons.lang3.text.WordUtils;

import java.awt.Color;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DestinyItemCommand
{
	//TODO Swords shouldnt have charge time
	
	public static final String[] DEL_NAMES = new String[]{
			"attack", "power", "light", "defense", "inventory size"
	};
	
	private static void run(
			Guild guild, BotChannel channel, User author, Message inputMessage, String[] args,
			int version)
	{
		String id = String.join(" ", args);
		CopyOnWriteArrayList<DestinyBaseItemObject> objects = new CopyOnWriteArrayList<>();
		
		if(id.isEmpty() || id.length() < 3){
			ChatUtils.sendEmbed(channel,author.getAsMention() + " Please input a search phrase!");
			return;
		}
		
		if (version == 1 || version == 0) {
			ArrayList<DestinyBaseItemObject> list = new ArrayList<>(Destiny1ItemSystem.getItemsByName(id));
			
			if (list != null && list.size() > 0) {
				objects.addAll(list);
			}
		}
		if (version == 2 || version == 0) {
			ArrayList<Destiny2ItemObject> list = new ArrayList<>(Destiny2ItemSystem.getItemsByName(id));
			
			list.removeIf((o) -> o.itemType == 20); //Remove dummy items
			
			if (list != null && list.size() > 0) {
				objects.addAll(list);
			}
		}
		
		objects.sort((o1, o2) -> Integer.compare(o2.destinyVersion, o1.destinyVersion));
		
		
		//If the searchword was a standalone word prioritize matches with standalone name (Example "Ace" will prioritize "Ace of Spades" over a word that contains "ace" in it)
		objects.sort((j1, j2) -> {
			Pattern p = Pattern.compile("(?i)(?:^|\\W)" + id + "(?:$|\\W)");
			Matcher m1 = p.matcher(j1.getName());
			Matcher m2 = p.matcher(j2.getName());

			boolean e1 = m1.find();
			boolean e2 = m2.find();

			return e1 && e2 ? 0 : e1 ? -1 : e2 ? 1 : 0;
		});
		
		objects.removeIf(Objects::isNull);
		
//		CopyOnWriteArrayList<DestinyBaseItemObject> finalObjects1 = objects;
		
		//TODO What did this code do? It resulted in duplicates without source being removed aka Hardlight only having D1 version
//		objects.forEach((o) -> {
//			if(finalObjects1.stream().filter((o1) -> o1 != null && o1.getName().equalsIgnoreCase(o.getName())).count() > 1 && o.source != null && !o.source.isEmpty()){
//				finalObjects1.removeIf((o2) -> o2.getName().equalsIgnoreCase(o.getName()) && (o2.source == null || o.source.isEmpty()));
//			}
//		});
		
		if (objects.size() > 5) {
			List<DestinyBaseItemObject> list = objects.subList(0, 5);
			objects = new CopyOnWriteArrayList<>(list);
		}
		
		if (objects.size() > 1) {
			StringJoiner joiner = new StringJoiner("\n");
			
			int i = 1;
			for (DestinyBaseItemObject object : objects) {
				String dlc = (object.dlc != null ? " - " + WordUtils.capitalize(object.dlc) + "" : (object instanceof Destiny2ItemObject) && ((Destiny2ItemObject)object).getSeasonName() != null ? " - " + WordUtils.capitalize(((Destiny2ItemObject)object).getSeasonName()) : "");
				
				String versionPre = "**[Destiny " + object.destinyVersion + dlc + "]**";
				String namePre = "**" + object.getName() + "** (*" + object.getItemTierAndType() + "*)";
				String sourcePre = (object.source != null && !object.source.isEmpty() ? "\n > " + (!object.source.startsWith(
						"Source: ") ? "Source: " : "") + object.source : "");
				
				if (object.destinyVersion == 2) {
					Destiny2ItemObject object1 = (Destiny2ItemObject)object;
					
					if (object1.curated) {
						namePre = "**Curated** " + namePre;
					}
				}
				
				joiner.add(
						"**" + i + "**) " + versionPre + " " + namePre + sourcePre + (sourcePre.isEmpty() ? "\n" : "\n"));
				i++;
			}
			
			EmbedBuilder builder = new EmbedBuilder();
			builder.setDescription(joiner.toString());
			builder.setTitle("Select which item you would like to view from the list below by using the below buttons.");
			
			ArrayList<ItemComponent> actions = new ArrayList<>();
			SlashCommandChannel ch = null;
			
			if(channel instanceof SlashCommandChannel) {
				ch = (SlashCommandChannel)channel;
			}
			
			for(int g = 1; g <= objects.size(); g++){
				DestinyBaseItemObject object = objects.get(g - 1);
				actions.add(ComponentResponseSystem.addComponent(author, ch != null ? ch.event : null, Button.secondary("id", Integer.toString(g)), (e) -> {
					showInfo(channel, object);
				}));
			}
			
			SlashMessageBuilder slashBuilder = ChatUtils.createSlashMessage(author, channel);
			slashBuilder.withEmbed(builder);
			slashBuilder.withActions(actions);
			slashBuilder.send();
			
		} else if (objects.size() == 1) {
			DestinyBaseItemObject object = objects.get(0);
			showInfo(channel, object);
			
		} else if (objects.size() <= 0) {
			ChatUtils.sendEmbed(channel, author.getAsMention() + " Found no items with that name!");
		}
	}
	
	public static void showInfo(BotChannel channel,  DestinyBaseItemObject object)
	{
		if (object == null || object.getName() == null || object.getName().isEmpty()) {
			ChatUtils.sendEmbed(channel, "Found no item with that name!");
			return;
		}
		
		EmbedBuilder builder = new EmbedBuilder();
		builder.setAuthor("Destiny " + object.destinyVersion, null, null);
		
		if (object.getImage() != null) {
			builder.setImage(Destiny2UserUtil.BASE_BUNGIE_URL + object.getImage());
		}
		if (object.getIcon() != null) {
			builder.setThumbnail(Destiny2UserUtil.BASE_BUNGIE_URL + object.getIcon());
		}
		if (object.getName() != null) {
			builder.setTitle(object.getName());
		}
		if (object.getDescription() != null) {
			builder.setDescription(object.getDescription());
		}
		
		builder.setColor(new Color(195, 188, 180));
		
		if (object.getItemTier() == 3) {
			builder.setColor(new Color(54, 111, 66));
		}
		if (object.getItemTier() == 4) {
			builder.setColor(new Color(80, 118, 163));
		}
		if (object.getItemTier() == 5) {
			builder.setColor(new Color(82, 47, 101));
		}
		if (object.getItemTier() == 6) {
			builder.setColor(new Color(206, 174, 51));
		}
		
		StringBuilder builder1 = new StringBuilder();
		
		for (Map.Entry<String, String> ent : object.getInfo().entrySet()) {
			builder1.append("*").append(ent.getKey()).append("*" + (ent.getKey().equals("\u200b") ? "" : ": ")).append(ent.getValue()).append("\n");
		}
		
		if(object instanceof Destiny2ItemObject){
			Destiny2ItemObject object1 = (Destiny2ItemObject)object;
			StringBuilder builder2 = new StringBuilder();
			
			if(object1.seasonInfo != null && object1.seasonInfo.size() > 0){
				for (Map.Entry<String, String> ent : object1.seasonInfo.entrySet()) {
					builder2.append("*").append(ent.getKey()).append("*: ").append(ent.getValue()).append("\n");
				}
			}
			
			if (builder2.toString().length() > 0) {
				builder.addField("Season", builder2.toString(), false);
			}
		}
		
		if(object instanceof Destiny2ItemObject){
			Destiny2ItemObject object1 = (Destiny2ItemObject)object;
			StringBuilder builder2 = new StringBuilder();
			
			if(object1.seasonInfo != null && object1.powerInfo.size() > 0){
				for (Map.Entry<String, String> ent : object1.powerInfo.entrySet()) {
					builder2.append("*").append(ent.getKey()).append("*: ").append(ent.getValue()).append("\n");
				}
			}
			
			if (builder2.toString().length() > 0) {
				builder.addField("Power", builder2.toString(), false);
			}
		}
		
		if (builder1.toString().length() > 0) {
			builder.addField("Info", builder1.toString(), false);
		}
		
		List<BaseStatObject> list = new LinkedList<>(object.getStats().values());
		list.removeIf(Objects::isNull);
		list.removeIf((o) -> o.getName() == null || o.getName().isEmpty());
		list.sort((o1, o2) -> o1.getName().compareToIgnoreCase(o2.getName()));
		list.removeIf((o) -> o.getName().equalsIgnoreCase("charge time") && list.stream().anyMatch((o1) -> o1.getName().equalsIgnoreCase("draw time")));
		Collections.reverse(list);
		
		StringBuilder stringsBuilder = new StringBuilder();
		StringBuilder valuesBuilder = new StringBuilder();
		
		int numerics = 0;
		
		top:
		for (BaseStatObject ent : list) {
			String name = ent.getName();
			
			for (String t : DEL_NAMES) {
				if (name.toLowerCase().contains(t)) {
					continue top;
				}
			}
			
			float percent = ent.value != -1 ? (float)ent.value / (float)(ent.displayMaximum > 0 ? ent.displayMaximum : 100) : 0;
			int num = Math.round(percent * 5F);
			int leftNum = 5 - num;
			
			boolean displayTime = name.toLowerCase().contains("time");
			String value = ent.value != -1 ? ent.value + (displayTime ? " ms " : "") : "X";
			
			if(displayTime && ent.value == 0) continue;
			
			//TODO Make sure non numeric stats are all displayed before any numerics
			if (name != null && !name.isEmpty()) {
				//The equippable check is for catalysts but may need to be changed
				if (object.isEquippable() || ent.value <= 0) {
					if (!ent.displayAsNumeric && num >= 0 && leftNum >= 0) {
						String text = "*" + name + "*:\n";
						String valueText = "[" + Strings.repeat(DestinySystem.BAR_FULL_ICON, num) + Strings.repeat(
								DestinySystem.BAR_EMPTY_ICON, leftNum) + "] **" + value + "**\n";
						
						if(stringsBuilder.toString().length() + text.length() >= MessageEmbed.VALUE_MAX_LENGTH
						|| valuesBuilder.toString().length() + valueText.length() >= MessageEmbed.VALUE_MAX_LENGTH){
							if (!stringsBuilder.toString().isBlank()) {
								builder.addField("Stats", stringsBuilder.toString(), true);
							}
							
							if (!valuesBuilder.toString().isBlank()) {
								builder.addField("\u200b", valuesBuilder.toString(), true);
							}
							
							if (!valuesBuilder.toString().isBlank() || !stringsBuilder.toString().isBlank()) {
								builder.addField("\u200b", "", false);
							}
							
							stringsBuilder = new StringBuilder();
							valuesBuilder = new StringBuilder();
						}
						stringsBuilder.append(text);
						valuesBuilder.append(valueText);
					}else{
						numerics++;
					}
				}
			}
		}
		
		if(numerics > 0) {
			top:
			for (BaseStatObject ent : list) {
				String name = ent.getName();
				
				for (String t : DEL_NAMES) {
					if (name.toLowerCase().contains(t)) {
						continue top;
					}
				}
				
				boolean displayTime = name.toLowerCase().contains("time");
				String value = ent.value + (displayTime ? " ms " : "");
				
				if(displayTime && ent.value == 0) continue;
				
				float percent = ent.value != -1 ? (float)ent.value / (float)(ent.displayMaximum > 0 ? ent.displayMaximum : 100) : 0;
				int num = Math.round(percent * 5F);
				int leftNum = 5 - num;
				
				if (name != null && !name.isEmpty()) {
					if (ent.displayAsNumeric || (num < 0 || leftNum < 0)) {
						String text = "*" + name + "*:\n";
						String valueText = "**" + value + "**\n";
						
						if(stringsBuilder.toString().length() + text.length() >= MessageEmbed.VALUE_MAX_LENGTH
						   || valuesBuilder.toString().length() + valueText.length() >= MessageEmbed.VALUE_MAX_LENGTH){
							if (!stringsBuilder.toString().isBlank()) {
								builder.addField("Stats", stringsBuilder.toString(), true);
							}
							
							if (!valuesBuilder.toString().isBlank()) {
								builder.addField("\u200b", valuesBuilder.toString(), true);
							}
							
							if (!valuesBuilder.toString().isBlank() || !stringsBuilder.toString().isBlank()) {
								builder.addField("\u200b", "\u200b", false);
							}
							
							stringsBuilder = new StringBuilder();
							valuesBuilder = new StringBuilder();
						}
						
						stringsBuilder.append(text);
						valuesBuilder.append(valueText);
					}
				}
			}
		}
		
		top:
		for (BaseStatObject ent : list) {
			String name = ent.getName();
			
			for (String t : DEL_NAMES) {
				if (name.toLowerCase().contains(t)) {
					continue top;
				}
			}
			
			boolean displayTime = name.toLowerCase().contains("time");
			String value = ent.value + (displayTime ? " ms " : "");
			
			if(displayTime && ent.value == 0) continue;
			
			if(!object.isEquippable() && ent.value > 0) {
				stringsBuilder.append("*" + name + "*: **" + "+" + value + "**\n");
			}
		}
		
		
		if (!stringsBuilder.toString().isBlank()) {
			builder.addField("Stats", stringsBuilder.toString(), true);
		}
		
		if (!valuesBuilder.toString().isBlank()) {
			builder.addField("\u200b", valuesBuilder.toString(), true);
		}
		
		
		//Get socket names and descriptions
		HashMap<Integer, ArrayList<SocketObject>> objects = object.getPerks();
		
		for (Map.Entry<Integer, ArrayList<SocketObject>> ent : objects.entrySet()) {
			StringJoiner joiner = new StringJoiner(" | ");
			StringBuilder builder2 = new StringBuilder();
			boolean isMod = false;
			
			int cur = 1;
			for (SocketObject socket : ent.getValue()) {
				String subDesc = "";
				
				if(socket.hash != null) {
					DestinyBaseItemObject perkObject = null;
					
					if (object.destinyVersion == 1) {
						perkObject = Destiny1ItemSystem.destinyItemObjects.getOrDefault(socket.hash.intValue(), null);
						
					} else if (object.destinyVersion == 2) {
						perkObject = Destiny2ItemSystem.destinyItemObjects.getOrDefault(socket.hash.intValue(), null);
					}
					
					if (perkObject != null) {
						Collection<BaseStatObject> stats;
						
						if(object.destinyVersion == 2){
							stats = ((Destiny2ItemObject)perkObject).getStats((Destiny2ItemObject)object).values();
							if(((Destiny2ItemObject)perkObject).itemTypeDisplayName.toLowerCase().contains("mod")) {
								isMod = true;
							}
							
						}else{
							stats = perkObject.getStats().values();
						}
						
						if (stats != null) {
							if (stats.size() > 0) {
								StringJoiner joiner1 = new StringJoiner("\n");
								
								for (BaseStatObject stat : stats) {
									if(stat.getName() == null) continue;
									joiner1.add("*" + stat.getName() + "*: **" + (stat.value > 0 ? "+" : "") + stat.value + "**");
								}
								
								subDesc = joiner1.toString();
							}
							
							if (subDesc.length() > 1) {
								subDesc = subDesc.substring(0, subDesc.length() - 1);
							}
						}
					}
				}
				
				
				if(socket != null){
					if (socket.name != null && socket.description != null) {
						
						if(socket.socketGroup == 8) continue;
						
						if (!socket.name.isEmpty() && !socket.description.isEmpty()) {
							//TODO Fix these more
							if (joiner.toString().length() + socket.name.length() >= MessageEmbed.TITLE_MAX_LENGTH) {
								break;
							}
							
							if (builder2.toString().length() + socket.description.length() >= MessageEmbed.VALUE_MAX_LENGTH) {
								break;
							}
							
							boolean isMulti = ent.getValue().size() > 1;
							
							String desc = socket.description;
							
							if (desc.startsWith("\n")) {
								desc = desc.replaceFirst("\n", "");
							}
							
							if(!subDesc.isEmpty()) {
								if (!(builder2.toString().length() + socket.description.length() + subDesc.length() >= MessageEmbed.VALUE_MAX_LENGTH)) {
									desc += "*\n" + subDesc;
								}
							}
							
							joiner.add((isMulti ? (cur + " - ") : "") + socket.name);
							builder2.append(isMulti ? (cur + ") ") : "").append("*").append(desc).append("*\n\n");
						}
					}
				}
		
				
				cur += 1;
			}
			
			if (joiner.toString().length() > 0 && builder2.toString().length() > 0) {
				builder.addField((isMod ? "Mod: **" : "[") + joiner + (isMod ? "**" : "]"), builder2.toString(), false);
			}
		}
		
		
		if (object.destinyVersion == 2) {
			Destiny2ItemObject d2Object = (Destiny2ItemObject)object;
			
			//TODO This needs more work
//			if (d2Object.catalyst != null) {
//				CatalystObject cbOb = d2Object.catalyst;
//				builder.addField("Catalyst - " + cbOb.name,
//				                 "*" + cbOb.description + ((cbOb.stats.length() > 0 ? ("\n\n**Stats**:\n" + cbOb.stats) : "")) + "*" + (!cbOb.unlock.isEmpty() ? "\n\n**Unlock**: *" + cbOb.unlock + "*" : ""),
//				                 false);
//			}
			
			if (d2Object.masterworkObjects.size() > 0) {
				StringJoiner joiner = new StringJoiner(", ");
				
				for (MasterworkObject object1 : d2Object.masterworkObjects) {
					joiner.add("`" + object1.statName + " " + object1.name + "`");
				}
				
				builder.addField("Masterworks", "" + joiner + "", false);
			}
		}
		
		if (object.source != null && !object.source.isEmpty()) {
			String text = object.source;
			
			if (text.startsWith("Source: ")) {
				text = text.substring("Source: ".length());
			}
			
			builder.addField("Source", "***" + text + "***", false);
		}
		
		ChatUtils.sendMessage(channel, builder.build());
	}
	
	@SubCommand( parent = DestinySlashCommand.class)
	public static class DestinyItem implements ISlashCommand
	{
		@SlashArgument( key = "version", text = "Which version of destiny to search", choices = {"Both", "Destiny 1", "Destiny 2"})
		public int destinyVersion = 0;
		
		@SlashArgument( key = "item", text = "The item to search for", required = true )
		public String item;
		
		@Override
		public void slashCommandExecuted(SlashCommandInteractionEvent slashEvent, Guild guild, User author, SlashCommandChannel channel, SlashCommandMessage message)
		{
			run(channel.getGuild(), channel, message.getAuthor(), message, new String[]{item}, destinyVersion);
		}
		
		@Override
		public String getDescription()
		{
			return "Allows searching for items in Destiny 1 & 2";
		}
		
		@Override
		public String getSlashName()
		{
			return "item";
		}
	}
}
