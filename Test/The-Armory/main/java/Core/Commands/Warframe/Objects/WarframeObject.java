package Core.Commands.Warframe.Objects;

import java.util.ArrayList;
import java.util.Objects;

public class WarframeObject
{
	public String name;
	public String description;
	public int color;
	public int health;
	public int shield;
	public int armor;
	public int energy;
	public int masteryLevel;
	public double sprint;
	public String gender;
	public String wikiUrl;
	public String wikiImage;
	public ArrayList<CraftingObject> craftingObjects;
	public ArrayList<ItemObject> abilities;
	public ArrayList<String> polarities;
	public String aura;
	public boolean vaulted;
	
	public WarframeObject(
			String name, String description, int color, int health, int shield, int armor, int energy, int masteryLevel,
			double sprint, String gender, String wikiUrl, String wikiImage, ArrayList<CraftingObject> craftingObjects,
			ArrayList<ItemObject> abilities, ArrayList<String> polarities, String aura, boolean vaulted)
	{
		this.name = name;
		this.description = description;
		this.color = color;
		this.health = health;
		this.shield = shield;
		this.armor = armor;
		this.energy = energy;
		this.masteryLevel = masteryLevel;
		this.sprint = sprint;
		this.gender = gender;
		this.wikiUrl = wikiUrl;
		this.wikiImage = wikiImage;
		this.craftingObjects = craftingObjects;
		this.abilities = abilities;
		this.polarities = polarities;
		this.aura = aura;
		this.vaulted = vaulted;
	}
	
	@Override
	public int hashCode()
	{
		return Objects.hash(name, description, color, health, shield, armor, energy, masteryLevel, sprint, gender,
		                    wikiUrl, wikiImage, craftingObjects, abilities, polarities, aura, vaulted);
	}
	
	@Override
	public boolean equals(Object o)
	{
		if (this == o) {
			return true;
		}
		if (!(o instanceof WarframeObject)) {
			return false;
		}
		WarframeObject that = (WarframeObject)o;
		return color == that.color && health == that.health && shield == that.shield && armor == that.armor && energy == that.energy && masteryLevel == that.masteryLevel && Double.compare(
				that.sprint, sprint) == 0 && vaulted == that.vaulted && Objects.equals(name,
		                                                                               that.name) && Objects.equals(
				description, that.description) && Objects.equals(gender, that.gender) && Objects.equals(wikiUrl,
		                                                                                                that.wikiUrl) && Objects.equals(
				wikiImage, that.wikiImage) && Objects.equals(craftingObjects, that.craftingObjects) && Objects.equals(
				abilities, that.abilities) && Objects.equals(polarities, that.polarities) && Objects.equals(aura,
		                                                                                                    that.aura);
	}
	
	
	@Override
	public String toString()
	{
		return "WarframeObject{" + "name='" + name + '\'' + ", description='" + description + '\'' + ", color=" + color + ", health=" + health + ", shield=" + shield + ", armor=" + armor + ", energy=" + energy + ", masteryLevel=" + masteryLevel + ", sprint=" + sprint + ", gender='" + gender + '\'' + ", wikiUrl='" + wikiUrl + '\'' + ", wikiImage='" + wikiImage + '\'' + ", craftingObjects=" + craftingObjects + ", abilities=" + abilities + ", polarities=" + polarities + ", aura='" + aura + '\'' + ", vaulted=" + vaulted + '}';
	}
}
