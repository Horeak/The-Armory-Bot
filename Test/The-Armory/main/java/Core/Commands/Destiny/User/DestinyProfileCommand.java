package Core.Commands.Destiny.User;

import Core.CommandSystem.ChatUtils;
import Core.CommandSystem.CommandObjects.SlashCommands.SlashCommandChannel;
import Core.CommandSystem.CommandObjects.SlashCommands.SlashCommandMessage;
import Core.Commands.Destiny.DestinySlashCommand;
import Core.Commands.Destiny.DestinySystem;
import Core.Commands.Destiny.User.Register.Utils.Destiny2UserUtil;
import Core.Main.Logging;
import Core.Objects.Annotation.Commands.SlashArgument;
import Core.Objects.Annotation.Commands.SubCommand;
import Core.Objects.Interfaces.Commands.ISlashCommand;
import Core.Util.Time.TimeParserUtil;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.joda.time.Instant;
import org.json.JSONArray;
import org.json.JSONObject;

import java.awt.Color;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;


//TODO Add a better platform system. Platform icons and show all destiny accounts registered to the specific user
// on this command

@SubCommand( parent = DestinySlashCommand.class)
public class DestinyProfileCommand implements ISlashCommand
{
	@SlashArgument( key = "user", text = "Which user you want to see the profile for." )
	public User targetUser;
	
	@Override
	public String getSlashName()
	{
		return "profile";
	}
	
	@Override
	public String getDescription()
	{
		return "Shows basic account info about someones destiny account";
	}
	
	@Override
	public void slashCommandExecuted(SlashCommandInteractionEvent slashEvent, Guild guild, User author, SlashCommandChannel channel, SlashCommandMessage message1)
	{
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.UK);
		SimpleDateFormat format1 = new SimpleDateFormat("yyyy/MM/dd", Locale.UK);
		
		format.setTimeZone(TimeZone.getTimeZone("UTC"));
		format1.setTimeZone(TimeZone.getTimeZone("UTC"));
		
		User user = slashEvent.getUser();
		
		if (targetUser != null) {
			user = targetUser;
		}
		
		JSONObject object = Destiny2UserUtil.getData(Destiny2UserUtil.getAccessToken(user),
		                                             Destiny2UserUtil.GET_MEMBERSHIP_PATH);
		
		if (object == null) {
			ChatUtils.sendEmbed(channel,
			                    slashEvent.getUser().getAsMention() + " Found no info on this user! They may not have registered yet.");
			return;
		}
		
		if (object != null) {
			if (object.has("bungieNetUser")) {
				JSONObject ob = object.getJSONObject("bungieNetUser");
				
				EmbedBuilder builder = new EmbedBuilder();
				builder.setColor(Color.orange);
				
				if (ob.has("profilePicturePath")) {
					builder.setThumbnail(Destiny2UserUtil.BASE_BUNGIE_URL + ob.getString("profilePicturePath"));
				}
				
				builder.setTitle("Profile for " + ob.getString("displayName"));
				
				String about = ob.getString("about");
				String firstAccess = ob.getString("firstAccess");
				
				if (about != null && !about.isEmpty()) {
					builder.addField("About", about, false);
				}
				
				try {
					Date dt = format.parse(firstAccess);
					
					if (dt != null) {
						builder.addField("Account created", format1.format(dt), false);
					}
					
				} catch (ParseException e) {
					Logging.exception(e);
				}
				
				
				ArrayList<JSONObject> memberships = new ArrayList<>();
				boolean cross_save = false;
				
				if (object != null) {
					if (object.has("destinyMemberships")) {
						JSONArray array = object.getJSONArray("destinyMemberships");
						
						for (Object ob1 : array) {
							if (ob1 instanceof JSONObject) {
								JSONObject arrayObject = (JSONObject)ob1;
								
								if (arrayObject.has("crossSaveOverride") && arrayObject.getInt("crossSaveOverride") != 0) {
									if (arrayObject.getInt("crossSaveOverride") == arrayObject.getInt("membershipType")) {
										memberships.add(arrayObject);
										cross_save = true;
									}
								} else {
									memberships.add(arrayObject);
								}
							}
						}
					}
				}
				
				StringBuilder raceBuilder = new StringBuilder();
				StringBuilder classBuilders = new StringBuilder();
				StringBuilder powerBuilders = new StringBuilder();
				StringBuilder lastPlayedBuilder = new StringBuilder();
				
				for (JSONObject object1 : memberships) {
					String membershipId = object1.getString("membershipId");
					int membershipType = object1.getInt("membershipType");
					
					JSONObject profileObject = Destiny2UserUtil.getData(Destiny2UserUtil.getAccessToken(slashEvent.getUser()), "/Platform/Destiny2/" + membershipType + "/Profile/" + membershipId + "/?components=200");
					
					if(profileObject.has("characters")) {
						JSONObject charObject = profileObject.getJSONObject("characters");
						if (charObject.has("data")) {
							JSONObject data = charObject.getJSONObject("data");
							
							for (String key : data.keySet()) {
								Object ob1 = data.get(key);
								
								if (ob1 instanceof JSONObject) {
									JSONObject character = (JSONObject)ob1;
									
									if (character.has("genderType")) {
										int genderType = character.getInt("genderType");
										String gender = null;
										
										switch (genderType) {
											case 0:
												gender = "Male";
												break;
											case 1:
												gender = "Female";
												break;
											default:
												gender = null;
												break;
										}
										
										if (gender != null) {
											raceBuilder.append("*" + gender + "* ");
										}
									}
									
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
											raceBuilder.append("*" + raceType + "*\n");
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
											classBuilders.append("*" + DestinySystem.getClassIcon(clas) + " " + clas + "*\n");
										}
									}
									
									if (character.has("light")) {
										int light = character.getInt("light");
										powerBuilders.append("**" + light + "**\n");
									}
									
									//					Date dt = format.parse(firstAccess);
									if (character.has("dateLastPlayed")) {
										String lastPlayed = character.getString("dateLastPlayed");
										Instant instant = Instant.parse(lastPlayed);
										
										if (instant != null) {
											String timeSince = TimeParserUtil.getTime(instant.toDate());
											lastPlayedBuilder.append("**" + timeSince + "**\n");
										}
									}
									
								}
							}
							
							builder.addField("Characters", raceBuilder.toString(), true);
							builder.addField("Class", classBuilders.toString(), true);
							builder.addField("Power", powerBuilders.toString(), true);
							
							builder.addField("Characters", raceBuilder.toString(), true);
							builder.addField("Class", classBuilders.toString(), true);
							builder.addField("Last played", lastPlayedBuilder.toString(), true);
						}
					}
				}
				
				ChatUtils.sendMessage(channel, author.getAsMention(), builder.build());
			}
		}
	}
}
