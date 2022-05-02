package Core.Commands.Warframe.Objects;

import java.util.HashMap;

public class WikiItemObject
{
	public String name;
	public String description;
	public String image;
	public HashMap<String, HashMap<String, String>> info;
	
	public WikiItemObject(String name, String description, String image, HashMap<String, HashMap<String, String>> info)
	{
		this.name = name;
		this.description = description;
		this.image = image;
		this.info = info;
	}
}
