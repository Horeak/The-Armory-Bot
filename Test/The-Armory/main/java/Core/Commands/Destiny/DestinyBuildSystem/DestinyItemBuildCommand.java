package Core.Commands.Destiny.DestinyBuildSystem;

import Core.CommandSystem.CommandObjects.SlashCommands.SlashCommandChannel;
import Core.CommandSystem.CommandObjects.SlashCommands.SlashCommandMessage;
import Core.Objects.Interfaces.Commands.ISlashCommand;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

//TODO Add slash command
public class DestinyItemBuildCommand implements ISlashCommand
{
	/*
	@DataObject( file_path = "destiny/builds.json", name = "builds" )
	public static ConcurrentHashMap<Long, ConcurrentHashMap<Long, ConcurrentHashMap<String, DestinyItemBuild>>> builds = new ConcurrentHashMap<>();
	
	public static DestinyItemBuild getSpecificItemBuild(User user, Long itemId, String buildName)
	{
		ConcurrentHashMap<String, DestinyItemBuild> itemBuilds = getBuildsForItem(user, itemId);
		
		if (itemBuilds.containsKey(buildName.toLowerCase().replace(" ", "_"))) {
			return itemBuilds.get(buildName.toLowerCase().replace(" ", "_"));
		}
		
		return null;
	}
	
	public static ConcurrentHashMap<String, DestinyItemBuild> getBuildsForItem(User user, Long itemId)
	{
		ConcurrentHashMap<Long, ConcurrentHashMap<String, DestinyItemBuild>> userBuilds = getUserBuilds(user);
		
		if (userBuilds.containsKey(itemId)) {
			return userBuilds.get(itemId);
		}
		
		return new ConcurrentHashMap<>();
	}
	
	public static ConcurrentHashMap<Long, ConcurrentHashMap<String, DestinyItemBuild>> getUserBuilds(User user)
	{
		if (builds.containsKey(user.getIdLong())) {
			return builds.get(user.getIdLong());
		}
		
		return new ConcurrentHashMap<>();
	}
	
	public static void setSpecificItemBuild(
			User user, Long itemId, DestinyItemBuild build, String name)
	{
		if (!builds.containsKey(user.getIdLong())) {
			builds.put(user.getIdLong(), new ConcurrentHashMap<>());
		}
		
		if (!builds.get(user.getIdLong()).containsKey(itemId)) {
			builds.get(user.getIdLong()).put(itemId, new ConcurrentHashMap<>());
		}
		
		build.name = name;
		builds.get(user.getIdLong()).get(itemId).put(name.replace(" ", "_").toLowerCase(), build);
	}
	
	public static void removeSpecificItemBuild(User user, Long itemId, String name)
	{
		if (!builds.containsKey(user.getIdLong())) {
			return;
		}
		if (!builds.get(user.getIdLong()).containsKey(itemId)) {
			return;
		}
		builds.get(user.getIdLong()).get(itemId).remove(name.replace(" ", "_").toLowerCase());
		
		if (builds.get(user.getIdLong()).get(itemId).size() <= 0) {
			builds.get(user.getIdLong()).remove(itemId);
		}
		
		if (builds.get(user.getIdLong()).size() <= 0) {
			builds.remove(user.getIdLong());
		}
	}
	
	public static void startBuildProcess(Message inputMessage, User user, Guild guild, BotChannel channel, Destiny2ItemObject temp)
	{
		String json = JsonUtils.getGson_non_pretty().toJson(temp);
		Destiny2ItemObject object = JsonUtils.getGson_non_pretty().fromJson(json, Destiny2ItemObject.class);
		
		if (object != null) {
			object.curated = false;
			object.finalizeObject();
		}
		
		ConcurrentHashMap<String, DestinyItemBuild> builds = getBuildsForItem(user, object.hash);
		
		if (builds.size() > 0) {
			StringJoiner joiner = new StringJoiner("\n");
			
			for (Map.Entry<String, DestinyItemBuild> buildEntry : builds.entrySet()) {
				joiner.add("Build: `" + WordUtils.capitalize(
						buildEntry.getKey().replace("_", " ")) + "` - Created: " + TimeParserUtil.getTime(
						buildEntry.getValue().dateCreated));
			}
			
			//TODO Might need to add a page system or atleast a limit to have builds you can have per item
			EmbedBuilder builder = new EmbedBuilder();
			builder.setTitle(
					"Found existing builds for this item. Please select one of the builds view or type \"new\" to create a new build");
			builder.setDescription(joiner.toString());
			
			ArrayList<String> list = new ArrayList<>();
			list.addAll(builds.keySet());
			list.add("New");
			
			ArrayList<Component> actions = new ArrayList<>();
			SlashCommandChannel ch = null;
			
			if(channel instanceof SlashCommandChannel) {
				ch = (SlashCommandChannel)channel;
			}
			
			for(String key : list){
				actions.add(ComponentResponseSystem.addComponent(user, ch != null ? ch.event : null, Button.secondary("id", key), (e) -> {
					if (key.equalsIgnoreCase("new")) {
						DestinyItemBuild build1 = new DestinyItemBuild(object);
						viewOrEditBuild(inputMessage, user, guild, channel, object, build1, true);
						
					} else {
						DestinyItemBuild object1 = builds.get(key);
						
						if (object1 != null) {
							viewOrEditBuild(inputMessage, user, guild, channel, object, object1, false);
						}
					}
				}));
			}
			
			ChatMessageBuilder slashBuilder = ChatUtils.getCorrectBuilder(user, channel);
			slashBuilder.withEmbed(builder);
			slashBuilder.withActions(actions);
			slashBuilder.send();
			
		} else {
			EmbedBuilder builder = new EmbedBuilder();
			builder.setTitle("Found no existing builds for this item. Do you wish to create a new one? Yes/No");
			
			ArrayList<Component> actions = new ArrayList<>();
			SlashCommandChannel ch = null;
			
			if(channel instanceof SlashCommandChannel) {
				ch = (SlashCommandChannel)channel;
			}
			
			for(String key : new String[]{"Yes", "No"}) {
				actions.add(ComponentResponseSystem.addComponent(user, ch != null ? ch.event : null, Button.secondary("id", key), (e) -> {
					if (key.equalsIgnoreCase("yes")) {
						DestinyItemBuild build1 = new DestinyItemBuild(object);
						viewOrEditBuild(inputMessage, user, guild, channel, object, build1, true);
					} else {
						ChatUtils.sendEmbed(channel, "Please try again if you change your mind.");
					}
				}));
			}
			
			ChatMessageBuilder slashBuilder = ChatUtils.getCorrectBuilder(user, channel);
			slashBuilder.withEmbed(builder);
			slashBuilder.withActions(actions);
			slashBuilder.send();
		}
	}
	
	public static void viewOrEditBuild(Message inputMessage,
			User user, Guild guild, BotChannel channel, Destiny2ItemObject object,
			DestinyItemBuild build, boolean newBuild)
	{
		
		EmbedBuilder itemBuilder = getBuilder(object, build);
		
		ChatUtils.sendMessage(channel, itemBuilder.build(), (itemMessage, T) -> {
			if (!newBuild) {
				EmbedBuilder builder = new EmbedBuilder();
				builder.setTitle("Type `edit` to start editing the build or `delete` to delete it");
				
				ChatUtils.sendMessage(channel, user.getAsMention(), builder.build(), (textMessage, T1) -> ResponseCommand.scheduleSimpleResponse(inputMessage, textMessage, channel, user, new String[]{"edit", "delete"}, (mes2, args) -> {
					String key = String.join("", args);
					
					if (key.equalsIgnoreCase("delete")) {
						removeSpecificItemBuild(user, object.hash, build.name);
					} else {
						startEditing(inputMessage, user, guild, channel, object, build, itemMessage);
					}
				}, false));

			} else {
				startEditing(inputMessage, user, guild, channel, object, build, itemMessage);
			}
		});
	}
	
	public static void startEditing(Message inputMessage,
			User user, Guild guild, BotChannel channel, Destiny2ItemObject object,
			DestinyItemBuild build, Message itemMessage)
	{
		EmbedBuilder builder = new EmbedBuilder();
		builder.setTitle("Currently editing build for `" + object.getName() + "`");
		
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
		
		HashMap<Integer, ArrayList<SocketObject>> perkMap = new HashMap<>();
		
		int i = 0;
		
		if (object.sockets != null && object.sockets.socketEntries != null) {
			for (Destiny2ItemObject.SocketEntry socket : object.sockets.socketEntries) {
				if (socket != null && socket.randomizedPlugSetHash != null) {
					Destiny2PlugSetObject setObject = Destiny2ItemSystem.destinyPlugSetObjects.getOrDefault(socket.randomizedPlugSetHash.intValue(), null);
					
					for (Destiny2ItemObject.RandomizedPlugItem plugItem : setObject.reusablePlugItems) {
						if (plugItem != null) {
							Destiny2ItemObject perkObject = Destiny2ItemSystem.destinyItemObjects.getOrDefault(
									plugItem.plugItemHash.intValue(), null);
							
							if (perkObject != null) {
								if (!perkMap.containsKey(i)) {
									perkMap.put(i, new ArrayList<>());
								}
								
								perkMap.get(i).add(new SocketObject(perkObject.displayProperties.name,
								                                    perkObject.displayProperties.description, i,
								                                    plugItem.plugItemHash));
							}
						}
					}
				}
				
				i++;
			}
		}
		
		ChatUtils.sendMessage(channel, builder.build(), (textMessage, T) -> {
			int edits = build.socketHash.size() + 2; //+1 for masterwork, and +1 for mod slot
			edit(inputMessage, user, channel, object, build, itemMessage, builder, perkMap, textMessage, 0, edits);
		});
	}
	
	protected static void edit(Message inputMessage,
			User user, BotChannel channel, Destiny2ItemObject object,
			DestinyItemBuild build, Message itemMessage, EmbedBuilder builder,
			HashMap<Integer, ArrayList<SocketObject>> possibleSockets, Message textMessage, int currentEdit,
			int edits)
	{
		if (currentEdit < 0) {
			currentEdit = 0;
		}
		
		if (currentEdit < build.socketHash.size()) {
			ArrayList<String> list = new ArrayList<>();
			
			ArrayList<SocketObject> sockets = null;
			Collection<ArrayList<SocketObject>> c = possibleSockets.values();
			
			int i = 0;
			for (ArrayList<SocketObject> ent : c) {
				if (i == currentEdit) {
					sockets = ent;
					break;
				}
				
				i++;
			}
			
			StringJoiner joiner = new StringJoiner(", ");
			if (sockets != null && sockets.size() > 0) {
				for (SocketObject socket : sockets) {
					joiner.add("`" + WordUtils.capitalize(socket.name) + "`");
					list.add(socket.name);
				}
			}
			
			for(int x = 1; x <= sockets.size(); x++){
				list.add(Integer.toString(x));
			}
			
			list.add("skip");
			list.add("back");
			
			
			builder.setDescription(
					"Select which perk to use in slot " + (currentEdit + 1) + ", remember to check the destiny command to see available perks for this item. The perk can be selected with either a number or the name of a perk. Type `skip` to skip selecting this perk, or use `back` if you wish to go back to the previous edit.\n\nPerks available for this slot include the following: " + joiner.toString());
			int finalCurrentEdit = currentEdit;
			
			//TODO Add possible names
			
			ArrayList<SocketObject> finalSockets = sockets;
			ChatUtils.editMessage(textMessage, user.getAsMention(), builder.build(), (textMessage1, T1) -> ResponseCommand.scheduleSimpleResponse(inputMessage, textMessage1, channel, user, list.toArray(new String[0]), (mes2, args) -> {
				String key = String.join("_", args).toLowerCase();
				boolean found = false;
				if (possibleSockets.entrySet().size() > finalCurrentEdit) {
					if (Utils.isInteger(key)) {
						int num = Integer.parseInt(key) - 1;
						
						if (num >= 0 && num < finalSockets.size()) {
							SocketObject socket = finalSockets.get(num);
							build.socketHash.put(socket.socketGroup, socket);
							found = true;
						}
					} else {
						for (SocketObject socket : finalSockets) {
							if (socket.name.replace(" ", "_").equalsIgnoreCase(key)) {
								build.socketHash.put(socket.socketGroup, socket);
								found = true;
								break;
							}
						}
					}
				}
				
				
				if (key.equalsIgnoreCase("skip")) {
					edit(inputMessage, user, channel, object, build, itemMessage, builder, possibleSockets, textMessage1,
					     finalCurrentEdit + 1, edits);
					return;
				} else if (key.equalsIgnoreCase("back")) {
					edit(inputMessage, user, channel, object, build, itemMessage, builder, possibleSockets, textMessage1,
					     finalCurrentEdit - 1, edits);
					return;
				}
				
				if (found) {
					ChatUtils.editMessage(itemMessage, getBuilder(object, build).build(), (textMessage2, T) -> {
						if (finalCurrentEdit + 1 < edits) {
							edit(inputMessage, user, channel, object, build, textMessage2, builder, possibleSockets, textMessage, finalCurrentEdit + 1, edits);
						}
					});
				} else {
					edit(inputMessage, user, channel, object, build, itemMessage, builder, possibleSockets, textMessage1,
					     finalCurrentEdit, edits);
				}
			}, false));

		} else if (currentEdit == edits - 2 && (object.masterworkObjects != null && object.masterworkObjects.size() > 0)) {
			ArrayList<String> list = new ArrayList<>();
			
			//Masterwork
			ArrayList<MasterworkObject> masterworkObjects = object.masterworkObjects;
			
			if (masterworkObjects != null) {
				StringJoiner joiner = new StringJoiner(", ");
				
				for (MasterworkObject masterwork : masterworkObjects) {
					joiner.add("`" + masterwork.statName + " " + masterwork.name + "`");
					list.add(masterwork.statName);
					list.add(masterwork.statName + " " + masterwork.name);
				}
				
				for(int x = 1; x <= masterworkObjects.size(); x++){
					list.add(Integer.toString(x));
				}
				
				list.add("skip");
				list.add("back");
				
				builder.setDescription(
						"Select which masterwork to use for the item. The masterwork can be selected with either a number or the name of the masterwork. Type `skip` to skip selecting the masterwork, or use `back` if you wish to go back to the previous edit..\n\nMasterworks available for this item include the following: " + joiner.toString());
				int finalCurrentEdit1 = currentEdit;
				
				ChatUtils.editMessage(textMessage, user.getAsMention(), builder.build(), (perkMessage, T1) -> ResponseCommand.scheduleSimpleResponse(inputMessage, perkMessage, channel, user, list.toArray(new String[0]), (mes2, args) -> {
					String key = String.join("_", args).toLowerCase();
					boolean found = false;
					
					if (Utils.isInteger(key)) {
						int num = Integer.parseInt(key) - 1;
						
						if (num >= 0 && num < masterworkObjects.size()) {
							build.masterwork = masterworkObjects.get(num);
							found = true;
						}
					} else {
						for (MasterworkObject masterwork : masterworkObjects) {
							String name = masterwork.statName + " " + masterwork.name;
							
							if (name.replace(" ", "_").equalsIgnoreCase(
									key) || masterwork.description.replace(" ", "_").equalsIgnoreCase(
									key)) {
								build.masterwork = masterwork;
								found = true;
								break;
							}
						}
					}
					
					if (key.equalsIgnoreCase("skip")) {
						edit(inputMessage, user, channel, object, build, itemMessage, builder, possibleSockets,
						     perkMessage, finalCurrentEdit1 + 1, edits);
						return;
					} else if (key.equalsIgnoreCase("back")) {
						edit(inputMessage, user, channel, object, build, itemMessage, builder, possibleSockets,
						     perkMessage, finalCurrentEdit1 - 1, edits);
						return;
					}
					
					if (found) {
						ChatUtils.editMessage(itemMessage, getBuilder(object, build).build(), (mes, T) -> {
							if (finalCurrentEdit1 + 1 < edits) {
								edit(inputMessage, user, channel, object, build, itemMessage, builder, possibleSockets,
								     perkMessage, finalCurrentEdit1 + 1, edits);
							}
						});
					} else {
						edit(inputMessage, user, channel, object, build, itemMessage, builder, possibleSockets,
						     perkMessage, finalCurrentEdit1, edits);
					}
				}, false));
				
			} else {
				edit(inputMessage, user, channel, object, build, itemMessage, builder, possibleSockets, textMessage, currentEdit + 1, edits);
			}
		} else if (currentEdit == edits - 1) {
			//Mod slot
			
			//TODO Add mod support
			
			edit(inputMessage, user, channel, object, build, itemMessage, builder, possibleSockets, textMessage, currentEdit + 1, edits);
		} else  {
			
			builder.setDescription(
					"Please enter a name you wish to save this build as. Type `skip` or ignore this message if you do not wish to save");
			
			ChatUtils.editMessage(textMessage, user.getAsMention(), builder.build(), (editedMessage, T1) -> ResponseCommand.scheduleSimpleResponse(inputMessage, editedMessage, channel, user, null, (mes2, args) -> {
				String key = String.join("_", args);
				
				if (key.equalsIgnoreCase("skip")) {
					if (mes2 != null) {
						ChatUtils.deleteMessage(mes2);
					}
				} else {
					setSpecificItemBuild(user, object.hash, build, key);
					ChatUtils.sendEmbed(channel,
					                      user.getAsMention() + " The build has now been saved as `" + String.join(" ", args) + "`!");
				}
				
				if (editedMessage != null) {
					ChatUtils.deleteMessage(editedMessage);
				}
			}, false));
		}
	}
	
	
	public static EmbedBuilder getBuilder(Destiny2ItemObject object,  DestinyItemBuild build)
	{
		EmbedBuilder builder = new EmbedBuilder();
		
		if (object.getImage() != null) {
			builder.setImage(Destiny2UserUtil.BASE_BUNGIE_URL + object.getImage());
		}
		if (object.getIcon() != null) {
			builder.setThumbnail(Destiny2UserUtil.BASE_BUNGIE_URL + object.getIcon());
		}
		if (object.getName() != null) {
			builder.setTitle(object.getName());
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
		
		for (InvestmentStat stat : object.investmentStats) {
			if (stat.oldValue != -1) {
				stat.value = stat.oldValue;
				stat.oldValue = -1;
			}
		}
		
		HashMap<Long, Integer> oldValueMap = new HashMap<>();
		
		for (BaseStatObject ob : object.getStats().values()) {
			oldValueMap.put(ob.statHash, ob.value);
		}
		
		if (build != null && build.socketHash != null) {
			for (Entry<Integer, SocketObject> socketEntry : build.socketHash.entrySet()) {
				if (socketEntry.getValue().hash == null) continue;
				
				Destiny2ItemObject perk = Destiny2ItemSystem.destinyItemObjects.getOrDefault(
						socketEntry.getValue().hash.intValue(), null);
				if (perk != null) {
					if (perk.investmentStats != null && perk.investmentStats.length > 0) {
						for (InvestmentStat stat : perk.investmentStats) {
							InvestmentStat curStat = null;
							
							for (InvestmentStat stat1 : object.investmentStats) {
								if (stat1.statTypeHash.equals(stat.statTypeHash)) {
									curStat = stat1;
									break;
								}
							}
							
							if (curStat != null) {
								if (curStat.oldValue == -1) {
									curStat.oldValue = curStat.value;
								}
								
								curStat.value += stat.value;
							}
						}
					}
				}
			}
		}
		
		
		if (build.masterwork != null) {
			String stat = build.masterwork.statName;
			int value = build.masterwork.value;
			
			if (stat != null) {
				InvestmentStat curStat = null;
				for (InvestmentStat stat1 : object.investmentStats) {
					if (stat1.statTypeHash.equals(build.masterwork.statTypeHash)) {
						curStat = stat1;
						break;
					}
				}
				
				if (curStat != null) {
					if (curStat.oldValue == -1) {
						curStat.oldValue = curStat.value;
					}
					
					curStat.value += value;
				}
			}
		}
		
		List<BaseStatObject> list = new LinkedList<>(object.getStats().values());
		list.sort(Comparator.comparingInt(o -> o.value));
		Collections.reverse(list);
		
		ArrayList<String> strings = new ArrayList<>();
		for (BaseStatObject ent : list) {
			String name = ent.getName();
			if(name == null || name.isBlank()) continue;
			
			int dif = oldValueMap.containsKey(ent.statHash) ? ent.value - oldValueMap.get(ent.statHash) : 0;
			
			if (name.equalsIgnoreCase("charge time") || name.equalsIgnoreCase("draw time")) {
				dif *= -1;
			}
			
			String dif1 = dif > 0 ? "+" + dif : Integer.toString(dif);
			String randomStat = ent.oldValue != -1 && ent.oldValue != ent.value ? " (" + dif1 + ")" : "";
			strings.add("*" + name + "*: **" + ent.value + (randomStat) + "**\n");
		}
		
		strings.removeIf((e) -> {
			for (String t : DEL_NAMES) {
				if (e.toLowerCase().contains(t)) {
					return true;
				}
			}
			
			return false;
		});
		
		StringBuilder b2 = new StringBuilder();
		StringBuilder b3 = new StringBuilder();
		
		for (int i = 0; i < strings.size(); i++) {
			if (i % 2 == 0) {
				b2.append(strings.get(i));
			} else {
				b3.append(strings.get(i));
			}
		}
		
		if (strings.size() > 0 || builder.toString().length() > 0) {
			if (!b2.toString().isEmpty()) {
				builder.addField("Stats", b2.toString(), true);
			}
			if (!b3.toString().isEmpty()) {
				builder.addField(b3.toString().isEmpty() ? "Stats" : "\u200b", b3.toString(), true);
			}
		}
		
		if (build.socketHash != null && build.socketHash.size() > 0) {
			for (Entry<Integer, SocketObject> ent : build.socketHash.entrySet()) {
				StringJoiner joiner = new StringJoiner(" | ");
				StringBuilder builder2 = new StringBuilder();
				SocketObject socket = ent.getValue();
				
				if (socket.name != null && socket.description != null) {
					if (!socket.name.isEmpty() && !socket.description.isEmpty()) {
						
						String desc = socket.description;
						if (desc.startsWith("\n")) {
							desc = desc.replaceFirst("\n", "");
						}
						
						joiner.add(socket.name);
						builder2.append("*").append(desc).append("*\n\n");
					}
				}
				
				
				if (joiner.toString().length() > 0 && builder2.toString().length() > 0) {
					builder.addField("[" + joiner.toString() + "]", builder2.toString(), false);
				}
			}
		}
		
		if (build.masterwork != null) {
			builder.addField("[Masterwork]", "*" + build.masterwork.statName + " " + build.masterwork.name + "*",
			                 false);
		} else {
			builder.addField("[Masterwork]", "*No masterwork has been selected.*", false);
		}
		
		return builder;
	}
	
	
	@Override
	public void commandExecuted(Guild guild, BotChannel channel, User author, Message inputMessage, String[] args)
	{
		String id = String.join(" ", args);
		
		if (id.replace(" ", "").isEmpty()) {
			ChatUtils.sendEmbed(channel, author.getAsMention() + " Please enter a search word!");
			return;
		}
		
		ArrayList<Destiny2ItemObject> objects = new ArrayList<>();
		ArrayList<Destiny2ItemObject> list1 = Destiny2ItemSystem.getItemsByName(id, false);
		
		list1.removeIf((o) -> o.itemType == 20); //Remove dummy items
		
		if (list1 != null && list1.size() > 0) {
			objects.addAll(list1);
		}
		
		objects.sort((o1, o2) -> Integer.compare(o2.destinyVersion, o1.destinyVersion));
		
		if (objects.size() > 10) {
			List<Destiny2ItemObject> list = objects.subList(0, 5);
			objects = new ArrayList<>(list);
		}
		
		objects.removeIf(e -> e.preset || e.curated);
		
		if (objects.size() > 1) {
			StringJoiner joiner = new StringJoiner("\n\n");
			
			int i = 1;
			for (Destiny2ItemObject object : objects) {
				String versionPre = "**[Destiny " + object.destinyVersion + (object.dlc != null ? " - " + WordUtils.capitalize(
						object.dlc) + "" : "") + "]**";
				String namePre = "*" + object.getName() + "* (*" + object.getItemTierAndType() + "*)";
				String sourcePre = (object.source != null && !object.source.isEmpty() ? "\n > " + (!object.source.startsWith(
						"Source: ") ? "Source: " : "") + object.source : "");
				
				joiner.add("**" + i + "**) " + versionPre + " " + namePre + sourcePre);
				i++;
			}
			
			EmbedBuilder builder = new EmbedBuilder();
			builder.setDescription(joiner.toString());
			builder.setTitle(
					"Select which item you would like to view or create a build from the list below by typing the number you want");
			
			ArrayList<Destiny2ItemObject> finalObjects = objects;
			ChatUtils.sendMessage(channel, author.getAsMention() + " Please select which item you would like to view", builder.build(), (mes, T) -> ResponseCommand.scheduleResponse(inputMessage, channel, author, new ResponseAction()
			{
				@Override
				public boolean isValidInput(Message message, String[] args)
				{
					return Utils.isInteger(String.join("", args));
				}
				
				@Override
				public void execute(Message message, String[] args)
				{
					int num = Integer.parseInt(String.join("", args));
					
					if (num >= 1) {
						num--;
					}
					
					if (num > finalObjects.size()) {
						ChatUtils.sendEmbed(channel, message.getAuthor().getAsMention() + " Invalid number!");
					} else {
						Destiny2ItemObject object = finalObjects.get(num);
						
						if (object != null) {
							startBuildProcess(message, author, guild, channel, object);
						}
					}
					
					if (mes != null) {
						ChatUtils.deleteMessage(mes);
					}
					
					if (message != null) {
						ChatUtils.deleteMessage(message);
					}
				}
				
				@Override
				public void timeout()
				{
					ChatUtils.deleteMessage(mes);
				}
			}));
			
		} else if (objects.size() == 1) {
			Destiny2ItemObject object = objects.get(0);
			startBuildProcess(inputMessage, author, guild, channel, object);
			
		} else if (objects.size() <= 0) {
			ChatUtils.sendEmbed(channel,
			                      author.getAsMention() + " Found no items matching that search which could be used for a build!");
		}
	}
	
	
	@Override
	public  String getDescription() { return "Allows creating custom item builds based on Destiny 2"; }
	
	
	*/
	
	@Override
	public String getSlashName()
	{
		return "item-build";
	}
	
	@Override
	public void slashCommandExecuted(SlashCommandInteractionEvent slashEvent, Guild guild, User author, SlashCommandChannel channel, SlashCommandMessage message)
	{
	
	}
}

