package Core.CommandSystem;

import Core.Util.Utils;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.internal.utils.PermissionUtil;

import java.util.EnumSet;

public class PermissionsUtils
{
	public static boolean canConnect(User user, VoiceChannel channel)
	{
		return hasPermissions(user, channel.getGuild(), channel, EnumSet.of(Permission.VOICE_CONNECT));
	}
	public static boolean hasPermissions(User user, Guild guild, GuildChannel channel, EnumSet perms)
	{
		return hasPermissions(user, guild, channel, perms, true);
	}
	
	public static boolean hasPermissions(
			 User user,  Guild guild,  GuildChannel channel,
			 EnumSet<Permission> perms, boolean useChannelPerms)
	{
		if (useChannelPerms && guild != null) {
			if (channel == null) {
				return true; //Ignore permissions for private chats
			}
		}
		
		if (user == null) {
			return false; //Invalid parameters check
		}
		
		
		if (perms != null && perms.size() > 0) {
			Member mem = Utils.getMember(guild, user);
			
			if (useChannelPerms) {
				return PermissionUtil.checkPermission((IPermissionContainer)channel, mem, perms.toArray(new Permission[0]));
			} else {
				return PermissionUtil.checkPermission(mem, perms.toArray(new Permission[0]));
			}
		} else {
			return true;
		}
	}
	
	public static boolean hasPermissions(User user, Guild guild, EnumSet perms)
	{
		
		return hasPermissions(user, guild, null, perms, false);
	}
	
	public static boolean hasPermissions(Role role,  EnumSet<Permission> perms)
	{
		if (perms != null && perms.size() > 0) {
			return role.getPermissions().containsAll(perms);
		} else {
			return true;
		}
	}
	
	
	public static boolean botHasPermission(MessageChannel channel, EnumSet<Permission> perms){
		if(channel != null) {
			if(ChatUtils.isPrivate(channel)){
				return true;
			}
			
			if(channel instanceof TextChannel) {
				TextChannel tChannel = (TextChannel)channel;
				
				if (tChannel.getGuild() != null) {
					Member member = tChannel.getGuild().getSelfMember();
					
					if (member != null) {
						PermissionOverride botOverridePermissions = tChannel.getPermissionOverride(member);
						EnumSet<Permission> botPermissions = member.getPermissions();
						
						boolean overrides = botOverridePermissions != null && botOverridePermissions.getAllowed().containsAll(
								perms);
						boolean normal = botPermissions != null && botPermissions.containsAll(perms);
						
						return overrides || normal;
					}
				}
			}
		}
		
		return false;
	}
	
	public static boolean botHasPermission(Guild guild, EnumSet<Permission> perms){
		if(guild != null) {
			Member member = guild.getSelfMember();
			
			if (member != null) {
				EnumSet<Permission> botPermissions = member.getPermissions();
				return botPermissions != null && botPermissions.containsAll(perms);
			}
		}
		
		return false;
	}
}
