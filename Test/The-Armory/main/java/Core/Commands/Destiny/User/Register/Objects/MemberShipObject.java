package Core.Commands.Destiny.User.Register.Objects;

import org.json.JSONArray;
import org.json.JSONObject;

public class MemberShipObject
{
	private final JSONObject object;
	private int memberType;
	private String id;
	private String displayName;
	
	public MemberShipObject(JSONObject object) {this.object = object;}
	
	public int getMemberType()
	{
		return memberType;
	}
	
	public String getId()
	{
		return id;
	}
	
	public String getDisplayName()
	{
		return displayName;
	}
	
	public JSONObject getObject()
	{
		return object;
	}
	
	public MemberShipObject invoke()
	{
		memberType = -1;
		id = null;
		displayName = null;
		
		if (object.has("primaryMembershipId")) {
			id = object.getString("primaryMembershipId");
			
			if (object.has("destinyMemberships")) {
				JSONArray array = object.getJSONArray("destinyMemberships");
				
				for (Object _ob : array) {
					if (_ob instanceof JSONObject) {
						JSONObject object1 = (JSONObject)_ob;
						
						if(object1.has("displayName")) {
							displayName = object1.getString("displayName");
						}
						
						if (object1.has("crossSaveOverride") && object1.getInt("crossSaveOverride") != 0) {
							if (object1.getInt("crossSaveOverride") == object1.getInt("membershipType")) {
								memberType = object1.getInt("crossSaveOverride");
								break;
							}
						}
						
						String membershipId = object1.getString("membershipId");
						int membershipType = object1.getInt("membershipType");
						
						if (membershipId.equalsIgnoreCase(id)) {
							memberType = membershipType;
							break;
						}
					}
				}
			}
		}else{
			//TODO This just picks the first membership, maybe tweak that?
			if (object.has("destinyMemberships")) {
				JSONArray array = object.getJSONArray("destinyMemberships");
				
				for (Object _ob : array) {
					if (_ob instanceof JSONObject) {
						JSONObject object1 = (JSONObject)_ob;
						
						if(object1.has("displayName")) {
							displayName = object1.getString("displayName");
						}
						
						id = object1.getString("membershipId");
						memberType = object1.getInt("membershipType");
						break;
					}
				}
			}
		}
		return this;
	}
	
	public boolean isValid(){
		return id != null && memberType != -1;
	}
}
