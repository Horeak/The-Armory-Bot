package Core.CommandSystem.CommandObjects;

import net.dv8tion.jda.api.entities.Message;

@FunctionalInterface
public interface ArgumentMessageRunnable
{
	void run(Message mes, String[] args);
}
