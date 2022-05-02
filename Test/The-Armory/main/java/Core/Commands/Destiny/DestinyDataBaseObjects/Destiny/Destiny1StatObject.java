package Core.Commands.Destiny.DestinyDataBaseObjects.Destiny;

import Core.Commands.Destiny.DestinyDataBaseObjects.Destiny2.DatabaseObject;

public class Destiny1StatObject extends DatabaseObject
{
	public String statName;
	public String statDescription;
	public String statIdentifier;
	public String icon;
	public int statCategory;
	public boolean hasComputedBlock;
	public int aggregationType;
	
	public Destiny1StatObject() { }
}
