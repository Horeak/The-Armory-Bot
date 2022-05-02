package Core.Commands.Destiny.DestinyDataBaseObjects.Destiny2;

public class Destiny2StatGroupObject extends DatabaseObject
{
	public int maximumValue;
	public scaledStat[] scaledStats;
	
	public static class scaledStat
	{
		public Long statHash;
		public int maximumValue;
		public boolean displayAsNumeric;
		public displayInterpolation[] displayInterpolation;
	}
	
	public static class displayInterpolation
	{
		public int value;
		public int weight;
		
		@Override
		public String toString()
		{
			return "displayInterpolation{" + "value=" + value + ", weight=" + weight + '}';
		}
	}
}
