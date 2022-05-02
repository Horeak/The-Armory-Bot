package Core.Commands.Destiny.DestinyDataBaseObjects.BaseObjects;

import java.util.Objects;

public class SocketObject
{
	public String name;
	public String description;
	public int socketGroup;
	public Long hash;
	
	public SocketObject(String name, String description, int socketGroup, Long hash)
	{
		this.name = name;
		this.description = description;
		this.socketGroup = socketGroup;
		this.hash = hash;
	}
	
	public SocketObject(String name, String description, int socketGroup)
	{
		this.name = name;
		this.description = description;
		this.socketGroup = socketGroup;
	}
	
	@Override
	public int hashCode()
	{
		return Objects.hash(name, description, socketGroup, hash);
	}
	
	@Override
	public boolean equals(Object o)
	{
		if (this == o) {
			return true;
		}
		if (!(o instanceof SocketObject)) {
			return false;
		}
		SocketObject that = (SocketObject)o;
		return socketGroup == that.socketGroup && Objects.equals(name, that.name) && Objects.equals(description,
		                                                                                            that.description) && Objects.equals(
				hash, that.hash);
	}
	
	
	@Override
	public String toString()
	{
		return "SocketObject{" + "name='" + name + '\'' + ", description='" + description + '\'' + ", socketGroup=" + socketGroup + ", hash=" + hash + '}';
	}
}
