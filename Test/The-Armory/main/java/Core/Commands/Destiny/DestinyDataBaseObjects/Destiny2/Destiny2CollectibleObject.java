package Core.Commands.Destiny.DestinyDataBaseObjects.Destiny2;

public class Destiny2CollectibleObject extends DatabaseObject
{
	public DisplayProperties displayProperties;
	public long itemHash;
	public long sourceHash;
	public String sourceString;
	public int scope;
	public StateInfo stateInfo;
	
	public static class StateInfo
	{
		public Requirements requirements;
	}
	
	public static class Requirements
	{
		public String entitlementUnavailableMessage;
	}
}
