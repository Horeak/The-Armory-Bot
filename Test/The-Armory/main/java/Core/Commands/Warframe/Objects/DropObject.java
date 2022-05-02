package Core.Commands.Warframe.Objects;

import java.util.Objects;

public class DropObject
{
	
	public String name;
	
	public String rarity, place;
	
	public String rotation, mode;
	public String refinedState;
	public double chance;
	public int amount;
	
	public DropObject(
			 String name,  String rarity,  String place,  String rotation,
			double chance, int amount)
	{
		if (name != null) {
			this.name = name;
		}
		if (rarity != null) {
			this.rarity = rarity;
		}
		if (place != null) {
			this.place = place;
		}
		if (rotation != null) {
			this.rotation = rotation;
		}
		if (chance > 0.000) {
			this.chance = chance;
		}
		if (amount > 0) {
			this.amount = amount;
		}
	}
	
	@Override
	public int hashCode()
	{
		return Objects.hash(rarity, place, rotation, mode, refinedState, chance, amount);
	}
	
	@Override
	public boolean equals(Object o)
	{
		if (this == o) {
			return true;
		}
		if (!(o instanceof DropObject)) {
			return false;
		}
		DropObject that = (DropObject)o;
		return Double.compare(that.chance, chance) == 0 && amount == that.amount && Objects.equals(rarity,
		                                                                                           that.rarity) && Objects.equals(
				place, that.place) && Objects.equals(rotation, that.rotation) && Objects.equals(mode,
		                                                                                        that.mode) && Objects.equals(
				refinedState, that.refinedState);
	}
	
	
	@Override
	public String toString()
	{
		return "DropObject{" + "rarity='" + rarity + '\'' + ", place='" + place + '\'' + ", rotation='" + rotation + '\'' + ", mode='" + mode + '\'' + ", refinedState='" + refinedState + '\'' + ", chance=" + chance + ", amount=" + amount + '}';
	}
}
