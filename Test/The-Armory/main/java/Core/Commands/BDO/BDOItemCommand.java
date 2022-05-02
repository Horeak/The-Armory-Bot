package Core.Commands.BDO;

import Core.CommandSystem.ChatUtils;
import Core.CommandSystem.CommandObjects.ComponentSystem.ComponentResponseSystem;
import Core.CommandSystem.CommandObjects.SlashCommands.SlashCommandChannel;
import Core.CommandSystem.CommandObjects.SlashCommands.SlashCommandMessage;
import Core.CommandSystem.SlashMessageBuilder;
import Core.Commands.BDO.BDOItem.BDOItemData;
import Core.Commands.BDO.BDOItem.BDOItemEnhancement;
import Core.Objects.Annotation.Commands.SlashArgument;
import Core.Objects.Annotation.Commands.SubCommand;
import Core.Objects.BotChannel;
import Core.Objects.Interfaces.Commands.ISlashCommand;
import Core.Util.Utils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.components.ItemComponent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;

import java.awt.Color;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.*;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@SubCommand( parent = BDOSlashCommand.class)
public class BDOItemCommand implements ISlashCommand
{
	@SlashArgument( key = "enhancement", text = "Which enhancement level to show.", choices = {"PEN", "TET", "TRI", "DUO", "PRI",
	                                                                                           "+15", "+14", "+13", "+12", "+11",
	                                                                                           "+10", "+9", "+8", "+7", "+6", "+5",
	                                                                                           "+4", "+3", "+2", "+1"})
	public String enhancement;
	
	@SlashArgument( key = "item", text = "The item you wish to look up", required = true )
	public String item;
	
	
	static void displayStats(BDOItemEnhancement enhancement, StringBuilder sBuilder1)
	{
		if(enhancement.damage != null && !enhancement.damage.isBlank() && !enhancement.damage.equals("0") && !enhancement.damage.equals("0 ~ 0")){
			sBuilder1.append("*__Damage__*: *" + enhancement.damage + "*\n");
		}
		
		if(enhancement.defense != null && !enhancement.defense.isBlank() && !enhancement.defense.equals("0") && !enhancement.defense.equals("0 ~ 0")){
			sBuilder1.append("*__Defense__*: *" + enhancement.defense + "*\n");
		}
		
		if(enhancement.accuracy != null && !enhancement.accuracy.isBlank() && !enhancement.accuracy.equals("0") && !enhancement.accuracy.equals("0 ~ 0")){
			sBuilder1.append("*__Accuracy__*: *" + enhancement.accuracy + "*\n");
		}
		
		if(enhancement.evasion != null && !enhancement.evasion.isBlank() && !enhancement.evasion.equals("0") && !enhancement.evasion.equals("0 ~ 0")){
			if(enhancement.hevasion != null && !enhancement.hevasion.isBlank() && !enhancement.hevasion.equals("0") && !enhancement.hevasion.equals("0 ~ 0")){
				sBuilder1.append("*__Evasion__*: *" + enhancement.evasion + "* (*" + enhancement.hevasion + "*)");
			}else{
				sBuilder1.append("*__Evasion__*: *" + enhancement.evasion + "*");
			}
			sBuilder1.append("\n");
		}
		
		if(enhancement.dreduction != null && !enhancement.dreduction.isBlank() && !enhancement.dreduction.equals("0") && !enhancement.dreduction.equals("0 ~ 0")){
			if(enhancement.hdreduction != null && !enhancement.hdreduction.isBlank() && !enhancement.hdreduction.equals("0") && !enhancement.hdreduction.equals("0 ~ 0")){
				sBuilder1.append("*__Damage Reduction__*: *" + enhancement.dreduction + "* (*" + enhancement.hdreduction + "*)");
			}else{
				sBuilder1.append("*__Damage Reduction__*: *" + enhancement.dreduction + "*");
			}
		}
		sBuilder1.append("\n");
	}
	
	@Override
	public String getDescription()
	{
		return "Allows you to look up stats and effects of items in BDO";
	}
	
	@Override
	public void slashCommandExecuted(SlashCommandInteractionEvent slashEvent, Guild guild, User author, SlashCommandChannel channel, SlashCommandMessage message)
	{
		int enhance_level = enhancement != null ? BDOItemUtils.getEnhanceLevel(enhancement) : 0;
		
		ArrayList<String> nameList = new ArrayList<>(BDOItemIndex.ITEM_INDEX.values());
		HashMap<String, Integer> keyList = new HashMap<>();
		BDOItemIndex.ITEM_INDEX.forEach((s1, s2) -> keyList.put(s2, s1));
		
		nameList.removeIf((ob) -> {
			String j1 = BDOItemIndex.fullCleanString(ob).toLowerCase().replace(" ", "");
			String j2 = item.toLowerCase().replace(" ", "");
			
			return !j1.contains(j2) && !j1.equals(j2);
		});
		
		if(enhance_level > 0){
			int finalEnhance_level1 = enhance_level;
			
			nameList.removeIf((o) -> {
				int itemId = keyList.get(o);
				
				if(BDOItemIndex.ITEM_ENHANCEMENTS.containsKey(itemId)){
					int eh = finalEnhance_level1;
					int enhance = BDOItemIndex.ITEM_ENHANCEMENTS.get(itemId);
					
					if (enhance <= 5) {
						return eh < 16 || eh > (enhance + 15);
					}
					
					return eh > enhance;
				}else{
					return true;
				}
			});
		}
		
		nameList = new ArrayList<>(nameList.stream().distinct().collect(Collectors.toList()));
		
		nameList.sort((j1, j2) -> {
			Pattern p = Pattern.compile("(?i)(?:^|\\W)" + item + "(?:$|\\W)");
			Matcher m1 = p.matcher(BDOItemIndex.fullCleanString(j1));
			Matcher m2 = p.matcher(BDOItemIndex.fullCleanString(j2));
			
			boolean e1 = m1.find();
			boolean e2 = m2.find();
			
			return e1 && e2 ? 0 : e1 ? -1 : e2 ? 1 : 0;
		});
		
		nameList.sort(
				Comparator.comparingDouble(o -> Utils.compareStrings(BDOItemIndex.fullCleanString(o).toLowerCase().replace(" ", ""), item.toLowerCase().replace(" ", ""))));
		
		if (nameList.size() > 5) {
			List<String> list = nameList.subList(0, 5);
			nameList = new ArrayList<>(list);
		}
		
		if (nameList.size() > 1) {
			StringJoiner joiner = new StringJoiner("\n");
			
			int i = 1;
			for (String object : nameList) {
				String enhancePrefix = "";
				
				int itemId = keyList.get(object);
				
				int enhanceVal = enhance_level;
				
				if(BDOItemIndex.ITEM_ENHANCEMENTS.containsKey(itemId)) {
					int enhance = BDOItemIndex.ITEM_ENHANCEMENTS.get(itemId);
					enhanceVal = BDOItemUtils.getEnhanceIndex(enhance, enhanceVal);
					enhancePrefix = BDOItemUtils.getEnhancePrefix(enhanceVal, enhance);
				}
				
				joiner.add("**" + i + "**) [*" + enhancePrefix + object + "*](" + BDOItemIndex.BASE_URL + "us/item/" + itemId + ")");
				i++;
			}
			
			EmbedBuilder builder = new EmbedBuilder();
			
			int finalEnhance_level = enhance_level;
			
			builder.setTitle(
					"Select which item you would like to view from the buttons below");
			
			ArrayList<ItemComponent> actionRow = new ArrayList<>();
			
			SlashCommandMessage sh = null;
			SlashCommandChannel ch = null;
			
			if(message instanceof SlashCommandMessage){
				sh = (SlashCommandMessage)message;
				ch = (SlashCommandChannel)sh.getChannel();
			}
			
			for (String object : nameList) {
				String enhancePrefix = "";
				int itemId = keyList.get(object);
				
				int enhanceVal = enhance_level;
				
				if(BDOItemIndex.ITEM_ENHANCEMENTS.containsKey(itemId)) {
					int enhance = BDOItemIndex.ITEM_ENHANCEMENTS.get(itemId);
					enhanceVal = BDOItemUtils.getEnhanceIndex(enhance, enhanceVal);
					enhancePrefix = BDOItemUtils.getEnhancePrefix(enhanceVal, enhance);
				}
				
				String label = enhancePrefix + " " + object;
				
				actionRow.add(ComponentResponseSystem.addComponent(slashEvent.getUser(), ch != null ? ch.event : null, Button.secondary("id", label), (e) -> {
					BotChannel chan = channel;
					
					if(message instanceof SlashCommandMessage){
						SlashCommandMessage mesg = new SlashCommandMessage(e);
						chan = (BotChannel)mesg.getChannel();
					}
					
					showInfo(chan, object, itemId, finalEnhance_level, null);
				}));
			}
			
			SlashMessageBuilder slashBuilder = ChatUtils.createSlashMessage(slashEvent.getUser(), channel);
			slashBuilder.withEmbed(builder);
			slashBuilder.setMultiRow();
			slashBuilder.withActions(actionRow);
			slashBuilder.send();
			
		} else if (nameList.size() == 1) {
			String object = nameList.get(0);
			int itemId = keyList.get(object);
			
			if(message instanceof SlashCommandMessage){
				showInfo(channel, object, itemId, enhance_level, null);
			}else{
				startInfo(channel, object, itemId, enhance_level);
			}
		} else if (nameList.size() <= 0) {
			ChatUtils.sendEmbed(channel, slashEvent.getUser().getAsMention() + " Found no items with that name!");
		}
	}
	
	public void startInfo(BotChannel channel,  String object, int id, int enhance_level)
	{
		ChatUtils.sendEmbed(channel, "Retrieving item info. This may take a moment", (ms, er) -> showInfo(channel, object, id, enhance_level, ms));
	}
	
	public void showInfo(BotChannel channel, String object, int id, int enhance_level, Message ms)
	{
		BDOItem sItem = BDOItemUtils.getItem(id);
		
		if(sItem == null || sItem.data == null){
			ChatUtils.sendEmbed(channel, "Unable to retrieve the item info. This may be due to an issue on the website. Please try again later");
			ChatUtils.deleteMessage(ms);
			return;
		}
		
		BDOItemData itemData = sItem.data;
		if(itemData != null){
			String name = BDOItemUtils.processItemName(itemData, itemData.name, enhance_level);
			int enhance = BDOItemUtils.getEnhanceIndex(itemData, enhance_level);
			BDOItemEnhancement enhancement = BDOItemUtils.getEnhanceObject(itemData, enhance);
			
			EmbedBuilder builder = new EmbedBuilder();
			
			if(itemData.grade == 0){
				builder.setColor(new Color(189, 189, 189));
			}else if (itemData.grade == 1){
				builder.setColor(new Color(128, 161, 66));
			}else if (itemData.grade == 2){
				builder.setColor(new Color(60, 124, 178));
			}else if (itemData.grade == 3){
				builder.setColor(new Color(234, 178, 56));
			}else if (itemData.grade == 4){
				builder.setColor(new Color(189, 86, 66));
			}
			
			builder.setTitle(name, BDOItemIndex.BASE_URL + "us/item/" + id + "/#" + enhance);
			
			if(itemData.icon != null){
				builder.setThumbnail(itemData.icon);
			}
			
			StringBuilder sBuilder = new StringBuilder();
			
			boolean validEnhance = false;
			
			if(enhancement != null){
				validEnhance = true;
				displayStats(enhancement, sBuilder);
			}

			if(itemData.specialInfo != null && itemData.specialInfo.size() > 0){
				for(String t : itemData.specialInfo){
					sBuilder.append("__" + t + "__\n");
				}
				
				sBuilder.append("\n");
			}
			
			if(itemData.requiredClass != null && !itemData.requiredClass.isBlank()){
				sBuilder.append("*" + itemData.requiredClass + " Exclusive*\n\n");
			}
			
			if(itemData.description.size() > 0){
				sBuilder.append("- **Description**: \n");
				
				for(String t : itemData.description){
					sBuilder.append("*" + t + "*" + "\n\n");
				}
			}
			
			if (itemData.itemEffects.size() > 0 || validEnhance) {
				List<Entry<String, ArrayList<String>>> list = new LinkedList<>(itemData.itemEffects.entrySet());
				
				if(validEnhance){
					list = new LinkedList<>(enhancement.itemEffects.entrySet());
				}
				
				list.sort((o1, o2) -> {
					char j1 = o1.getKey().charAt(0);
					char j2 = o2.getKey().charAt(0);
					
					boolean k1 = Character.isDigit(j1);
					boolean k2 = Character.isDigit(j2);
					
					return k1 && k2 ? 0 : k1 && !k2 ? 1 : !k1 && k2 ? -1 : 0;
				});
				
				
				LinkedHashMap<String, ArrayList<String>> map = new LinkedHashMap<>();
				list.forEach(s -> map.put(s.getKey(), s.getValue()));
				
				for (Entry<String, ArrayList<String>> ent : map.entrySet()) {
					StringBuilder sk = new StringBuilder();
					ent.getValue().forEach(s -> sk.append("*" + s + "*\n"));
					builder.addField(" - **" + ent.getKey() + "**:", sk.toString(), false);
				}
			}
			
			StringBuilder marketPrice = new StringBuilder();
			
			DecimalFormat formatter = (DecimalFormat) NumberFormat.getInstance(Locale.US);
			DecimalFormatSymbols symbols = formatter.getDecimalFormatSymbols();
			
			symbols.setGroupingSeparator(',');
			formatter.setDecimalFormatSymbols(symbols);
			
			if(itemData.EU_marketPrice != null){
				Long price = BDOItemUtils.getItemPrice(itemData.EU_marketPrice, enhance);
				if(price != null) marketPrice.append("**EU**: *" + formatter.format(price) + "* Silver\n");
			}
			
			if(itemData.NA_marketPrice != null){
				Long price = BDOItemUtils.getItemPrice(itemData.NA_marketPrice, enhance);
				if(price != null) marketPrice.append("**NA**: *" + formatter.format(price) + "* Silver\n");
			}
			
			if(!marketPrice.toString().isBlank()){
				builder.addField("Marketplace price", marketPrice.toString(), false);
			}
			
			if(itemData.vendorSell != null || itemData.repairCost != null){
				StringBuilder vendor = new StringBuilder();
				
				if(itemData.vendorSell != null){
					vendor.append("**Sell price**: *" + formatter.format(itemData.vendorSell) + "* Silver\n");
				}
				
				if(itemData.repairCost != null){
					vendor.append("**Repair cost**: *" + formatter.format(itemData.repairCost) + "* Silver\n");
				}
				
				if(!vendor.toString().isBlank()) {
					builder.addField("Vendor prices", vendor.toString(), false);
				}
			}
			
			builder.setDescription(sBuilder.toString());
			
			if(ms == null){
				ChatUtils.sendMessage(channel, builder.build());
			}else {
				ChatUtils.editMessage(ms, "", builder.build());
			}
			
		}else{
			if(ms == null){
				ChatUtils.sendMessage(channel, "Found no item with that name!");
				
			}else {
				ChatUtils.editMessage(ms, "Found no item with that name!");
			}
		}
	}
	
	@Override
	public String getSlashName()
	{
		return "item";
	}
	
}
