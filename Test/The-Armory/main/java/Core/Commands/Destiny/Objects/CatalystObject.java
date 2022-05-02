package Core.Commands.Destiny.Objects;

public class CatalystObject
{
	public String name;
	public String description;
	public String unlock;
	public String stats;
	
	public CatalystObject(String name, String description, String unlock, String stats)
	{
		this.name = name;
		this.description = description;
		this.unlock = unlock;
		this.stats = stats;
	}
}
