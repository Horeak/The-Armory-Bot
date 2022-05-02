package Core.Commands.Warframe.Objects;

import Core.Util.Time.TimeParserUtil;
import net.dv8tion.jda.api.EmbedBuilder;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public abstract class InfoObject
{
	public String id;
	public String platform;
	public String nodeName;
	public String planet;
	public String missionType;
	public String factionName;
	
	public String expiresInText;
	public ArrayList<String> keys = new ArrayList<>();
	public String startTime;
	public String endTime;
	public Long expiresIn;
	
	public InfoObject(){ }
	
	public InfoObject(String platform, String id, String node, String planet, String type, String faction, String eta)
	{
		this.platform = platform;
		this.id = id;
		this.missionType = type;
		this.factionName = faction;
		this.expiresInText = eta;
		
		this.nodeName = node;
		this.planet = planet;
		
		Long time = TimeParserUtil.getTime(eta.split(" "));
		
		if (time > 0) {
			expiresIn = time;
		}
	}
	
	public abstract String getName();
	public abstract String getType();
	
	public abstract InfoObject loadObject(JSONObject object);
	
	public abstract EmbedBuilder genEmbed();
	public abstract String getInfo();
	
	public void sort(List<InfoObject> objects)
	{
		if(objects != null && objects.size() > 0) {
			objects.sort((o1, o2) -> {
				if(o1 != null && o2 != null){
					if(o1.expiresIn != null && o2.expiresIn != null){
						return Long.compare(o1.expiresIn, o2.expiresIn);
					}
				}
				
				return 0;
			});
		}
	}
	
	public void done()
	{
		if (keys != null && keys.size() > 0) {
			keys.replaceAll((e) -> {
				if (e != null) {
					return e.replace(" ", ".").toLowerCase();
				}
				
				return e;
			});
		}
	}
}