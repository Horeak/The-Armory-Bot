package Core.Commands.Generic;

import Core.CommandSystem.ChatUtils;
import Core.CommandSystem.CommandObjects.SlashCommands.SlashCommandChannel;
import Core.CommandSystem.CommandObjects.SlashCommands.SlashCommandMessage;
import Core.CommandSystem.SlashMessageBuilder;
import Core.Main.BotListApi;
import Core.Main.Startup;
import Core.Objects.Annotation.Commands.Command;
import Core.Objects.Annotation.Fields.VariableState;
import Core.Objects.Interfaces.Commands.ISlashCommand;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.callbacks.IReplyCallback;
import net.dv8tion.jda.api.interactions.components.buttons.Button;

@VariableState( variable_class = "Core.Main.BotListApi", variable_name = "VOTING_ENABLED" )
@Command
public class VoteCommand implements ISlashCommand
{
	@Override
	public String getDescription()
	{
		return "Shows whether or not you have voted for the bot today.";
	}
	
	@Override
	public String getSlashName()
	{
		return "vote";
	}
	
	@Override
	public void slashCommandExecuted(SlashCommandInteractionEvent slashEvent, Guild guild, User author, SlashCommandChannel channel, SlashCommandMessage message)
	{
		if(channel instanceof SlashCommandChannel){
			SlashCommandChannel ch = (SlashCommandChannel)channel;
			if(ch.event instanceof IReplyCallback){
				((IReplyCallback)ch.event).deferReply(true).queue();
			}
		}
		
		BotListApi.api.getVotingMultiplier().whenComplete((multiplier, e1) -> BotListApi.api.hasVoted(author.getId()).whenComplete((hasVoted, e2) -> {
			EmbedBuilder builder = new EmbedBuilder();
			
			builder.setAuthor("Daily voting", null, Startup.discordClient.getSelfUser().getAvatarUrl());
			builder.setThumbnail(author.getAvatarUrl());
			
			builder.setColor(ChatUtils.getEmbedColor(guild, author));
			ChatUtils.setFooter(builder,author, channel);
			
			if(hasVoted){
				builder.setDescription("You have already voted today!\nThank you for supporting the bot!");
			}else{
				if(multiplier.isWeekend()){
					builder.setDescription("You have not yet voted today and votes are currently worth 2x due to the weekend!\nPlease consider helping to support the bot by voting over at top.gg with the link below!");
				}else{
					builder.setDescription("You have not yet voted today.\nPlease consider helping to support the bot by voting over at top.gg using the link below!");
				}
			}
			
			SlashMessageBuilder slashBuilder = ChatUtils.createSlashMessage(author, channel);
			slashBuilder.withEmbed(builder);
			
			if(!hasVoted){
				slashBuilder.addAction(Button.link(BotListApi.VOTE_URL, "Vote here"));
			}
			
			slashBuilder.send();
		}));
	}
}
