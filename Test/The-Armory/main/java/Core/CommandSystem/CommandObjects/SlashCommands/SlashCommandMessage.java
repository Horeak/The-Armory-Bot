package Core.CommandSystem.CommandObjects.SlashCommands;

import gnu.trove.set.hash.TLongHashSet;
import net.dv8tion.jda.api.entities.MessageType;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.callbacks.IDeferrableCallback;
import net.dv8tion.jda.internal.entities.ReceivedMessage;

import java.util.ArrayList;

public class SlashCommandMessage extends ReceivedMessage
{
	public SlashCommandMessage(IDeferrableCallback event)
	{
		super(0l,
		      new SlashCommandChannel(event),
		      MessageType.DEFAULT,
		      null,
		      false,
		      false,
		      new TLongHashSet(),
		      new TLongHashSet(),
		      false,
		      false,
		      event instanceof SlashCommandInteractionEvent ? ((SlashCommandInteractionEvent)event).getName() : event instanceof ButtonInteractionEvent ? ((ButtonInteractionEvent)event).getComponentId() : null,
		      null,
		      event.getUser(),
		      event.getMember(),
		      null,
		      null,
		      new ArrayList<>(),
		      new ArrayList<>(),
		      new ArrayList<>(),
		      new ArrayList<>(),
		      new ArrayList<>(),
		      0,
		      null);
	}
}
