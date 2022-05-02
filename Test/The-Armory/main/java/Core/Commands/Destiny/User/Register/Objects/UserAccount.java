package Core.Commands.Destiny.User.Register.Objects;

public class UserAccount
{
	public AccountTypes accountType;
	public String accountName;
	
	public UserAccount(AccountTypes accountType, String accountName)
	{
		this.accountType = accountType;
		this.accountName = accountName;
	}
}
