package Core.Commands;

import Core.CommandSystem.ChatUtils;
import Core.CommandSystem.CommandObjects.ComponentSystem.ComponentResponseSystem;
import Core.CommandSystem.CommandObjects.SlashCommands.SlashCommandChannel;
import Core.CommandSystem.CommandObjects.SlashCommands.SlashCommandMessage;
import Core.Main.Logging;
import Core.Main.Startup;
import Core.Objects.Annotation.Commands.Command;
import Core.Objects.Annotation.Commands.SlashArgument;
import Core.Objects.Annotation.Fields.DataObject;
import Core.Objects.Annotation.Method.EventListener;
import Core.Objects.Annotation.Method.Interval;
import Core.Objects.Annotation.Method.Startup.PostInit;
import Core.Objects.Interfaces.Commands.ISlashCommand;
import Core.Util.Utils;
import com.google.common.io.Files;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.SelectMenuInteractionEvent;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.selections.SelectMenu;
import net.dv8tion.jda.api.interactions.components.selections.SelectOption;
import net.dv8tion.jda.api.requests.restaction.MessageAction;
import net.dv8tion.jda.api.requests.restaction.WebhookMessageAction;
import net.dv8tion.jda.api.requests.restaction.interactions.ReplyCallbackAction;
import net.dv8tion.jda.internal.entities.DataMessage;
import org.kohsuke.github.*;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.security.Key;
import java.security.KeyFactory;
import java.security.spec.PKCS8EncodedKeySpec;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;

@Command
public class BugReportCommand implements ISlashCommand
{
	//TODO Make a way to block people from using the bug report command if too many of their issues are marked as spam
	
	//TODO Add a command to view current open bug reports on discord
	//TODO Make a command to allow responding to issues through the bot to for example ask for more info
	
	@DataObject( file_path = "bugReports.json", name = "issues" )
	public static CopyOnWriteArrayList<IssueObject> issuesReported = new CopyOnWriteArrayList<>();
	
	private static GitHub github;
	
	@PostInit
	public static void init(){
		try {
			GHAppInstallation installation = getInstallation();
			GHAppInstallationToken token = installation.createToken().create();
			
			GitHubBuilder gb1 = new GitHubBuilder();
			github = gb1.withAppInstallationToken(token.getToken()).build();
			
		} catch (Exception e) {
			Logging.exception(e);
		}
	}
	
	public static GHAppInstallation getInstallation() throws Exception
	{
		GitHubBuilder gb = new GitHubBuilder();
		String jwt = createJWT();
		GitHub githubT = gb.withJwtToken(jwt).build();
		return githubT.getApp().getInstallationByRepository("Horeak", "The-Armory");
	}
	
	public static GitHub getGithub(){
		if(github != null){
			if(github.isCredentialValid()){
				return github;
			}
		}
		
		try {
			GHAppInstallation installation = getInstallation();
			GHAppInstallationToken token = installation.createToken().create();
			
			GitHubBuilder gb1 = new GitHubBuilder();
			github = gb1.withAppInstallationToken(token.getToken()).build();
			
			return github;
			
			
		} catch (Exception e) {
			Logging.exception(e);
		}
		
		
		return null;
	}
	
	public static GHRepository getRepo(GitHub gh){
		try {
			return gh.getRepositoryById(138820606L);
		} catch (IOException e) {
			Logging.exception(e);
		}
		
		return null;
	}
	
	
	@Interval(time_interval = 5, time_unit = TimeUnit.HOURS, initial_delay = 0)
	public static void closedCheck(){
		GitHub github = getGithub();
		
		if(github != null && github.isCredentialValid()) {
			GHRepository repo = getRepo(github);
			
			if(repo != null && repo.hasIssues()) {
				for (IssueObject object : issuesReported) {
					try {
						GHIssue issue = repo.getIssue(Math.toIntExact(object.githubIssueID));
						
						if(issue != null){
							if(object.issueStatus == null || object.issueStatus != issue.getState()){
								updateIssue(object, issue);
							}
							
							if(object.issueStatus == GHIssueState.CLOSED){
								if(System.currentTimeMillis() - object.issueTime >= TimeUnit.MILLISECONDS.convert(30, TimeUnit.DAYS)){
									removeIssue(object);
								}
							}
						}
					} catch (GHFileNotFoundException e1){
						if(System.currentTimeMillis() - object.issueTime >= TimeUnit.MILLISECONDS.convert(30, TimeUnit.DAYS)){
							removeIssue(object);
						}
						
					} catch (IOException e) {
						Logging.exception(e);
					}
				}
			}
		}
	}
	
	public static void addIssue(IssueObject object){
		GitHub github = getGithub();
		
		if(github != null && github.isCredentialValid()) {
			GHRepository repo = getRepo(github);
			
			if (repo != null && repo.hasIssues()) {
				User user = Utils.getUser(object.userID);
				
				if (user != null) {
					String commandName = null;
					
					String title = "Bug Report " + (commandName != null && !commandName.isBlank() ? " about " + commandName : "") + " by " + user.getAsTag();
					GHIssueBuilder builder = repo.createIssue(title);
					
					builder.label("Bug");
					
					StringBuilder sb = new StringBuilder();
					sb.append("| | |\n|-|-:|\n");
					
					if(commandName != null && !commandName.isBlank()) {
						sb.append("|Command|" + commandName + "|\n");
					}
					
					Date date = new Date();
					SimpleDateFormat formatTime = new SimpleDateFormat("HH:mm:ss");
					SimpleDateFormat formatDate = new SimpleDateFormat("dd-MM-yyyy");
					
					sb.append("|__Reported by__|__" + user.getAsTag() + "__|\n");
					
					sb.append("|__Time__|" + formatTime.format(date) + "|\n");
					sb.append("|__Date__|" + formatDate.format(date) + "|\n");
					
					sb.append("|__Can contact__|__" + (object.canBeContacted ? "Yes" : "No") + "__|\n");
					sb.append("|__Alerted on fix__|__" + (object.wishToKnowWhenFixed ? "Yes" : "No") + "__|\n");
					sb.append("|__Description__|_" + object.issueDescription + "_|\n");
					
					builder.body(sb.toString());
					
					try {
						GHIssue issue = builder.create();
						
						if (issue != null) {
							object.githubIssueID = issue.getNumber();
						}
					} catch (IOException e) {
						Logging.exception(e);
					}
					
					initNewIssue(object);
				}
			}
		}
	}
	
	private static final Long channelID = 815631215805595709L;
	
	private static void initNewIssue(IssueObject object){
		object.issueStatus = GHIssueState.OPEN;
		
		if(!issuesReported.contains(object)) {
			issuesReported.add(object);
			createDiscordMessage(object);
		}
	}
	
	private static void createDiscordMessage(IssueObject object){
		TextChannel channel = Startup.discordClient.getTextChannelById(channelID);
		
		if(channel != null){
			MessageAction action = channel.sendMessageEmbeds(genEmbed(object));
			
			SelectMenu menu = SelectMenu.create("github:" + object.githubIssueID)
					.setPlaceholder("Change status of the github issue")
					.setRequiredRange(1, 1)
					.addOptions(SelectOption.of("Open", "open").withDescription("Change the status of the issue to OPEN").withDefault(object.issueStatus == GHIssueState.OPEN), SelectOption.of("Closed", "closed").withDescription("Change the status of the issue to CLOSED").withDefault(object.issueStatus == GHIssueState.CLOSED))
					.build();
			
			action.setActionRows(ActionRow.of(menu));
			action.queue((mes) -> object.discordStatusMessage = mes.getIdLong());
		}
	}
	
	@EventListener
	public static void SelectMenuInteractionEvent(SelectMenuInteractionEvent event)
	{
		String id = event.getComponentId();
		
		if(id.startsWith("github:")){
			if(Startup.appInfo.getOwner().getIdLong() == event.getUser().getIdLong()) {
				String ghId = id.substring("github:".length());
				String command = event.getSelectedOptions().get(0).getValue();
				
				GitHub github = getGithub();
				
				if(Utils.isInteger(ghId)) {
					int gitId = Integer.parseInt(ghId);
					
					if (github != null && github.isCredentialValid()) {
						GHRepository repo = getRepo(github);
						if (repo != null && repo.hasIssues()) {
							for (IssueObject object : issuesReported) {
								if (object.githubIssueID == gitId) {
									
									try {
										GHIssue issue = repo.getIssue(Math.toIntExact(object.githubIssueID));
										
										if (issue != null) {
											event.deferEdit().queue();
											
											if(command.equalsIgnoreCase("closed")){
												issue.close();
											}else if(command.equalsIgnoreCase("open")){
												issue.reopen();
											}
											
											issue = repo.getIssue(Math.toIntExact(object.githubIssueID));
											updateIssue(object, issue);
										}
										
									} catch (GHFileNotFoundException e1) {
										if (System.currentTimeMillis() - object.issueTime >= TimeUnit.MILLISECONDS.convert(
												30, TimeUnit.DAYS)) {
											removeIssue(object);
										}
										
									} catch (IOException e) {
										Logging.exception(e);
									}
								}
							}
						}
					}
				}
			}
		}
	}
	
	private static MessageEmbed genEmbed(IssueObject object){
		EmbedBuilder builder = new EmbedBuilder();
		
		Color c = object.issueStatus == GHIssueState.CLOSED ? Color.red : Color.GREEN.darker();
		
		builder.setColor(c);
		
		String title = "Issue #" + object.githubIssueID;
		builder.setTitle(title, "https://github.com/Horeak/The-Armory/issues/" + object.githubIssueID);
		
		StringBuilder builder1 = new StringBuilder();
		
		builder1.append("**Status**: *" + (object.issueStatus == GHIssueState.CLOSED ? "CLOSED" : "OPEN") + "*\n");
		builder1.append("**Reported by**: *" + object.userTag + "*\n");
		builder1.append("\n**Description**:\n`" + Utils.limitString(object.issueDescription, 120) + "`");
		
		builder.setDescription(builder1);
		builder.setFooter(Startup.discordClient.getSelfUser().getName(), Startup.discordClient.getSelfUser().getAvatarUrl());
		builder.setTimestamp(Instant.ofEpochMilli(object.issueTime));
		
		return builder.build();
	}
	
	private static void updateIssue(IssueObject object, GHIssue issue){
		object.issueStatus = issue.getState();
		
		if(object.discordStatusMessage == null){
			createDiscordMessage(object);
		}else{
			TextChannel channel = Startup.discordClient.getTextChannelById(channelID);
			
			if(channel != null){
				channel.retrieveMessageById(object.discordStatusMessage).queue((mes) -> {
					MessageAction action = mes.editMessageEmbeds(genEmbed(object));
					SelectMenu menu = SelectMenu.create("github:" + object.githubIssueID)
							.setPlaceholder("Change status of the github issue")
							.setRequiredRange(1, 1)
							.addOptions(SelectOption.of("Open", "open").withDescription("Change the status of the issue to OPEN").withDefault(issue.getState() == GHIssueState.OPEN), SelectOption.of("Closed", "closed").withDescription("Change the status of the issue to CLOSED").withDefault(issue.getState() == GHIssueState.CLOSED))
							.build();
					
					action.setActionRows(ActionRow.of(menu));
					action.queue();
				});
			}
		}
		
		if(issue.getState() == GHIssueState.CLOSED){
			if(object.wishToKnowWhenFixed){
				User user = Utils.getUser(object.userID);
				
				if(user != null){
					PrivateChannel channel = user.openPrivateChannel().complete();
					if(channel != null) {
						ChatUtils.sendEmbed(channel, "The issue you reported has now been marked as fixed!");
					}
				}
			}
		}
	}
	
	private static void removeIssue(IssueObject object){
		if(object.discordStatusMessage != null){
			TextChannel channel = Startup.discordClient.getTextChannelById(channelID);
			
			if(channel != null){
				channel.retrieveMessageById(object.discordStatusMessage).queue((mes) -> mes.delete().queue());
			}
		}
		
		issuesReported.remove(object);
	}
	
	
	@Override
	public String getDescription()
	{
		return "Report any issues you may find with the bot";
	}
	
	@SlashArgument( key = "issue", text = "Explain what the issue is about.", required = true )
	public String issue;
	
	@SlashArgument( key = "command", text = "What command is this bug report about?" )
	public String command;
	
	@Override
	public String getSlashName()
	{
		return "bugreport";
	}
	
	@Override
	public void slashCommandExecuted(SlashCommandInteractionEvent slashEvent, Guild guild, User author, SlashCommandChannel channel, SlashCommandMessage message)
	{
		EmbedBuilder builder = new EmbedBuilder();
		builder.setDescription("Are you okay with being contacted about the issue if more details are needed?");
		ChatUtils.setEmbedColor(slashEvent.getGuild(), slashEvent.getUser(), builder);
		
		DataMessage mes = new DataMessage(false, null, null, Arrays.asList(builder.build()));
		
		ReplyCallbackAction action = slashEvent.reply(mes);
		action.setEphemeral(true);
		action.addActionRow(ComponentResponseSystem.addComponent(slashEvent.getUser(), slashEvent, Button.success("id", "Yes"), (e) -> {
			nextSlash(slashEvent, issue, command, true);
		}), ComponentResponseSystem.addComponent(slashEvent.getUser(), slashEvent, Button.danger("id", "No"), (e) -> {
			nextSlash(slashEvent, issue, command, false);
		}));
		
		action.queue();
	}
	private static void nextSlash(SlashCommandInteractionEvent event, String issue, String command, boolean contact){
		EmbedBuilder builder = new EmbedBuilder();
		builder.setDescription(" Do you wish to be notified when the issue is resolved?");
		ChatUtils.setEmbedColor(event.getGuild(), event.getUser(), builder);
		
		DataMessage mes = new DataMessage(false, null, null, Arrays.asList(builder.build()));
		
		WebhookMessageAction act = event.getHook().sendMessage(mes);
		act.setEphemeral(true);
		
		act.addActionRow(ComponentResponseSystem.addComponent(event.getUser(), event, Button.success("id", "Yes"), (e) -> {
			IssueObject object = new IssueObject();
			
			object.whichCommand = command;
			object.canBeContacted = contact;
			object.issueDescription = issue;
			object.wishToKnowWhenFixed = true;
			object.issueTime = System.currentTimeMillis();
			object.userID = event.getUser().getIdLong();
			object.userTag = event.getUser().getAsTag();
			
			addIssue(object);
			slashDone(event, object);
			
		}), ComponentResponseSystem.addComponent(event.getUser(), event, Button.danger("id", "No"), (e) -> {
			IssueObject object = new IssueObject();
			
			object.whichCommand = command;
			object.canBeContacted = contact;
			object.issueDescription = issue;
			object.wishToKnowWhenFixed = false;
			object.issueTime = System.currentTimeMillis();
			object.userID = event.getUser().getIdLong();
			object.userTag = event.getUser().getAsTag();
			
			addIssue(object);
			slashDone(event, object);
		}));
		
		act.queue();
	}
	
	public static void slashDone(SlashCommandInteractionEvent event, IssueObject object){
		EmbedBuilder builder = new EmbedBuilder();
		builder.setDescription("Your issue has now been reported! Thank you for helping development of the bot!" + (object.wishToKnowWhenFixed ? "\nYou will be notified when the issue has been resolved!" : ""));
		ChatUtils.setEmbedColor(event.getGuild(), event.getUser(), builder);
		
		DataMessage mes = new DataMessage(false, null, null, Arrays.asList(builder.build()));
		
		WebhookMessageAction act = event.getHook().sendMessage(mes);
		act.setEphemeral(true);
		act.queue();
	}
	
	
	public static class IssueObject{
		public Long userID;
		public String userTag;
		
		public boolean canBeContacted;
		public boolean wishToKnowWhenFixed;
		
		public String whichCommand;
		public String issueDescription;
		
		public Long issueTime;
		
		public int githubIssueID;
		public Long discordStatusMessage;
		
		public GHIssueState issueStatus;
	}
	
	private static final ResourceBundle properties = ResourceBundle.getBundle("github",
	                                                                          new ResourceBundle.Control() {
		                                                                          @Override
		                                                                          public List<Locale> getCandidateLocales(String name,
				                                                                          Locale locale) {
			                                                                          return Collections.singletonList(Locale.ROOT);
		                                                                          }
	                                                                          });
	
	public static String createJWT() throws Exception {
		String appId = properties.getString("app_id");
		
		SignatureAlgorithm signatureAlgorithm = SignatureAlgorithm.RS256;
		
		long nowMillis = System.currentTimeMillis();
		Date now = new Date(nowMillis);
		
		File fe = new File(Startup.FilePath + "/../github.cert.der");
		byte[] keyBytes = Files.toByteArray(fe);
		
		PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
		KeyFactory kf = KeyFactory.getInstance("RSA");
		
		Key signingKey = kf.generatePrivate(spec);
		
		JwtBuilder builder = Jwts.builder()
				.setIssuedAt(now)
				.setIssuer(appId)
				.signWith(signingKey, signatureAlgorithm);
		
		long expMillis = nowMillis + 60000;
		Date exp = new Date(expMillis);
		builder.setExpiration(exp);
		
		return builder.compact();
	}
}
