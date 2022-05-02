package Core.Commands.Warframe.Objects;

import java.util.ArrayList;
import java.util.Objects;

public class WeaponObject
{
	public String name;
	public String description;
	public String trigger;
	public String noise;
	public String type;
	public String category;
	public String projectile;
	public String damage;
	public String wikiUrl;
	public String wikiImage;
	public ArrayList<CraftingObject> craftingObjects;
	public ArrayList<DamageObject> damageObjects;
	public ArrayList<DamageObject> damages;
	public ArrayList<String> polarities;
	public String aura;
	public boolean vaulted;
	public int rivenDisposition;
	public int masteryLevel;
	public int magazineSize;
	public int accuracy;
	public int ammo;
	public double reloadTime;
	public double totalDamage;
	public double criticalChance;
	public double criticalMultiplier;
	public double statusChance;
	public double fireRate;
	
	public WeaponObject(
			String name, String description, String trigger, String noise, String type, String category,
			String projectile, String damage, String wikiUrl, String wikiImage,
			ArrayList<CraftingObject> craftingObjects, ArrayList<DamageObject> damageObjects,
			ArrayList<DamageObject> damages, int masteryLevel, int magazineSize, int accuracy, int ammo,
			double reloadTime, double totalDamage, double criticalChane, double criticalMultiplier, double statusChance,
			double fireRate, ArrayList<String> polarities, String aura, boolean vaulted, int rivenDisposition)
	{
		this.name = name;
		this.description = description;
		this.trigger = trigger;
		this.noise = noise;
		this.type = type;
		this.category = category;
		this.projectile = projectile;
		this.damage = damage;
		this.wikiUrl = wikiUrl;
		this.wikiImage = wikiImage;
		this.craftingObjects = craftingObjects;
		this.damageObjects = damageObjects;
		this.damages = damages;
		this.masteryLevel = masteryLevel;
		this.magazineSize = magazineSize;
		this.accuracy = accuracy;
		this.ammo = ammo;
		this.reloadTime = reloadTime;
		this.totalDamage = totalDamage;
		this.criticalChance = criticalChane;
		this.criticalMultiplier = criticalMultiplier;
		this.statusChance = statusChance;
		this.fireRate = fireRate;
		this.polarities = polarities;
		this.aura = aura;
		this.vaulted = vaulted;
		this.rivenDisposition = rivenDisposition;
	}
	
	@Override
	public int hashCode()
	{
		return Objects.hash(name, description, trigger, noise, type, category, projectile, damage, wikiUrl, wikiImage,
		                    craftingObjects, damageObjects, damages, polarities, aura, vaulted, rivenDisposition,
		                    masteryLevel, magazineSize, accuracy, ammo, reloadTime, totalDamage, criticalChance,
		                    criticalMultiplier, statusChance, fireRate);
	}
	
	@Override
	public boolean equals(Object o)
	{
		if (this == o) {
			return true;
		}
		if (!(o instanceof WeaponObject)) {
			return false;
		}
		WeaponObject that = (WeaponObject)o;
		return vaulted == that.vaulted && rivenDisposition == that.rivenDisposition && masteryLevel == that.masteryLevel && magazineSize == that.magazineSize && accuracy == that.accuracy && ammo == that.ammo && Double.compare(
				that.reloadTime, reloadTime) == 0 && Double.compare(that.totalDamage,
		                                                            totalDamage) == 0 && Double.compare(
				that.criticalChance, criticalChance) == 0 && Double.compare(that.criticalMultiplier,
		                                                                    criticalMultiplier) == 0 && Double.compare(
				that.statusChance, statusChance) == 0 && Double.compare(that.fireRate, fireRate) == 0 && Objects.equals(
				name, that.name) && Objects.equals(description, that.description) && Objects.equals(trigger,
		                                                                                            that.trigger) && Objects.equals(
				noise, that.noise) && Objects.equals(type, that.type) && Objects.equals(category,
		                                                                                that.category) && Objects.equals(
				projectile, that.projectile) && Objects.equals(damage, that.damage) && Objects.equals(wikiUrl,
		                                                                                              that.wikiUrl) && Objects.equals(
				wikiImage, that.wikiImage) && Objects.equals(craftingObjects, that.craftingObjects) && Objects.equals(
				damageObjects, that.damageObjects) && Objects.equals(damages, that.damages) && Objects.equals(
				polarities, that.polarities) && Objects.equals(aura, that.aura);
	}
	
	
	@Override
	public String toString()
	{
		return "WeaponObject{" + "name='" + name + '\'' + ", description='" + description + '\'' + ", trigger='" + trigger + '\'' + ", noise='" + noise + '\'' + ", type='" + type + '\'' + ", category='" + category + '\'' + ", projectile='" + projectile + '\'' + ", damage='" + damage + '\'' + ", wikiUrl='" + wikiUrl + '\'' + ", wikiImage='" + wikiImage + '\'' + ", craftingObjects=" + craftingObjects + ", damageObjects=" + damageObjects + ", damages=" + damages + ", polarities=" + polarities + ", aura='" + aura + '\'' + ", vaulted=" + vaulted + ", rivenDisposition=" + rivenDisposition + ", masteryLevel=" + masteryLevel + ", magazineSize=" + magazineSize + ", accuracy=" + accuracy + ", ammo=" + ammo + ", reloadTime=" + reloadTime + ", totalDamage=" + totalDamage + ", criticalChance=" + criticalChance + ", criticalMultiplier=" + criticalMultiplier + ", statusChance=" + statusChance + ", fireRate=" + fireRate + '}';
	}
}
