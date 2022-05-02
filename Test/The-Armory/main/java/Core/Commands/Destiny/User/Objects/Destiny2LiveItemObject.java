package Core.Commands.Destiny.User.Objects;

import Core.Commands.Destiny.DestinyDataBaseObjects.Destiny2.Destiny2ItemObject;

public class Destiny2LiveItemObject extends Destiny2ItemObject
{
	public int currentPower;
	
	@Override
	public void finalizeObject()
	{
		super.finalizeObject();
		
		if (currentPower != 0) {
			powerInfo.put("Current Power", "**" + currentPower + "**");
		}
		stats.stats.clear();
	}
}
