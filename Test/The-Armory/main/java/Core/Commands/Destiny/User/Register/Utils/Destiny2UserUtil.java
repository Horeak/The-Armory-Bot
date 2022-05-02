package Core.Commands.Destiny.User.Register.Utils;

import Core.CommandSystem.ChatUtils;
import Core.Commands.Destiny.User.Objects.AccessToken;
import Core.Commands.Destiny.User.Register.Objects.AccountTypes;
import Core.Commands.Destiny.User.Register.Objects.MemberShipObject;
import Core.Commands.Destiny.User.Register.Objects.UserAccount;
import Core.Main.Logging;
import Core.Objects.Annotation.Fields.DataObject;
import Core.Objects.Annotation.Method.Interval;
import Core.Objects.BotChannel;
import net.dv8tion.jda.api.entities.User;
import org.jetbrains.annotations.Nullable;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class Destiny2UserUtil
{
	@DataObject( file_path = "destiny/d2_user_ids.json", name = "accessTokens")
	public static ConcurrentHashMap<Long, AccessToken> accessTokenStore = new ConcurrentHashMap<>();
	
	@Interval(time_interval = 2, initial_delay = 1, time_unit = TimeUnit.DAYS)
	public static void refreshTokens(){
		for(AccessToken token : accessTokenStore.values()){
			if(!token.refreshExpired() && (token.isExpired() || (token.refreshExpiry - System.currentTimeMillis() <= TimeUnit.MILLISECONDS.convert(3, TimeUnit.DAYS)))){
				DestinyTokenUtils.refreshToken(token);
			}
		}
	}
	
	public static final String BASE_BUNGIE_URL = "https://www.bungie.net";
	public static final String GET_MEMBERSHIP_PATH = "/Platform/User/GetMembershipsForCurrentUser/";
	
	public static String getUserName(AccessToken accessToken)
	{
		JSONObject object = DestinyRegisterHTTPHandler.getUserObject(accessToken);
		
		if (object != null) {
			if (object.has("displayName")) {
				return object.getString("displayName");
			}
		}
		
		return null;
	}
	
	
	public static ArrayList<UserAccount> getUserAccounts(AccessToken accessToken)
	{
		JSONObject object = DestinyRegisterHTTPHandler.getUserObject(accessToken);
		ArrayList<UserAccount> list = new ArrayList<>();
		
		if (object.has("blizzardDisplayName")) {
			list.add(new UserAccount(AccountTypes.BATTLENET, object.getString("blizzardDisplayName")));
		}
		
		if (object.has("steamDisplayName")) {
			list.add(new UserAccount(AccountTypes.STEAM, object.getString("steamDisplayName")));
		}
		
		if (object.has("psnDisplayName")) {
			list.add(new UserAccount(AccountTypes.PSN, object.getString("psnDisplayName")));
		}
		
		if (object.has("xboxDisplayName")) {
			list.add(new UserAccount(AccountTypes.XBOX, object.getString("xboxDisplayName")));
		}
		
		if (object.has("stadiaDisplayName")) {
			list.add(new UserAccount(AccountTypes.STADIA, object.getString("stadiaDisplayName")));
		}
		
		return list;
	}
	
	
	public static String getUserTag(AccessToken accessToken)
	{
		JSONObject object = DestinyRegisterHTTPHandler.getUserObject(accessToken);
		HashMap<String, String> userTags = new HashMap<>();
		
		if (object != null) {
			if (object.has("displayName")) {
				return object.getString("displayName");
			}
		}
		
		return null;
	}
	
	public static String getFirstMembership(AccessToken token)
	{
		String[] list = getMembershipIds(token);
		
		if (list.length > 0) {
			return list[0];
		}
		
		return null;
	}
	
	public static String[] getMembershipIds(AccessToken token)
	{
		HttpURLConnection con = null;
		ArrayList<String> list = new ArrayList<>();
		
		try {
			URL obj = new URL("https://www.bungie.net/Platform/User/GetMembershipsForCurrentUser/");
			con = (HttpURLConnection)obj.openConnection();
			
			con.setRequestMethod("GET");
			con.setRequestProperty("X-API-Key", DestinyRegisterHTTPHandler.API_KEY);
			con.setRequestProperty("Authorization", token.getAuthorization());
			
			JSONObject object = DestinyRegisterHTTPHandler.getJsonObject(con.getInputStream());
			
			if (object != null) {
				if (object.has("Response")) {
					JSONObject temp = object.getJSONObject("Response");
					
					if (temp.has("destinyMemberships")) {
						JSONArray array = temp.getJSONArray("destinyMemberships");
						
						for (Object ob : array) {
							if (ob instanceof JSONObject) {
								JSONObject userInfoCard = (JSONObject)ob;
								
								if (userInfoCard.has("membershipId")) {
									list.add(userInfoCard.getString("membershipId"));
								}
							}
						}
					}
				}
			}
			
		} catch (Exception e) {
			DestinyRegisterHTTPHandler.handleException(e, con);
		}
		
		return list.toArray(new String[0]);
	}
	
	
	//TODO This has cleared existing accounts?
	
	public static JSONObject postData( AccessToken accessToken, String path, JSONObject data)
	{
		if (accessToken == null) return null;
		
		HttpURLConnection con = null;
		try {
			URL obj = new URL(BASE_BUNGIE_URL + path);
			con = (HttpURLConnection)obj.openConnection();
			con.setRequestMethod("POST");
			
			con.setRequestProperty("Authorization", accessToken.tokenType + " " + accessToken.token);
			con.setRequestProperty("X-API-Key", DestinyRegisterHTTPHandler.API_KEY);
			
			con.setRequestProperty("Content-Type", "application/json; utf-8");
			con.setRequestProperty("Accept", "application/json");
			
			con.setDoOutput(true);
			
			try(OutputStream os = con.getOutputStream()) {
				byte[] input = data.toString().getBytes("utf-8");
				os.write(input, 0, input.length);
			}
			
			JSONObject t = DestinyRegisterHTTPHandler.getJsonObject(con.getInputStream());
			
			if (t instanceof JSONObject) {
				if (t != null) {
					if (t.has("ErrorCode")) {
						int errorCode = t.getInt("ErrorCode");
						
						if (errorCode != 1) {
							System.err.println("Error: " + t.getString("ErrorStatus"));
							System.out.println(t);
							return null;
						}
					}
					
					return t;
				}
			}
			
		} catch (IOException e) {
			return DestinyRegisterHTTPHandler.handleException(e, con);
		}
		
		return null;
	}
	
	public static JSONObject getData( AccessToken accessToken, String path)
	{
		if (accessToken == null) return null;
		
		HttpURLConnection con = null;
		try {
			URL obj = new URL(BASE_BUNGIE_URL + path);
			con = (HttpURLConnection)obj.openConnection();
			
			con.setRequestMethod("GET");
			con.setRequestProperty("Authorization", accessToken.tokenType + " " + accessToken.token);
			con.setRequestProperty("X-API-Key", DestinyRegisterHTTPHandler.API_KEY);
			
			JSONObject t = DestinyRegisterHTTPHandler.getJsonObject(con.getInputStream());
			
			if (t instanceof JSONObject) {
				if (t != null) {
					if (t.has("ErrorCode")) {
						int errorCode = t.getInt("ErrorCode");
						
						if (errorCode != 1) {
							System.err.println("Error: " + t.getString("ErrorStatus"));
							System.out.println(t);
							return null;
						}
					}
					
					if (t.has("Response")) {
						return t.getJSONObject("Response");
					}
				}
			}
			
		} catch (IOException e) {
			return DestinyRegisterHTTPHandler.handleException(e, con);
		}
		
		return null;
	}
	
	public static AccessToken getOrCreateAccessToken(User user, String auth)
	{
		AccessToken token = getAccessToken(user);
		
		if (token != null && !token.isExpired()) {
			return token;
		}
		
		try {
			JSONObject object = DestinyRegisterHTTPHandler.getUserAuthenticateObject(auth);
			
			if (object != null) {
				AccessToken token1 = new AccessToken();
				
				token1.token = object.getString("access_token");
				token1.refreshToken = object.getString("refresh_token");
				
				token1.expiryDate = System.currentTimeMillis() + TimeUnit.MILLISECONDS.convert(object.getInt("expires_in"), TimeUnit.SECONDS);
				token1.refreshExpiry = System.currentTimeMillis() + TimeUnit.MILLISECONDS.convert(object.getInt("refresh_expires_in"), TimeUnit.SECONDS);
				
				token1.membershipId = object.getString("membership_id");
				token1.tokenType = object.getString("token_type");
				
				if (token1.token != null && !token1.isExpired()) {
					accessTokenStore.put(user.getIdLong(), token1);
					return token1;
				} else {
					System.err.println(object);
				}
			}
			
		} catch (IOException e) {
			Logging.exception(e);
		}
		
		return null;
	}
	
	public static AccessToken getAccessToken(User user)
	{
		return getAccessToken(user.getIdLong());
	}
	
	public static AccessToken getAccessToken(Long user)
	{
		AccessToken token = accessTokenStore.getOrDefault(user, null);
		
		if (token != null && token.isExpired()) {
			if (!token.refreshExpired()) {
				Long expiry = token.expiryDate;
				DestinyTokenUtils.refreshToken(token);
				
				if (expiry.equals(token.expiryDate)) {
					System.err.println("Token didnt refresh!");
				}
			} else {
				accessTokenStore.remove(user);
			}
		}
		
		return token;
	}
	
	@Nullable
	public static MemberShipObject getMemberShipObject(BotChannel channel, User author)
	{
		JSONObject object = getData(getAccessToken(author), GET_MEMBERSHIP_PATH);
		
		if (object == null) {
			ChatUtils.sendEmbed(channel,
			                    author.getAsMention() + " Found no account registered to you. Please register before trying to use this command.");
			return null;
		}
		
		MemberShipObject memberShipObject = new MemberShipObject(object).invoke();
		
		if(memberShipObject == null || !memberShipObject.isValid()){
			if(!memberShipObject.isValid()){
				ChatUtils.sendEmbed(channel,
				                    author.getAsMention() + " Your user login has expired, please register again.");
			}else{
				ChatUtils.sendEmbed(channel, author.getAsMention() + " Found no account registered to you. Please register before trying to use this command.");
			}
			
	return null;
		}
		return memberShipObject;
	}
}
