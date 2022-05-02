package Core.Commands.Warframe;

import Core.CommandSystem.ChatUtils;
import Core.CommandSystem.CommandObjects.SlashCommands.SlashCommandChannel;
import Core.CommandSystem.CommandObjects.SlashCommands.SlashCommandMessage;
import Core.Commands.Warframe.Objects.DropObject;
import Core.Commands.Warframe.Objects.WikiItemObject;
import Core.Main.Logging;
import Core.Objects.Annotation.Commands.SlashArgument;
import Core.Objects.Annotation.Commands.SubCommand;
import Core.Objects.Interfaces.Commands.ISlashCommand;
import Core.Util.ConnectionUtils;
import Core.Util.Utils;
import com.google.common.base.Strings;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.awt.Color;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SubCommand(parent = Warframe.WarframeInfo.class)
public class WarframeWikiCommand implements ISlashCommand
{
	//TODO Detect version of wikiitem to use to update
	public static final CopyOnWriteArrayList<WikiItemObject> wikiItems = new CopyOnWriteArrayList<>();
	
	public static boolean contains(String name)
	{
		return getObject(name) != null;
	}
	
	public static WikiItemObject getObject(String name)
	{
		for (WikiItemObject ob : wikiItems) {
			if (ob.name.replace(" ", "_").equalsIgnoreCase(name.replace(" ", "_"))) {
				return ob;
			}
		}
		
		return null;
	}
	
	public static boolean hasItem(String name)
	{
		return getWikiItemObject(name) != null;
	}
	
	
	public static WikiItemObject getWikiItemObject(String itemName)
	{
		WikiItemObject item = getObject(itemName);
		
		if (item == null) {
			String wikiLink = Warframe.getWikiLink(itemName, false);
			
			if (wikiLink != null) {
				try {
					Document doc1 = ConnectionUtils.getDocument(wikiLink);
					doc1.removeClass("img").removeAttr("img");
					doc1.select("img").remove();
					
					if (doc1 != null) {
						String name = null;
						String description = null;
						HashMap<String, HashMap<String, String>> map = new HashMap<>();
						
						if (name == null) {
							Elements es_name = doc1.select("meta[property=og:title]");
							
							if (es_name != null) {
								name = es_name.attr("content");
							}
						}
						
						Elements es_desc = doc1.select("meta[property=og:description]");
						
						//TODO shows the proper description which is shown on the wiki instead of the meta description
						
						if (es_desc != null) {
							description = es_desc.attr("content");
						}
						
						Elements infoBox = doc1.select(".portable-infobox");
						
						if (infoBox.size() <= 1) {
							Elements sectionsDiv = doc1.select(".portable-infobox > section");
							Elements nonSectionsDiv = doc1.select(".portable-infobox > .pi-item:not(.pi-group)");
							
							//TODO Why the fuck wont ".portable-infobox > h2 .pi-item .pi-header" work!?
							Elements nonSectionsHeader = doc1.select(".portable-infobox > h2");
							
							for (Element e : sectionsDiv) {
								Element eN = e.selectFirst(".pi-header");
								String sectionName = eN.text();
								
								Elements data = e.select(".pi-item .pi-data:has(h3)");
								
								for (Element es : data) {
									parseInfo(map, sectionName, es);
								}
							}
							
							//TODO Fix
							if (nonSectionsHeader != null) {
								if (nonSectionsHeader.size() > 0) {
									String t = nonSectionsHeader.get(nonSectionsHeader.size() > 1 ? 1 : 0).text();
									
									for (Element es : nonSectionsDiv) {
										parseInfo(map, t, es);
									}
								}
							}
						}
						
						if (name != null && description != null) {
							String image = Warframe.getThumbnail(name, false);
							
							WikiItemObject object = new WikiItemObject(name, description, image, map);
							wikiItems.add(object);
							
							item = object;
						}
					}
					
				} catch (IOException e) {
					Logging.exception(e);
				}
			}
		}
		return item;
	}
	
	protected static void parseInfo(
			HashMap<String, HashMap<String, String>> map,  String sectionName, Element es)
	{
		Elements key = es.select(".pi-data-label");
		Elements value = es.select(".pi-data-value");
		
		String keyText = null, valueText = null;
		
		if (key != null) {
			if (key.hasAttr("title")) {
				keyText = key.attr("title");
			} else {
				keyText = key.text();
			}
		}
		
		if (value != null) {
			for (Element es4 : value.select("a > img[alt]")) {
				es4.text(es4.attr("alt"));
			}
			
			String tmp = value.html().replace("<br>", "[n]");
			valueText = Jsoup.parseBodyFragment(tmp).text();
		}
		
		if (keyText != null && valueText != null) {
			if (valueText.contains("---")) {
				return;
			}
			
			if (valueText.contains(" Pol")) {
				Pattern pattern = Pattern.compile("(\\w+)\\s+Pol");
				Matcher matcher = pattern.matcher(valueText);
				
				while (matcher.find()) {
					valueText = valueText.replaceFirst(matcher.group(), "Pol:" + matcher.group(1));
				}
			}
			
			if (valueText.contains(" b ")) {
				Pattern pattern = Pattern.compile(" (\\w+)\\s+b\\s+(\\d+%)");
				Matcher matcher = pattern.matcher(valueText);
				
				while (matcher.find()) {
					valueText = valueText.replaceFirst("\\(" + matcher.group() + "\\)", "");
				}
			}
			
			if (sectionName == null || sectionName.isEmpty()) {
				return;
			}
			
			if (!map.containsKey(sectionName)) {
				map.put(sectionName, new HashMap<>());
			}
			
			if (keyText == null || keyText.isEmpty()) {
				return;
			}
			if (valueText == null || valueText.isEmpty()) {
				return;
			}
			
			map.get(sectionName).put(keyText, valueText);
		}
	}
	@Override
	public String getSlashName()
	{
		return "wiki";
	}
	
	@SlashArgument(key = "item", text = "What info you want to look for from the warframe wiki", required = true)
	public String itemName;
	
	@Override
	public void slashCommandExecuted(SlashCommandInteractionEvent slashEvent, Guild guild, User author, SlashCommandChannel channel, SlashCommandMessage message)
	{
		WikiItemObject item = getWikiItemObject(itemName);
		
		if (item == null) {
			ChatUtils.sendEmbed(channel, author.getAsMention() + " Found no item with that name!");
			return;
		}
		
		CopyOnWriteArrayList<DropObject> dropSources = new CopyOnWriteArrayList<>(WarframeRelics.getDrops(item.name));
		dropSources.sort(Comparator.comparingDouble(e -> e.chance));
		Collections.reverse(dropSources);
		
		StringJoiner sources = new StringJoiner("\n");
		ConcurrentHashMap<String, CopyOnWriteArrayList<DropObject>> list = new ConcurrentHashMap<>();
		
		for (DropObject object1 : dropSources) {
			String key = object1.rotation != null ? object1.rotation : "";
			
			if (!list.containsKey(key)) {
				list.put(key, new CopyOnWriteArrayList<>());
			}
			
			list.get(key).add(object1);
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
		
		boolean hasSource = sources.length() > 0;
		
		
		EmbedBuilder builder = new EmbedBuilder();
		builder.setColor(Color.cyan);
		
		String wikiLink = Warframe.getWikiLink(item.name, false);
		
		if (item.image != null) {
			if(!item.image.isBlank() && item.image.startsWith("http")) {
				builder.setThumbnail(item.image);
			}
		}
		
		builder.setTitle(item.name);
		
		if (wikiLink != null) {
			builder.setTitle(item.name, wikiLink);
		}
		
		builder.setDescription("*" + item.description + "*");
		
		for (Map.Entry<String, HashMap<String, String>> ent1 : item.info.entrySet()) {
			
			StringBuilder joiner = new StringBuilder();
			for (Map.Entry<String, String> ent : ent1.getValue().entrySet()) {
				String key = ent.getKey();
				String value = ent.getValue();
				
				if (key == null || key.isEmpty()) {
					continue;
				}
				if (value == null || value.isEmpty()) {
					continue;
				}
				
				String prefix = "";
				if (key.equalsIgnoreCase("source")) {
					if (hasSource) {
						continue;
					}
				}
				
				
				if (value.toLowerCase().contains("credits")) {
					Pattern pattern = Pattern.compile("Credits(\\w+)");
					Matcher matcher = pattern.matcher(value);
					
					while (matcher.find()) {
						value = value.replaceFirst(matcher.group(), Warframe.credits_icon);
					}
				}
				
				if (value.contains("Pol:")) {
					Pattern pattern = Pattern.compile("Pol:(\\w+)");
					Matcher matcher = pattern.matcher(value);
					
					while (matcher.find()) {
						String t = matcher.group(1);
						String g = matcher.group();
						String tmp = "";
						
						if (t != null && !t.isEmpty()) {
							tmp = Warframe.getPolarityIcon(t);
						}
						
						if (g != null && tmp != null) {
							value = value.replaceFirst(g, tmp);
						}
					}
				}
				String tkj = "Disposition";
				
				if (value.contains(tkj) && value.length() >= tkj.length() + 1) {
					int t2 = tkj.length() + 1;
					String tk = value.substring(tkj.length(), t2);
					
					if (Utils.isInteger(tk)) {
						int tkl = Integer.parseInt(tk);
						String rivenT = Strings.repeat(Warframe.riven_filled_icon, tkl) + Strings.repeat(
								Warframe.riven_empty_icon, 5 - tkl);
						
						value = value.replace(tkj + tk, rivenT);
					}
				}
				
				if (key != null && value != null) {
					ArrayList<StringJoiner> valueJoiner = new ArrayList<>();
					int cur = 0;
					
					valueJoiner.add(new StringJoiner(value.split("\\[n\\]").length > 4 ? "\n" : ", "));
					
					if (value.split("\\[n\\]").length > 4) {
						valueJoiner.get(cur).add("");
					}
					
					//TODO If word ends with : then combine two words
					
					if (!Utils.isNumber(value) && !Utils.isDouble(value)) {
						for (String tk : value.split("\\[n\\]")) {
							tk = tk.trim();
							
							if (tk.isEmpty()) {
								continue;
							}
							
							String sub = "";
							String text = "";
							
							if (tk.contains(" (")) {
								sub = " " + tk.substring(tk.indexOf("("));
								tk = tk.substring(0, tk.indexOf(" ("));
							}
							
							
							if (key.toLowerCase().contains("drops") || key.toLowerCase().contains(
									"source") || key.toLowerCase().contains("stances")) {
								if (tk != null && !tk.isEmpty()) {
									String linkW = Warframe.getWikiLink(tk);
									
									if (linkW != null) {
										text = "[" + tk + "](" + linkW + ")" + sub;
									}
								}
							}
							
							if (text.isEmpty()) {
								text = tk + sub;
							}
							
							text = text.trim();
							
							String textT = text.replace("\n", "");
							
							if(valueJoiner.get(cur).length() + key.length() + textT.length() >= (MessageEmbed.VALUE_MAX_LENGTH - 10)){
								valueJoiner.add(new StringJoiner(value.split("\\[n\\]").length > 4 ? "\n" : ", "));
								cur += 1;
								
								if (value.split("\\[n\\]").length > 4) {
									valueJoiner.get(cur).add("");
								}
							}
							
							if (tk != null && !tk.isEmpty() && textT != null && !textT.isEmpty()) {
								valueJoiner.get(cur).add(textT);
							}
						}
					} else {
						value = value.trim();
						
						if (value != null && !value.isEmpty()) {
							if(valueJoiner.get(cur).length() + key.length() + value.length() >= (MessageEmbed.VALUE_MAX_LENGTH - 10)){
								valueJoiner.add(new StringJoiner(value.split("\\[n\\]").length > 4 ? "\n" : ", "));
								cur += 1;
								
								if (value.split("\\[n\\]").length > 4) {
									valueJoiner.get(cur).add("");
								}
							}
							
							valueJoiner.get(cur).add(value);
						}
					}
					
					for(StringJoiner val : valueJoiner) {
						StringBuilder tkJoiner = new StringBuilder();
						tkJoiner.append("**").append(key).append("**: ");
						tkJoiner.append(prefix).append(val.toString()).append("\n");
						
						if ((tkJoiner.length() + joiner.length()) >= (MessageEmbed.VALUE_MAX_LENGTH - 5)) {
							builder.addField(ent1.getKey(), joiner.toString(), false);
							joiner = new StringBuilder();
						}
						
						joiner.append(tkJoiner);
					}
				}
			}
			
			if (joiner.length() > 0) {
				builder.addField(ent1.getKey(), joiner.toString(), false);
			}
		}
		
		if (moreFound) {
			sources.add("\n*For more detailed info on sources use `/warframe info drops`*");
		}
		
		if (sources.length() > 0) {
			builder.addField("Sources", sources.toString(), false);
		}
		
		ChatUtils.sendMessage(channel, author.getAsMention(), builder.build());
	}
}
