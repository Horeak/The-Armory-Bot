package Core.Commands.Destiny.Objects;

public class MasterworkObject
{
	public String name;
	public String description;
	public String icon;
	public Long statTypeHash;
	public int value;
	public boolean isConditionallyActive;
	public String statName;
	public String statDescription;
	
	public MasterworkObject(
			String name, String description, String icon, Long statTypeHash, int value, boolean isConditionallyActive)
	{
		this.name = name;
		this.description = description;
		this.icon = icon;
		this.statTypeHash = statTypeHash;
		this.value = value;
		this.isConditionallyActive = isConditionallyActive;
	}
}
