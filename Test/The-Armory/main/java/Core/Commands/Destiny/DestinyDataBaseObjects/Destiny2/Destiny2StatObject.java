package Core.Commands.Destiny.DestinyDataBaseObjects.Destiny2;

public class Destiny2StatObject extends DatabaseObject
{
	public DisplayProperties displayProperties;
	public int statCategory;
	public boolean hasComputedBlock;
	public int aggregationType;
	public int index;
	
	public Destiny2StatObject() { }
	
	@Override
	public String toString()
	{
		return "Destiny2StatObject{" + "displayProperties=" + displayProperties + ", statCategory=" + statCategory + ", hasComputedBlock=" + hasComputedBlock + ", aggregationType=" + aggregationType + '}';
	}
}
