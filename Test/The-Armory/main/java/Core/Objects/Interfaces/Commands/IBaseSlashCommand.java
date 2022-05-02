package Core.Objects.Interfaces.Commands;

public interface IBaseSlashCommand
{
	String getSlashName();
	default String getDescription() { return null; }
}
