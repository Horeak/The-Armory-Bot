package Core.Commands.Destiny.User.Register.Objects;

public enum AccountTypes
{
	STEAM("Steam", null),
	BATTLENET("Battle.net", null),
	XBOX("XBOX", null),
	PSN("PSN", null),
	STADIA("Stadia", null),
	CROSS_SAVE("Cross Save", null);
	
	public String name;
	public String icon;
	
	AccountTypes(String name, String icon)
	{
		this.name = name;
		this.icon = icon;
	}
}
