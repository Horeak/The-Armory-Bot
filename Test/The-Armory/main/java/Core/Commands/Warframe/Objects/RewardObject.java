package Core.Commands.Warframe.Objects;

import Core.Commands.Warframe.Warframe;

import java.util.Map;

public class RewardObject
{
	public int amount;
	public String name;
	
	public RewardObject(String name, int amount)
	{
		this.name = name;
		this.amount = amount;
		
		for (Map.Entry<String, String> ent : Warframe.specificNames.entrySet()) {
			if (name.equalsIgnoreCase(ent.getKey())) {
				this.name = ent.getValue();
				break;
			}
		}
	}
	
	
	@Override
	public String toString()
	{
		return amount + "x " + name;
	}
}
