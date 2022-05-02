package Core.Commands.Generic.Status;

import Core.CommandSystem.CommandObjects.SlashCommands.SlashCommandChannel;
import Core.CommandSystem.CommandObjects.SlashCommands.SlashCommandMessage;
import Core.Objects.Annotation.Commands.Command;
import Core.Objects.Annotation.Commands.SlashArgument;
import Core.Objects.Annotation.Debug;
import Core.Objects.Interfaces.Commands.ISlashCommand;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

@Debug
@Command
public class TestingCommand implements ISlashCommand
{
	@SlashArgument( key = "target", text = "The user to poke", required = true)
	public User user;
	
	@SlashArgument( key = "choice", text = "make a choice", choices = {"Choice 1", "Choice 2", "Choice 3"})
	public String choice;
	
	@Override
	public String getSlashName()
	{
		return "test";
	}
	
	@Override
	public void slashCommandExecuted(SlashCommandInteractionEvent slashEvent, Guild guild, User author, SlashCommandChannel channel, SlashCommandMessage message)
	{
		slashEvent.reply(choice + " | Poke -> " + user.getAsMention()).queue();
	}
}
