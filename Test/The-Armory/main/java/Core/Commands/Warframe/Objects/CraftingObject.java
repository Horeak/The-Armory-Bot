package Core.Commands.Warframe.Objects;

public class CraftingObject
{
	public ItemObject item;
	public int count;
	public boolean tradable;
	
	public CraftingObject(ItemObject item, int count, boolean tradable)
	{
		this.item = item;
		this.count = count;
		this.tradable = tradable;
	}
}
