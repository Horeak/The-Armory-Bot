package Core.Commands.Destiny.DestinyDataBaseObjects.Destiny2;

public class Destiny2VendorObject extends DatabaseObject
{
	public DisplayProperties displayProperties;
	
	public boolean enabled;
	public boolean visible;
	
	public boolean inhibitSelling;
	public boolean inhibitBuying;
	
	public itemListItem[] itemList;
	
	public Long resetIntervalMinutes;
	public Long resetOffsetMinutes;
	
	
	public static class itemListItem{
		public int vendorItemIndex;
		public Long itemHash;
		public int quantity;
		public int weight;
	}
}
