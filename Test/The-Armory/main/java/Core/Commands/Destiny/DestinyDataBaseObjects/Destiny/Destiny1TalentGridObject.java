package Core.Commands.Destiny.DestinyDataBaseObjects.Destiny;

import java.util.Arrays;

public class Destiny1TalentGridObject
{
	public Long gridHash;
	public Long hash;
	public Node[] nodes;
	
	public static class Node
	{
		public boolean autoUnlocks;
		public int column;
		public boolean isRandom;
		public int nodeHash;
		public int row;
		public Step[] steps;
		
		
		@Override
		public String toString()
		{
			return "Node{" + "autoUnlocks=" + autoUnlocks + ", column=" + column + ", isRandom=" + isRandom + ", nodeHash=" + nodeHash + ", row=" + row + ", steps=" + Arrays.toString(
					steps) + '}';
		}
	}
	
	public static class Step
	{
		public int damageType;
		public Long damageTypeHash;
		public String icon;
		public String nodeStepName;
		public String nodeStepDescription;
		public Long nodeStepHash;
		public Long[] perkHashes;
		public Long[] statHashes;
		public boolean affectsLevel;
		public boolean affectsQuality;
		
		
		@Override
		public String toString()
		{
			return "Step{" + "damageType=" + damageType + ", damageTypeHash=" + damageTypeHash + ", icon='" + icon + '\'' + ", nodeStepName='" + nodeStepName + '\'' + ", nodeStepDescription='" + nodeStepDescription + '\'' + ", nodeStepHash=" + nodeStepHash + ", perkHashes=" + Arrays.toString(
					perkHashes) + ", statHahes=" + Arrays.toString(
					statHashes) + ", affectsLevel=" + affectsLevel + ", affectsQuality=" + affectsQuality + '}';
		}
	}
}
