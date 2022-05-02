package Core.CommandSystem.CommandObjects.ResponseSystem;

import net.dv8tion.jda.api.entities.Message;

public class ResponseObject
{
	final ResponseAction object;
	final Long time;
	final Message issuedMessage;
	
	public ResponseObject(Message issuedMessage, ResponseAction object)
	{
		this.object = object;
		this.issuedMessage = issuedMessage;
		this.time = System.currentTimeMillis();
	}
}
