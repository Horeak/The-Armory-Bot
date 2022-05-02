package Core.CommandSystem.CommandObjects.ResponseSystem;

import net.dv8tion.jda.api.entities.Message;

public abstract class ResponseAction
{
	public abstract boolean isValidInput(Message message, String[] args);
	public abstract void execute(Message message, String[] args);
	public abstract void timeout();
}
