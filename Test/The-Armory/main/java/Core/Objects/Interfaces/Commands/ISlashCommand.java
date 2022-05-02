package Core.Objects.Interfaces.Commands;

import Core.CommandSystem.CommandObjects.SlashCommands.SlashCommandChannel;
import Core.CommandSystem.CommandObjects.SlashCommands.SlashCommandMessage;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.privileges.CommandPrivilege;

import java.util.EnumSet;

public interface ISlashCommand extends IBaseSlashCommand
{
	default OptionData[] commandOptions(){
		return new OptionData[0];
	}
	default CommandPrivilege[] commandPrivileges(Guild guild){
		return new CommandPrivilege[0];
	}
	default EnumSet<Permission> getRequiredPermissions() { return null; }
	
	void slashCommandExecuted(SlashCommandInteractionEvent slashEvent, Guild guild, User author, SlashCommandChannel channel, SlashCommandMessage message);
}
