package Core.Commands;

import Core.CommandSystem.ChatUtils;
import Core.CommandSystem.CommandObjects.SlashCommands.SlashCommandChannel;
import Core.CommandSystem.CommandObjects.SlashCommands.SlashCommandMessage;
import Core.Objects.Annotation.Commands.Command;
import Core.Objects.Annotation.Commands.SlashArgument;
import Core.Objects.Interfaces.Commands.ISlashCommand;
import Core.Util.Time.TimeParserUtil;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.utils.TimeFormat;

@Command
public class TimeFormatCommand implements ISlashCommand
{
	@SlashArgument( key = "time", text = "The time you want the format for.", required = true )
	public String time;
	
	@Override
	public String getSlashName()
	{
		return "timestamp";
	}
	
	@Override
	public void slashCommandExecuted(SlashCommandInteractionEvent slashEvent, Guild guild, User author, SlashCommandChannel channel, SlashCommandMessage message)
	{
		Long timeMil = TimeParserUtil.getTime(time) + System.currentTimeMillis();
		
		StringBuilder builder1 = new StringBuilder();
		StringBuilder builder2 = new StringBuilder();
		StringBuilder builder3 = new StringBuilder();
		
		builder3.append("Time Short\n");
		builder3.append("Time Long\n");
		builder3.append("Date Short\n");
		builder3.append("Date Long\n");
		builder3.append("Date & Time Short\n");
		builder3.append("Date & Time Long\n");
		builder3.append("Relative\n");
		
		builder1.append(TimeFormat.TIME_SHORT.format(timeMil) + "\n");
		builder1.append(TimeFormat.TIME_LONG.format(timeMil) + "\n");
		builder1.append(TimeFormat.DATE_SHORT.format(timeMil) + "\n");
		builder1.append(TimeFormat.DATE_LONG.format(timeMil) + "\n");
		builder1.append(TimeFormat.DATE_TIME_SHORT.format(timeMil) + "\n");
		builder1.append(TimeFormat.DATE_TIME_LONG.format(timeMil) + "\n");
		builder1.append(TimeFormat.RELATIVE.format(timeMil) + "\n");
		
		builder2.append("`" + TimeFormat.TIME_SHORT.format(timeMil) + "`\n");
		builder2.append("`" + TimeFormat.TIME_LONG.format(timeMil) + "`\n");
		builder2.append("`" + TimeFormat.DATE_SHORT.format(timeMil) + "`\n");
		builder2.append("`" + TimeFormat.DATE_LONG.format(timeMil) + "`\n");
		builder2.append("`" + TimeFormat.DATE_TIME_SHORT.format(timeMil) + "`\n");
		builder2.append("`" + TimeFormat.DATE_TIME_LONG.format(timeMil) + "`\n");
		builder2.append("`" + TimeFormat.RELATIVE.format(timeMil) + "`\n");
		
		EmbedBuilder embedBuilder = new EmbedBuilder();
		embedBuilder.addField("Name", builder3.toString(), true);
		embedBuilder.addField("Output", builder1.toString(), true);
		embedBuilder.addField("Format", builder2.toString(), true);
		
		ChatUtils.sendMessage(channel, embedBuilder.build());
	}
}
