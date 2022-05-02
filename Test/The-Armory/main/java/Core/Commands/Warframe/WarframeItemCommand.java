package Core.Commands.Warframe;

import Core.CommandSystem.ChatUtils;
import Core.CommandSystem.CommandObjects.SlashCommands.SlashCommandChannel;
import Core.CommandSystem.CommandObjects.SlashCommands.SlashCommandMessage;
import Core.Objects.Annotation.Commands.SlashArgument;
import Core.Objects.Annotation.Commands.SubCommand;
import Core.Objects.CustomEntry;
import Core.Objects.Interfaces.Commands.ISlashCommand;
import Core.Util.JsonUtils;
import Core.Util.Time.TimeParserUtil;
import com.google.common.base.Strings;
import com.google.gson.reflect.TypeToken;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.apache.commons.lang3.text.WordUtils;
import org.json.JSONObject;

import java.awt.Color;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SubCommand( parent = Warframe.WarframeInfo.class)
public class WarframeItemCommand implements ISlashCommand
{
	@Override
	public String getDescription()
	{
		return "Allows looking up items from warframe";
	}
	
	@Override
	public String getSlashName()
	{
		return "item";
	}
	
	@SlashArgument( key = "item", text = "Which warframe item you want to search for", required = true)
	public String itemName;
	
	@Override
	public void slashCommandExecuted(SlashCommandInteractionEvent slashEvent, Guild guild, User author, SlashCommandChannel channel, SlashCommandMessage message)
	{
		DecimalFormat formatter = (DecimalFormat) NumberFormat.getInstance(Locale.US);
		DecimalFormatSymbols symbols = formatter.getDecimalFormatSymbols();
		
		symbols.setGroupingSeparator('.');
		formatter.setDecimalFormatSymbols(symbols);
		
		org.json.JSONObject object = WarframeItemSystem.getItem(itemName);
		
		if(object == null){
			ChatUtils.sendEmbed(channel, author.getAsMention() + " Found no item with that name!");
			return;
		}
		
		if(object != null){
			EmbedBuilder builder = new EmbedBuilder();
			
			String name = object.getString("name");
			String url = object.has("wikiaUrl") ? object.getString("wikiaUrl") : null;
			
			if(url == null){
				url = Warframe.getWikiLink(name);
			}
			
			if(url != null){
				builder.setTitle(name, url);
			}else{
				builder.setTitle(name);
			}
			
			if(object.has("description")){
				builder.setDescription("*" + object.getString("description") + "*");
			}
			
			if(object.has("vaulted")){
				boolean vaulted = object.getBoolean("vaulted");
				
				if(vaulted){
					if(!object.has("vaultDate") && !object.has("estimatedVaultDate")) {
						builder.appendDescription("\n\n**This item has been vaulted!**");
					}else{
						if(object.has("vaultDate")){
							builder.appendDescription("\n\n**This item was vaulted on `" + object.getString("vaultDate") + "`**");
						}else if(object.has("estimatedVaultDate")){
							builder.appendDescription("\n\n**This item was vaulted on `" + object.getString("estimatedVaultDate") + "`**");
						}
					}
				}
			}
			
			if(object.has("imageName")){
				builder.setThumbnail(WarframeItemSystem.IMAGE_LINK + object.getString("imageName"));
			}
			
			int color = object.has("color") ? object.getInt("color") : 0;
			
			if(color != 0){
				builder.setColor(new Color(color));
			}
			
			//Item type
			{
				String[] ignoredSlots = new String[]{"warframes", "resources", "skins", "misc", "mods"};
				
				String type = object.has("type") ? object.getString("type") : null;
				String noise = object.has("noise") ? object.getString("noise") : null;
				String trigger = object.has("trigger") ? object.getString("trigger") : null;
				String category = object.has("category") ? object.getString("category") : null;
				
				if(category != null){
					for(String t : ignoredSlots){
						if(category.equalsIgnoreCase(t)){
							category = null;
							break;
						}
					}
				}
				
				StringBuilder typeBuilder = new StringBuilder();
				
				if(type != null && !type.equalsIgnoreCase("warframe") && trigger != null){
					typeBuilder.append("Item type: **" + type + " (" + trigger + ")" + "**\n");
				}else{
					if(type != null && !type.equalsIgnoreCase("warframe") && !type.equalsIgnoreCase("misc")) typeBuilder.append("Item type: **" + type + "**\n");
					if(trigger != null) typeBuilder.append("Trigger mode: **" + trigger + "**\n");
				}
				
				if(category != null) typeBuilder.append("Item slot: **" + category + "**\n");
				if(noise != null) typeBuilder.append("Noise level: **" + noise + "**\n");
				
				if(typeBuilder.toString().length() > 0 && !typeBuilder.toString().isBlank()) {
					builder.addField("Item info", typeBuilder.toString(), true);
				}
			}
			
			
			//Defence
			{
				int health = object.has("health") ? object.getInt("health") : 0;
				int shield = object.has("shield") ? object.getInt("shield") : 0;
				int armor = object.has("armor") ? object.getInt("armor") : 0;
				
				if(health != 0 || shield != 0 || armor != 0) {
					StringBuilder defenceBuilder = new StringBuilder();
					
					defenceBuilder.append("Health: **" + health + "**\n");
					defenceBuilder.append("Shield: **" + shield + "**\n");
					defenceBuilder.append("Armor: **" + armor + "**\n");
					
					builder.addField("Defence", defenceBuilder.toString(), true);
				}
			}
			
			
			//Movement
			{
				double sprint = object.has("sprint") ? object.getDouble("sprint") : 0;
				double sprintSpeed = object.has("sprintSpeed") ? object.getDouble("sprintSpeed") : 0;
				int stamina = object.has("stamina") ? object.getInt("stamina") : 0;
				
				if(sprint != 0 || sprintSpeed != 0 || stamina != 0) {
					StringBuilder movementBuilder = new StringBuilder();
					
					movementBuilder.append("Sprint speed: **" + sprintSpeed + "**\n");
					movementBuilder.append("Stamina: **" + stamina + "**\n");
					
					builder.addField("Movement", movementBuilder.toString(), true);
				}
			}
			
			//Level Stats
			{
				org.json.JSONArray array = object.has("levelStats") ? object.getJSONArray("levelStats") : null;
				StringBuilder builder1 = new StringBuilder();
				
				if (array != null) {
					for (Object t : array) {
						if (t instanceof JSONObject) {
							JSONObject object1 = (JSONObject)t;
							
							if(object1.has("stats")){
								org.json.JSONArray array1 = object1.has("stats") ? object1.getJSONArray("stats") : null;
								
								if (array1 != null) {
									for (Object t1 : array1) {
										if (t1 instanceof String) {
											String val = (String)t1;
											builder1.append(val + "\n");
										}
									}
								}
								if(builder1.toString().length() > 0 && !builder1.toString().isBlank()) {
									break;
								}
							}
						}
					}
				}
				
				if(builder1.toString().length() > 0 && !builder1.toString().isBlank()){
					builder.addField("Effect", builder1.toString(), false);
				}
			}
			
			//Misc
			{
				StringBuilder miscBuilder = new StringBuilder();
				
				int power = object.has("power") ? object.getInt("power") : 0;
				int masteryReq = object.has("masteryReq") ? object.getInt("masteryReq") : 0;
				int baseDrain = object.has("baseDrain") ? object.getInt("baseDrain") : 0;
				
				String polarity = object.has("polarity") ? object.getString("polarity") : null;
				
				if (masteryReq > 0) {
					miscBuilder.append("Mastery rank required: **" + masteryReq + "**\n");
				}
				
				if (power != 0) {
					miscBuilder.append("Energy: **" + power + "**\n");
				}
				
				if (baseDrain != 0) {
					miscBuilder.append("Base cost: **" + baseDrain + "**\n");
				}
				
				if (polarity != null) {
					miscBuilder.append("Polarity: **" + Warframe.getPolarityIcon(polarity) + "**\n");
				}
				
				if (object.has("buildTime")) {
					Long time = object.getLong("buildTime");
					Long changedTime = TimeUnit.MILLISECONDS.convert(time, TimeUnit.SECONDS);
					String timeText = TimeParserUtil.getTimeText(changedTime);
					
					if (timeText != null && !timeText.isBlank()) {
						miscBuilder.append("Build time: **" + timeText + "**\n");
					}
				}
				
				if (object.has("buildPrice")) {
					miscBuilder.append("Build cost: **" + Warframe.credits_icon + formatter.format(object.getInt("buildPrice")) + "**\n");
				}
				
				if (object.has("skipBuildTimePrice")){
					miscBuilder.append("Skip cost: **" + Warframe.platinum_icon + formatter.format(object.getInt("skipBuildTimePrice")) + "**\n");
				}
				
				if(miscBuilder.toString().length() > 0 && !miscBuilder.toString().isBlank()){
					builder.addField("Misc", miscBuilder.toString(), false);
				}
			}
			
			//Status
			{
				int criticalMultiplier = object.has("criticalMultiplier") ? object.getInt("criticalMultiplier") : -1;
				
				double criticalChance = object.has("criticalChance") ? object.getDouble("criticalChance") : -1;
				double procChance = object.has("procChance") ? object.getDouble("procChance") : -1;
				
				StringBuilder statusBuilder = new StringBuilder();
				
				if(criticalMultiplier != -1){
					statusBuilder.append("Critical multiplier: **" + criticalMultiplier + "x**\n");
				}
				
				if(criticalChance != -1){
					statusBuilder.append("Critical chance: **" + (Math.round(criticalChance * 100)) + "%**\n");
				}
				
				if(procChance != -1){
					statusBuilder.append("Status chance: **" + (Math.round(procChance * 100)) + "%**\n");
				}
				
				if(statusBuilder.toString().length() > 0 && !statusBuilder.toString().isBlank()) {
					builder.addField("Chance", statusBuilder.toString(), true);
				}
			}
			
			//Stats
			{
				int accuracy = object.has("accuracy") ? object.getInt("accuracy") : -1;
				int magazineSize = object.has("magazineSize") ? object.getInt("magazineSize") : -1;
				
				double fireRate = object.has("fireRate") ? object.getDouble("fireRate") : -1;
				double reloadTime = object.has("reloadTime") ? object.getDouble("reloadTime") : -1;
				
				StringBuilder statsBuilder = new StringBuilder();
				
				if(accuracy != -1){
					statsBuilder.append("Accuracy: **" + accuracy + "%**\n");
				}
				
				if(fireRate != -1){
					statsBuilder.append("Fire rate: **" + (Math.round(fireRate)) + "/s**\n");
				}
				
				if(reloadTime != -1){
					statsBuilder.append("Reload speed: **" + (Math.round(reloadTime * 60)) + " rounds/s**\n");
				}
				
				if(magazineSize != -1){
					statsBuilder.append("Magazine size: **" + magazineSize + "**\n");
				}
				
				if(statsBuilder.toString().length() > 0 && !statsBuilder.toString().isBlank()) {
					builder.addField("Stats", statsBuilder.toString(), true);
				}
			}
			
			
			//Damage
			{
				
				//TODO Add support for radial attack and secondary
				StringBuilder damageBuilder = new StringBuilder();
				
				if (object.has("damageTypes")) {
					JSONObject ob = object.getJSONObject("damageTypes");
					HashMap<String, Double> map = JsonUtils.getGson_pretty().fromJson(ob.toString(), new TypeToken<HashMap<String, Double>>() {}.getType());
					
					if(map != null) {
						for (Entry<String, Double> ent : map.entrySet()) {
							damageBuilder.append(WordUtils.capitalize(ent.getKey()) + ": **" + ent.getValue() + "**\n");
						}
					}
					
					builder.addField("Damage types", damageBuilder.toString(), false);
				}
			}
			
			//disposition
			{
				int disposition = object.has("disposition") ? object.getInt("disposition") : -1;
				
				if(disposition != -1){
					int rivenLeft = 5 - disposition;
					String rivenT = Strings.repeat(Warframe.riven_filled_icon, disposition) + Strings.repeat(Warframe.riven_empty_icon, rivenLeft);
					builder.addField("Riven disposition", rivenT, false);
				}
			}
			
			//Polarities
			{
				if (object.has("aura") || object.has("polarities")) {
					StringBuilder auraBuilder = new StringBuilder();
					
					if (object.has("aura")) {
						String icon = Warframe.getPolarityIcon(object.getString("aura"));
						if (icon != null) {
							auraBuilder.append("Aura: " + icon + "\n");
						}
					}
					
					org.json.JSONArray array = object.has("polarities") ? object.getJSONArray("polarities") : null;
					StringJoiner arrayBuilder = new StringJoiner(" ");
					
					if (array != null) {
						for (Object t : array) {
							if (t instanceof String) {
								String val = (String)t;
								String icon = Warframe.getPolarityIcon(val);
								
								if (icon != null) {
									arrayBuilder.add(icon);
								}
							}
						}
					}
					
					if (arrayBuilder.length() > 0) {
						auraBuilder.append("Polarities: " + arrayBuilder.toString() + "\n");
					}
					
					if (auraBuilder != null && auraBuilder.toString() != null && !auraBuilder.toString().isBlank()){
						builder.addField("Polarities", auraBuilder.toString(), true);
					}
				}
			}
			
			
			//Abilities
			{
				if (object.has("abilities")) {
					StringBuilder abilityBuilder = new StringBuilder();
					
					if (object.has("passiveDescription")) {
						String passDesc = object.getString("passiveDescription");
						
						
						//TODO Find a better way to do this. Is  there anywhere the stats are hidden?
						
						final Pattern pattern = Pattern.compile("(\\|)(.*?)(\\|)", Pattern.MULTILINE);
						final Matcher matcher = pattern.matcher(passDesc);
						
						while (matcher.find()) {
							passDesc = passDesc.replace(matcher.group(), "X");
						}
						
						abilityBuilder.append("**Passive**:\n*" + passDesc + "*\n\n");
					}
					
					org.json.JSONArray array = object.getJSONArray("abilities");
					int i = 1;
					
					for (Object obj : array) {
						if (obj instanceof org.json.JSONObject) {
							org.json.JSONObject jObject = (org.json.JSONObject)obj;
							
							String abilName = jObject.getString("name");
							String abilDesc = jObject.getString("description");
							
							final Pattern pattern = Pattern.compile("(<)(.*?)(>)", Pattern.MULTILINE);
							final Matcher matcher = pattern.matcher(abilDesc);
							
							while (matcher.find()) {
								abilDesc = abilDesc.replace(matcher.group(), "");
							}
							
							String text = "**" + i + "**) **" + abilName + "**\n*" + abilDesc + "*\n\n";
							
							if (abilityBuilder.toString().length() + text.length() >= (MessageEmbed.VALUE_MAX_LENGTH - 10)) {
								builder.addField("Abilities", abilityBuilder.toString(), false);
								abilityBuilder = new StringBuilder();
							}
							
							abilityBuilder.append(text);
							
							i++;
						}
					}
					
					if (abilityBuilder != null && abilityBuilder.toString() != null && !abilityBuilder.toString().isBlank()){
						builder.addField("Abilities", abilityBuilder.toString(), false);
					}
				}
			}
			
			
			//Crafting
			{
				if (object.has("components")) {
					StringBuilder componentBuilder = new StringBuilder();
					
					org.json.JSONArray array = object.getJSONArray("components");
					
					for (Object obj : array) {
						if (obj instanceof org.json.JSONObject) {
							org.json.JSONObject jObject = (org.json.JSONObject)obj;
							
							String compName = jObject.getString("name");
							int amount = jObject.getInt("itemCount");
							boolean tradable = jObject.getBoolean("tradable");
							
							for (String tk : WarframeItemSystem.componentNames) {
								if (compName.equalsIgnoreCase(tk)) {
									compName = name + " " + compName;
								}
							}
							
							String compUrl = Warframe.getWikiLink(compName);
							String compText = compUrl != null ? "[" + compName + "](" + compUrl + ")" : compName;
							
							componentBuilder.append("**" + formatter.format(amount) + "x** " + compText + " [" + (tradable ? Warframe.trading_icon_allow : Warframe.trading_icon_not_allowed) + "]\n");
						}
					}
					
					if (object.has("buildQuantity")) {
						int am = object.getInt("buildQuantity");
						
						if (am > 1) {
							componentBuilder.append("\nMakes: **x" + am + "**");
						}
					}
					
					if (componentBuilder != null && componentBuilder.toString() != null && !componentBuilder.toString().isBlank()) {
						builder.addField("Crafting ", componentBuilder.toString(), false);
					}
				}
			}
			
			
			//Drops
			{
				ArrayList<org.json.JSONObject> dropObject = WarframeItemSystem.getDropItem(name);
				
				if(dropObject != null) {
					if (dropObject.size() > 0) {
						ArrayList<CustomEntry<Integer, JSONObject>> list = new ArrayList<>();
						
						for(JSONObject jsonObject : dropObject) {
							if(!jsonObject.has("drops")) continue;
							
							int itemCount = jsonObject.has("itemCount") ? jsonObject.getInt("itemCount") : 1;
							
							org.json.JSONArray array = jsonObject.getJSONArray("drops");
							
							for (Object obj : array) {
								if (obj instanceof JSONObject) {
									JSONObject jObject = (JSONObject)obj;
									
									if (jObject.has("chance")) {
										list.add(new CustomEntry<>(itemCount, jObject));
									}
								}
							}
						}
						
						list.sort(Comparator.comparingDouble(o -> o.getValue().has("chance") ? o.getValue().getDouble("chance") : 0));
						Collections.reverse(list);
						
						StringBuilder dropsBuilder = new StringBuilder();
						
						int number = 0;
						
						for (CustomEntry<Integer, JSONObject> entry : list) {
							JSONObject object1 = entry.getValue();
							
							double chance = object1.has("chance") ? object1.getDouble("chance") : -1;
							String rotation = object1.has("rotation") ? object1.getString("rotation") : null;
							String location = object1.has("location") ? object1.getString("location") : null;
							
							String locName = location;
							
							if(rotation != null &&
							   (!rotation.equalsIgnoreCase("rewards") && !rotation.equalsIgnoreCase("Sabotage"))){
								locName += ", (Rotation: " + rotation + ")";
							}
							
							if (chance != -1) locName += ", " +(Math.round((chance * 100) * 100.0) / 100.0) + "%";
							
							if(dropsBuilder.toString().contains(locName)){
								continue;
							}
							
							if(dropsBuilder.toString().length() + locName.length() >= (MessageEmbed.VALUE_MAX_LENGTH - 20)){
								break;
							}
							
							int current = dropsBuilder.toString().split("\\n").length;
							
							if(current >= 10){
								break;
							}
							
							dropsBuilder.append("`" + locName + "`\n");
							number++;
						}
						
						if (dropsBuilder != null && dropsBuilder.toString() != null && !dropsBuilder.toString().isBlank()) {
							builder.addField("Top " + number + " drops", dropsBuilder.toString(), false);
						}
					}
				}
			}
			
			ChatUtils.sendMessage(channel, builder.build());
		}
	}
}
