package Core.Commands.Destiny.User.Objects;

public class AccessToken
{
	public String token;
	public String refreshToken;
	public String membershipId;
	public String tokenType;
	public Long expiryDate;
	public Long refreshExpiry;
	
	public boolean isExpired()
	{
		return System.currentTimeMillis() >= expiryDate;
	}
	public boolean refreshExpired()
	{
		return System.currentTimeMillis() >= refreshExpiry;
	}
	public String getAuthorization()
	{
		return tokenType + " " + token;
	}
}
