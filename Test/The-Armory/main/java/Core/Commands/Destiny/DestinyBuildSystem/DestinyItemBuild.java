package Core.Commands.Destiny.DestinyBuildSystem;

import Core.Commands.Destiny.DestinyDataBaseObjects.BaseObjects.SocketObject;
import Core.Commands.Destiny.DestinyDataBaseObjects.Destiny2.Destiny2ItemObject;
import Core.Commands.Destiny.DestinyDataBaseObjects.Destiny2.Destiny2ItemSystem;
import Core.Commands.Destiny.DestinyDataBaseObjects.Destiny2.Destiny2PlugSetObject;
import Core.Commands.Destiny.Objects.MasterworkObject;

import java.util.Date;
import java.util.HashMap;

public class DestinyItemBuild
{
	public String name;
	public Long itemHash;
	public HashMap<Integer, SocketObject> socketHash;
	public Long modSlot;
	public MasterworkObject masterwork;
	public Long armorType; //Resiliance, Recovery, Mobility
	public int armorStat; //Same as above but the selected one
	public Date dateCreated;
	
	public DestinyItemBuild(
			Long itemHash, HashMap<Integer, SocketObject> socketHash, Long modSlot, MasterworkObject masterwork,
			Long armorType, int armorStat, Date dateCreated)
	{
		this.itemHash = itemHash;
		this.socketHash = socketHash;
		this.modSlot = modSlot;
		this.masterwork = masterwork;
		this.armorType = armorType;
		this.armorStat = armorStat;
		this.dateCreated = dateCreated;
	}
	
	public DestinyItemBuild() {}
	
	public DestinyItemBuild(Destiny2ItemObject item)
	{
		this.socketHash = new HashMap<>();
		this.dateCreated = new Date();
		
		int i = 1;
		
		for (Destiny2ItemObject.SocketEntry entry : item.sockets.socketEntries) {
			if (entry.randomizedPlugSetHash != null) {
				Destiny2PlugSetObject setObject = Destiny2ItemSystem.destinyPlugSetObjects.getOrDefault(entry.randomizedPlugSetHash.intValue(), null);
				
				if(setObject.reusablePlugItems.length > 0) {
					if (!this.socketHash.containsKey(i)) {
						this.socketHash.put(i, new SocketObject("Empty Perk slot " + i,
						                                        "This slot does currently not have a selected perk!", i));
						i++;
					}
				}
			}
		}
	}
}
