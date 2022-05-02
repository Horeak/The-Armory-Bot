package Core.Commands.Destiny.User;

import Core.CommandSystem.CommandObjects.SlashCommands.SlashCommandChannel;
import Core.CommandSystem.CommandObjects.SlashCommands.SlashCommandMessage;
import Core.Objects.Interfaces.Commands.ISlashCommand;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;


//TODO Refine the search based on similar items
//TODO Maybe add a option to transfer the viewed item to a specific character? (Use the paginator thingy to have a selection of each character to transfer it to?)
//TODO Add search filters so similar to DIM with stuff like PL>750 is:masterworked etc etc

//TODO Allow searching for elements
//TODO Allow searching for class items
//TODO Make qutation marks search for name specificly and then apply all other words like filters? So you can search for arc hand cannon for example?
//TODO Add a command to view current loadout more easily similar to charlemagne with "!Loadout kinetic" for example

//@SubSlashCommand(parent = DestinySlashCommand.class )
public class Destiny2ItemSearchCommand implements ISlashCommand
{
	@Override
	public String getSlashName()
	{
		return "search";
	}
	
	@Override
	public void slashCommandExecuted(SlashCommandInteractionEvent slashEvent, Guild guild, User author, SlashCommandChannel channel, SlashCommandMessage message)
	{
	
	}
	/**
	@Override
	public String getCategory()
	{
		return "Destiny";
	}
	
	private final Paginator.Builder pbuilder;
	public static EventWaiter waiter = new EventWaiter();
	
	@Init
	public static void init(){
		Startup.discordClient.getEventManager().register(waiter);
	}
	
	public Destiny2ItemSearchCommand()
	{
		pbuilder = new Paginator.Builder().setColumns(1)
				.setItemsPerPage(5)
				.showPageNumbers(true)
				.waitOnSinglePage(true)
				.useNumberedItems(true)
				.allowTextInput(false)
				.setFinalAction(m -> ChatUtils.deleteMessage(m))
				.setEventWaiter(waiter)
				.setTimeout(5, TimeUnit.MINUTES);
	}
	
	@Override
	public String commandPrefix()
	{
		return "destinySearch";
	}
	
	@Override
	public String[] commandPrefixes()
	{
		return new String[]{"find", "item", "destinyitem"};
	}
	
	@Override
	public void commandExecuted(
			Guild guild, BotChannel channel, User author, Message message, String[] args)
	{
		String searchPhrase = String.join(" ", args);
		
		MemberShipObject memberShipObject = Destiny2UserUtil.getMemberShipObject(channel, author);
		
		if (memberShipObject == null) return;
		
		if(searchPhrase.isBlank()){
			ChatUtils.sendEmbed(channel, author.getAsMention() + " Please input a search phrase!");
			return;
		}
		
		Message slowMes = channel.sendMessage(author.getAsMention() + " Searching for items, this may take a moment.").complete();
		
		ArrayList<CustomEntry<Destiny2ItemObject, HashMap<String, JSONObject>>>itemMap = new ArrayList<>();
		HashMap<String, String> charInfo = new HashMap<>();
		int itemsFound = 0;
		
		
		int memberType = memberShipObject.getMemberType();
		String memberId = memberShipObject.getId();
		
		String components = "CharacterEquipment,CharacterInventories,ProfileInventories,ItemInstances,ItemStats,ItemSockets";
		//TODO Will probably have to find a way to split this up as it will probably be too much to keep in memory at once
		JSONObject profileObject = Destiny2UserUtil.getData(Destiny2UserUtil.getAccessToken(author), "/Platform/Destiny2/" + memberType + "/Profile/" + memberId + "/?components=" + components);
		JSONObject characterObject = Destiny2UserUtil.getData(Destiny2UserUtil.getAccessToken(author), "/Platform/Destiny2/" + memberType + "/Profile/" + memberId + "/?components=Characters");
		
		if (profileObject.has("characterEquipment")) {
			itemsFound = getItemsFound(searchPhrase, itemMap, itemsFound, profileObject, "characterEquipment");
		}

		if (profileObject.has("characterInventories")) {
			itemsFound = getItemsFound(searchPhrase, itemMap, itemsFound, profileObject, "characterInventories");
		}

		if (profileObject.has("profileInventory")) {
			itemsFound = getItemsFound(searchPhrase, itemMap, itemsFound, profileObject, "profileInventory");
		}
		
		if (characterObject.has("characters") && characterObject.getJSONObject("characters").has("data")) {
			JSONObject data = characterObject.getJSONObject("characters").getJSONObject("data");
			
			for (String key : data.keySet()) {
				Object ob1 = data.get(key);
				
				if (ob1 instanceof JSONObject) {
					JSONObject character = (JSONObject)ob1;
					
					if (character.has("characterId")) {
						String characterId = character.getString("characterId");
						charInfo.put(characterId, getCharacterStringBuilder(character).toString());
					}
				}
			}
		}
		
		
		if(itemsFound > 0) {
			ArrayList<String> list = new ArrayList<>();
			
			itemMap.sort((c1, c2) ->{
				int c1S = 0, c2S = 0;
				
				if(c1.getKey().getItemTier() != 6) {
					if (c1.getValue().containsKey("versionNumber")) {
						JSONObject itemObject = c1.getValue().get("versionNumber");
						
						if (itemObject != null) {
							if (itemObject.has("versionNumber")) {
								int versionNumber = itemObject.getInt("versionNumber");
								
								if (c1.getKey().quality != null && c1.getKey().quality.versions != null && c1.getKey().quality.versions.length > 0) {
									Long hashId = c1.getKey().quality.versions[versionNumber].powerCapHash;
									
									Destiny2PowerCapObject val = Destiny2ItemSystem.destinyPowerCapObjects.getOrDefault(
											hashId.intValue(), null);
									
									if (val != null) {
										c1S = val.index;
									}
								}
							}
						}
					}
				}
				
				if(c2.getKey().getItemTier() != 6) {
					if (c2.getValue().containsKey("versionNumber")) {
						JSONObject itemObject = c2.getValue().get("versionNumber");
						
						if (itemObject != null) {
							if (itemObject.has("versionNumber")) {
								int versionNumber = itemObject.getInt("versionNumber");
								
								if (c2.getKey().quality != null && c2.getKey().quality.versions != null && c2.getKey().quality.versions.length > 0) {
									Long hashId = c2.getKey().quality.versions[versionNumber].powerCapHash;
									
									Destiny2PowerCapObject val = Destiny2ItemSystem.destinyPowerCapObjects.getOrDefault(
											hashId.intValue(), null);
									
									if (val != null) {
										c2S = val.index;
									}
								}
							}
						}
					}
				}
				
				return Integer.compare(c2S, c1S);
			});
			
			itemMap.sort((c1, c2) -> Integer.compare(c2.getKey().getItemTier(), c1.getKey().getItemTier()));
			
			itemMap.sort((c1, c2) ->{
				int c1S = 0, c2S = 0;
				
				if (c1.getValue().containsKey("instances")) {
					JSONObject itemObject = c1.getValue().get("instances");
					
					if (itemObject != null) {
						if (itemObject.has("primaryStat")) {
							JSONObject primaryStat = itemObject.getJSONObject("primaryStat");
							
							if (primaryStat.has("value")) {
								c1S = primaryStat.getInt("value");
							}
						}
					}
				}
				
				if (c2.getValue().containsKey("instances")) {
					JSONObject itemObject = c2.getValue().get("instances");
					
					if (itemObject != null) {
						if (itemObject.has("primaryStat")) {
							JSONObject primaryStat = itemObject.getJSONObject("primaryStat");
							
							if (primaryStat.has("value")) {
								c2S = primaryStat.getInt("value");
							}
						}
					}
				}
				
				return Integer.compare(c2S, c1S);
			});
			
			for (CustomEntry<Destiny2ItemObject, HashMap<String, JSONObject>> entry : itemMap) {
				JSONObject tempOb = entry.getValue().getOrDefault("charId", null);
				String charId = tempOb != null && tempOb.has("charId") ? tempOb.getString("charId") : null;
				
				if (charId != null) {
					if (charInfo.containsKey(charId)) {
						charId = charInfo.get(charId);
					}
				} else {
					charId = "Vault";
				}
				
				String text = entry.getKey().getName();
				
				boolean hasSlot = false;
				
				//Set rarity
				if (entry.getKey().getItemTier() != 0) {
					String icon = entry.getKey().getItemTier() == 3 ? DestinySystem.COMMON_ICON
							: entry.getKey().getItemTier() == 4 ? DestinySystem.RARE_ICON
							: entry.getKey().getItemTier() == 5 ? DestinySystem.LEGENDARY_ICON
							: entry.getKey().getItemTier() == 6 ? DestinySystem.EXOTIC_ICON : "";
					
					if (icon != null && !icon.isEmpty()) {
						text += "\n- " + icon;
						hasSlot = true;
					}
				}
				
				
				//Set damage type
				if (entry.getKey().defaultDamageTypeHash != null) {
					Destiny2DamageTypeObject damageTypeObject = Destiny2ItemSystem.destinyDamageTypeObjects.getOrDefault(
							entry.getKey().defaultDamageTypeHash.intValue(), null);
					
					if (damageTypeObject != null) {
						text += (hasSlot ? " " : "\n- ") + DestinySystem.getIcon(damageTypeObject.displayProperties.name) ;
						hasSlot = true;
					}
				}
				
				//Set ammo type/slot type
				if (entry.getKey().equippingBlock != null) {
					String slot = entry.getKey().equippingBlock.ammoType == 1 ? DestinySystem.PRIMARY_ICON : entry.getKey().equippingBlock.ammoType == 2 ? DestinySystem.SPECIAL_ICON : entry.getKey().equippingBlock.ammoType == 3 ? DestinySystem.HEAVY_ICON : "";
					
					if (slot != null && !slot.isEmpty()) {
						text += (hasSlot ? " " : "\n- ") + slot ;
						hasSlot = true;
					}
				}
				
				
				text += (hasSlot ? " " : "\n- ") + entry.getKey().itemTypeDisplayName + "\n";
				
				if(entry.getValue().containsKey("versionNumber")) {
					JSONObject itemObject = entry.getValue().get("versionNumber");
					
					if (itemObject != null) {
						if (itemObject.has("versionNumber")) {
							int versionNumber = itemObject.getInt("versionNumber");
							
							if (entry.getKey().quality != null && entry.getKey().quality.versions != null && entry.getKey().quality.versions.length > 0) {
								Long hashId = entry.getKey().quality.versions[versionNumber].powerCapHash;
								
								Destiny2PowerCapObject val = Destiny2ItemSystem.destinyPowerCapObjects.getOrDefault(
										hashId.intValue(), null);
								
								if (val != null) {
									if (val.index == 7 || entry.getKey().getItemTier() == 6) {
										String dlc = entry.getKey().getDLCName();
										
										if(dlc != null && !dlc.isBlank()) {
											text += "- From DLC: **" + dlc + "**\n";
										}
										
										String season = entry.getKey().getSeasonName();
										
										if(season != null && !season.isBlank()) {
											text += "- From Season: **" + season + "**\n";
										}
										
									} else {
										if (Destiny2ItemSystem.destinySeasons.containsKey(val.index)) {
											Destiny2SeasonObject seasonObject = Destiny2ItemSystem.destinySeasons.get(
													val.index);
											
											if (seasonObject.DLCName != null && !seasonObject.DLCName.isBlank()) {
												text += "- From DLC: **" + seasonObject.DLCName + "**\n";
											}
											
											if (seasonObject.seasonName != null && !seasonObject.seasonName.isBlank()) {
												text += "- From Season: **" + seasonObject.seasonName + "**\n";
											}
										}
									}
								}
							}
						}
					}
				}
				
				
				if(entry.getValue().containsKey("instances")) {
					JSONObject itemObject = entry.getValue().get("instances");
					
					if (itemObject != null) {
						if (itemObject.has("primaryStat")) {
							JSONObject primaryStat = itemObject.getJSONObject("primaryStat");
							
							if (primaryStat.has("value")) {
								text += "- At **" + primaryStat.getInt("value") + "** PL";
							}
						}
						
						if (itemObject.has("isEquipped")) {
							boolean isEquipped = itemObject.getBoolean("isEquipped");
							
							if (isEquipped) {
								charId = "Equipped on " + charId;
							}
						}
					}
				}
				
				text += "\n - Located: " + charId + "\n";
				list.add(text);
			}
			
			pbuilder.clearItems();
			
			Paginator p = pbuilder.setColor(ChatUtils.getEmbedColor(message.getGuild(), author))
					.setItems(list.toArray(new String[0]))
					.setUsers(message.getAuthor())
					.setText(message.getAuthor().getAsMention() + " Found " + itemsFound + " items!")
					.setColumns(1)
					.build();
			
			p.paginate(message.getChannel(), 0);
			
			ChatUtils.deleteMessage(slowMes);
			
			ArrayList<String> numList = new ArrayList<>();
			
			for(int i = 0; i < itemsFound; i++){
				numList.add(Integer.toString(i + 1));
			}
			
			ChatUtils.sendEmbed(channel, author.getAsMention() + " If you wish to view a item please type the number you wish to see", (mes, T) -> ResponseCommand.scheduleSimpleResponse(message, mes, channel, author, numList.toArray(new String[0]), (message1, args1) -> {
				String text = String.join("", args1);
				
				if(Utils.isInteger(text)){
					Integer num = Integer.parseInt(text);
					
					//Offset the number to be based on 0
					if(num != 0){
						num--;
					}
					
					CustomEntry<Destiny2ItemObject, HashMap<String, JSONObject>> entry = itemMap.get(num);
					
					Destiny2LiveItemObject viewItemObject;
					
					String data = JsonUtils.getGson_pretty().toJson(entry.getKey());
					viewItemObject = JsonUtils.getGson_pretty().fromJson(data, Destiny2LiveItemObject.class);
					viewItemObject.perkMap.clear();
					viewItemObject.destinyVersion = 2; //TODO This didnt transfer for some reason
					viewItemObject.preset = true;
					viewItemObject.sockets = new SocketsEntry();
					
					if(entry.getValue().containsKey("instances")) {
						JSONObject itemObject = entry.getValue().get("instances");
						
						if (itemObject != null) {
							if (itemObject.has("primaryStat")) {
								JSONObject primaryStat = itemObject.getJSONObject("primaryStat");
								
								if (primaryStat.has("value")) {
									viewItemObject.currentPower = primaryStat.getInt("value");
								}
							}
						}
					}
					
					if(entry.getValue().containsKey("versionNumber")) {
						JSONObject itemObject = entry.getValue().get("versionNumber");
						
						if (itemObject != null) {
							if (itemObject.has("versionNumber")) {
								int versionNumber = itemObject.getInt("versionNumber");
								viewItemObject.itemVersion = versionNumber;
							}
						}
					}
					
					if(entry.getValue().containsKey("stats")) {
							JSONObject statsData = entry.getValue().get("stats");
							
							if (statsData != null) {
								for (String key : statsData.keySet()) {
									Object ob = statsData.get(key);
									
									if (ob instanceof JSONObject) {
										JSONObject stat = (JSONObject)ob;
										
										if (stat.has("statHash") && stat.has("value")) {
											StatEntryObject ent = new StatEntryObject();
											ent.value = stat.getInt("value");
											ent.statHash = stat.getLong("statHash");
											
											viewItemObject.stats.stats.put(ent.statHash, ent);
										}
									}
								}
							}
						}
					
						if(entry.getValue().containsKey("sockets")) {
							JSONObject perkData = entry.getValue().get("sockets");
							JSONArray perksData = perkData.getJSONArray("sockets");
							
							ArrayList<SocketEntry> entries = new ArrayList<>();
							
							if(perksData != null){
								int i = 0;
								for(Object ob : perksData){
									if(ob instanceof JSONObject){
										JSONObject perk = (JSONObject)ob;
										
										if(perk.has("isVisible")){
											boolean isVisible = perk.getBoolean("isVisible");

											if(!isVisible){
												continue;
											}
										}

										if(perk.has("isEnabled")){
											boolean isEnabled = perk.getBoolean("isEnabled");

											if(!isEnabled){
												continue;
											}
										}

										if(perk.has("plugHash")) {
											Long id = perk.getLong("plugHash");
											Destiny2ItemObject perkOb = Destiny2ItemSystem.destinyItemObjects.getOrDefault(id.intValue(), null);
											
											//TODO id: 3228611386
											
											if(perkOb != null) {
												if(perkOb.itemTypeDisplayName == null || perkOb.itemTypeDisplayName.isBlank()){
													if(!perkOb.displayProperties.name.equalsIgnoreCase("masterwork")) {
														continue;
													}
												}
												
												if(perkOb.itemTypeDisplayName.toLowerCase().contains("shader")) continue;
												if(perkOb.itemTypeDisplayName.toLowerCase().contains("restore defaults")) continue;
												if(perkOb.displayProperties.name.toLowerCase().contains("shader")) continue;
												if(perkOb.displayProperties.description.toLowerCase().contains("shader")) continue;
												
												//Ornament
												if(perkOb.itemSubType == 21){
													if(perkOb.displayProperties.icon != null) viewItemObject.displayProperties.icon = perkOb.displayProperties.icon;
													if(perkOb.screenshot != null) viewItemObject.screenshot = perkOb.screenshot;
													continue;
												}
												
												SocketEntry sEntry = new SocketEntry();
												sEntry.singleInitialItemHash = id;
												sEntry.plugSources = 0;
												entries.add(sEntry);
											}
										}
									}
								i++;
							}
							
							
							viewItemObject.sockets.socketEntries = entries.toArray(new SocketEntry[0]);
						}
					}
					
					if(entry.getValue().containsKey("plugStates")) {
						JSONObject statsData = entry.getValue().get("plugStates");
						
						if(statsData.has("plugStates")){
							JSONArray array = statsData.getJSONArray("plugStates");
							
							for(Object ob : array){
								if(ob instanceof JSONObject){
									JSONObject plugState = (JSONObject)ob;
									System.out.println(plugState);
								}
							}
						}
					}
					
					viewItemObject.finalizeObject();
					DestinyItemCommand.showInfo(channel, viewItemObject);
				}
			}));
			
		}else{
			ChatUtils.sendEmbed(channel, author.getAsMention() + " Found no items with that search phrase!");
		}
	}
	
	@NotNull
	public StringBuilder getCharacterStringBuilder(JSONObject character)
	{
		StringBuilder charactersBuilder = new StringBuilder();
		
		if (character.has("raceType")) {
			int race = character.getInt("raceType");
			String raceType = null;
			
			switch (race) {
				case 0:
					raceType = "Human";
					break;
				case 1:
					raceType = "Awoken";
					break;
				case 2:
					raceType = "Exo";
					break;
				default:
					raceType = null;
					break;
			}
			
			if (raceType != null) {
				charactersBuilder.append("*" + raceType + "* ");
			}
		}
		
		
		if (character.has("classType")) {
			int classType = character.getInt("classType");
			String clas = null;
			
			switch (classType) {
				case 0:
					clas = "Titan";
					break;
				case 1:
					clas = "Hunter";
					break;
				case 2:
					clas = "Warlock";
					break;
				default:
					clas = null;
					break;
			}
			
			if (clas != null) {
				charactersBuilder.append("*" + clas + "* ");
			}
		}
		return charactersBuilder;
	}
	
	@Override
	public String getShortDescription(DiscordCommand sourceCommand, Message callerMessage)
	{
		return "Search for items from your D2 account";
	}
	
	public int getItemsFound(
			String searchPhrase,
			ArrayList<CustomEntry<Destiny2ItemObject, HashMap<String, JSONObject>>> itemMap,
			int itemsFound, JSONObject base, String name)
	{
		JSONObject equip = base.getJSONObject(name);
		
		if (equip.has("data")) {
			JSONObject jData1 = equip.getJSONObject("data");
			
			if(!jData1.has("items")) {
				for (String key : jData1.keySet()) {
					Object datObject = jData1.get(key);
					
					if (datObject instanceof JSONObject) {
						JSONObject jData = (JSONObject)datObject;
						
						itemsFound = getItemsFound(searchPhrase, itemMap, itemsFound, jData, base, key);
					}
				}
			}else{
				itemsFound = getItemsFound(searchPhrase, itemMap, itemsFound, jData1, base, null);
			}
		}
		
		return itemsFound;
	}
	
	public int getItemsFound(
			String searchPhrase, ArrayList<CustomEntry<Destiny2ItemObject, HashMap<String, JSONObject>>> itemMap,
			int itemsFound, JSONObject jData, JSONObject base, String charId)
	{
		
		if (jData.has("items")) {
			JSONArray array = jData.getJSONArray("items");
			
			for (Object ob : array) {
				if (ob instanceof JSONObject) {
					JSONObject itemObject = (JSONObject)ob;
					
					HashMap<String, JSONObject> objectHashMap = new HashMap<>();
					
					if(charId != null){
						HashMap<String, String> map1 = new HashMap<>();
						map1.put("charId", charId);
						objectHashMap.put("charId", new JSONObject(JsonUtils.getGson_pretty().toJson(map1)));
					}
					
					if (itemObject.has("itemInstanceId")) {
						String itemInstanceId = itemObject.getString("itemInstanceId");
						
						if (base.has("itemComponents")) {
							JSONObject itemComponents = base.getJSONObject("itemComponents");
							
							if (itemComponents.has("stats")) {
								JSONObject stats = itemComponents.getJSONObject("stats");
								
								if (stats.has("data")) {
									JSONObject data = stats.getJSONObject("data");
									
									if (data.has(itemInstanceId)) {
										JSONObject stat = data.getJSONObject(itemInstanceId);
										objectHashMap.put("stats", stat);
									}
								}
							}
							
							if (itemComponents.has("sockets")) {
								JSONObject sockets = itemComponents.getJSONObject("sockets");
								
								if (sockets.has("data")) {
									JSONObject data = sockets.getJSONObject("data");
									
									if (data.has(itemInstanceId)) {
										JSONObject stat = data.getJSONObject(itemInstanceId);
										objectHashMap.put("sockets", stat);
									}
								}
							}
							
							if (itemComponents.has("instances")) {
								JSONObject sockets = itemComponents.getJSONObject("instances");
								
								if (sockets.has("data")) {
									JSONObject data = sockets.getJSONObject("data");
									
									if (data.has(itemInstanceId)) {
										JSONObject stat = data.getJSONObject(itemInstanceId);
										objectHashMap.put("instances", stat);
									}
								}
							}
						}
					}
					
					if(itemObject.has("versionNumber")){
						HashMap<String, Integer> map1 = new HashMap<>();
						map1.put("versionNumber", itemObject.getInt("versionNumber"));
						objectHashMap.put("versionNumber", new JSONObject(JsonUtils.getGson_pretty().toJson(map1)));
					}
					
					if (itemObject.has("itemHash")) {
						long id = itemObject.getLong("itemHash");
						
						Destiny2ItemObject destiny2ItemObject = Destiny2ItemSystem.destinyItemObjects.getOrDefault((int)id, null);
						
						if (destiny2ItemObject != null && destiny2ItemObject.equippable) {
							if (validItem(destiny2ItemObject, searchPhrase)) {
								
								itemsFound++;
								itemMap.add(new CustomEntry<>(destiny2ItemObject, objectHashMap));
							}
						}
					}
				}
			}
		}
		return itemsFound;
	}
	
	public static boolean validItem(Destiny2ItemObject object, String search){
		if(object.displayProperties.name.equalsIgnoreCase(search) || object.displayProperties.name.toLowerCase().contains(search.toLowerCase())) return true;
		if(object.itemTypeDisplayName.equalsIgnoreCase(search) || object.itemTypeDisplayName.toLowerCase().contains(search.toLowerCase())) return true;
		return Utils.compareStrings(object.displayProperties.name.toLowerCase(), search.toLowerCase()) <= 3;
	}
	
	@Override
	public String getSlashName()
	{
		return "inventory-search";
	}
	
	@Override
	public OptionData[] commandOptions()
	{
		return new OptionData[]{new OptionData(OptionType.STRING, "item", "Which item you want to search for").setRequired(true)};
	}
	
	@Override
	public void slashCommandExecuted(SlashCommandInteractionEvent slashEvent, Guild guild, User author, SlashCommandChannel channel, SlashCommandMessage message)
	{
		SlashCommandSystem.executeNormalCommand(slashEvent, this, new String[]{slashEvent.getOption("item").getAsString()});
	}
	*/
}
