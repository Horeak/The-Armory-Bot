package Core.Commands.Generic;

import Core.CommandSystem.ChatUtils;
import Core.CommandSystem.CommandObjects.SlashCommands.SlashCommandChannel;
import Core.CommandSystem.CommandObjects.SlashCommands.SlashCommandMessage;
import Core.Objects.Annotation.Commands.Command;
import Core.Objects.Annotation.Commands.SlashArgument;
import Core.Objects.Interfaces.Commands.ISlashCommand;
import Core.Util.Utils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.StringJoiner;

@Command
public class UserInfoCommand implements ISlashCommand
{
	private static final DateFormat formatterTime = new SimpleDateFormat("MMMMM d, yyyy HH:mm:ss", Locale.US);
	
	@SlashArgument( key = "user", text = "Which user you want to see info for", required = true )
	public User targetUser;
	
	@Override
	public String getDescription()
	{
		return "Shows basic information about users";
	}

	@Override
	public String getSlashName()
	{
		return "user-info";
	}
	
	@Override
	public void slashCommandExecuted(SlashCommandInteractionEvent slashEvent, Guild guild, User author, SlashCommandChannel channel, SlashCommandMessage message1)
	{
		EmbedBuilder builder = new EmbedBuilder();
		
		builder.addField("ID", targetUser.getId(), true);
		builder.setThumbnail(targetUser.getAvatarUrl());
		
		Member mem = Utils.getMember(guild, targetUser);
		
		if(mem != null) {
			builder.setDescription(mem.getEffectiveName());
			ChatUtils.setEmbedColor(mem, builder);
			
			//			builder.addField("Status", mem.getOnlineStatus().getKey().toUpperCase(), true);
			builder.addField("Nickname", targetUser.getName().equals(mem.getEffectiveName()) ? "None" : mem.getNickname(),
			                 true);
			
			Instant createdInstant = mem.getTimeCreated().toInstant();
			Date createdDate = new Date(createdInstant.toEpochMilli());
			
			Instant joinInstant = mem.getTimeJoined().toInstant();
			Date joinDate = new Date(joinInstant.toEpochMilli());
			
			builder.addField("Account Created", formatterTime.format(createdDate), false);
			builder.addField("Join Date", formatterTime.format(joinDate), false);
			
			OffsetDateTime boostedSince = mem.getTimeBoosted();
			
			if (boostedSince != null) {
				builder.addField("Nitro boosted since", formatterTime.format(new Date(boostedSince.toInstant().toEpochMilli())), false);
			}
			
			List<Role> roles = mem.getRoles();
			final int[] roleNum = {0};
			StringJoiner joiner = new StringJoiner(", ");
			roles.forEach((r) -> {
				joiner.add(r.getAsMention());
				roleNum[0] += 1;
			});
			
			builder.addField("Roles [" + roleNum[0] + "]", joiner.toString(), false);
		}
		ChatUtils.sendMessage(channel, builder.build());
	}
}
