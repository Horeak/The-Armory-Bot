package Core.CommandSystem.CommandObjects.SlashCommands;

import Core.CommandSystem.ChatUtils;
import Core.CommandSystem.PermissionsUtils;
import Core.Main.Logging;
import Core.Main.Startup;
import Core.Objects.Annotation.Commands.Command;
import Core.Objects.Annotation.Commands.CommandGroup;
import Core.Objects.Annotation.Commands.SlashArgument;
import Core.Objects.Annotation.Commands.SubCommand;
import Core.Objects.Annotation.Method.EventListener;
import Core.Objects.Annotation.Method.Startup.PostInit;
import Core.Objects.Interfaces.Commands.IBaseSlashCommand;
import Core.Objects.Interfaces.Commands.ISlashCommand;
import Core.Util.Utils;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.guild.GuildReadyEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.*;
import net.dv8tion.jda.api.interactions.commands.privileges.CommandPrivilege;
import net.dv8tion.jda.api.requests.restaction.CommandCreateAction;
import net.dv8tion.jda.api.requests.restaction.CommandListUpdateAction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class SlashCommandSystem
{
	public static ConcurrentHashMap<String, ISlashCommand> command_registry = new ConcurrentHashMap<>();
	private static CopyOnWriteArrayList<CommandData> commandState = new CopyOnWriteArrayList<>();
	
	@PostInit
	public static void PostInit(){
		Set<Class<?>> commands = Startup.getReflection().getTypesAnnotatedWith(Command.class);
		Set<Class<?>> subCommands = Startup.getReflection().getTypesAnnotatedWith(SubCommand.class);
		Set<Class<?>> subCommandGroups = Startup.getReflection().getTypesAnnotatedWith(CommandGroup.class);
		
		ArrayList<ISlashCommand> slashCommands = new ArrayList<>();
		ArrayList<ISlashCommand> subSlashCommands = new ArrayList<>();
		ArrayList<IBaseSlashCommand> subSlashCommandGroups = new ArrayList<>();
		
		for(Class<?> shC : commands){
			try{
				ISlashCommand sh = (ISlashCommand)shC.newInstance();
				
				if(sh != null){
					slashCommands.add(sh);
				}
			}catch (Exception e){
				Logging.exception(e);
			}
		}
		
		for(Class<?> shC : subCommands){
			try{
				ISlashCommand sh = (ISlashCommand)shC.newInstance();
				
				if(sh != null){
					subSlashCommands.add(sh);
				}
			}catch (Exception e){
				Logging.exception(e);
			}
		}
		
		for(Class<?> shC : subCommandGroups){
			try{
				IBaseSlashCommand sh = (IBaseSlashCommand)shC.newInstance();
				
				if(sh != null){
					subSlashCommandGroups.add(sh);
				}
			}catch (Exception e){
				Logging.exception(e);
			}
		}
		
		
		System.out.println("Found " + commands.size() + " commands, " + subCommands.size() + " subcommands and " + subCommandGroups.size() + " subcommand groups");
		
		for(ISlashCommand command : slashCommands){
			if(command == null || command.getSlashName() == null) {
				System.err.println("Ignoring command from " + command);
				continue;
			}
 		
			String name = (Startup.debug ? "dev_" : "") + Utils.limitString(command.getSlashName().toLowerCase(), 32);
			String desc = command.getDescription() != null ? Utils.limitString(command.getDescription(), 100) : "Missing description.";
			
			if(name == null || name.isBlank()) continue;
			
			SlashCommandData newCommand = Commands.slash(name, desc);
			
			List<OptionData> argumentList = getOptions(command);
			
			if(argumentList.size() > 0) {
				argumentList.sort((c1, c2) -> Boolean.compare(c2.isRequired(), c1.isRequired()));
				newCommand.addOptions(argumentList);
			}
			
			if(command.commandOptions().length > 0) {
				newCommand.addOptions(command.commandOptions());
			}
			
			for(ISlashCommand subCommand : subSlashCommands){
				SubCommand sd = subCommand.getClass().getDeclaredAnnotation(SubCommand.class);
				
				if(sd != null && sd.parent() == command.getClass()){
					String sub_name = Utils.limitString(subCommand.getSlashName().toLowerCase(), 32);
					String sub_desc = subCommand.getDescription() != null ? Utils.limitString(subCommand.getDescription(), 100) : "Missing description.";
					
					SubcommandData subCommandData = new SubcommandData(sub_name, sub_desc);
					
					List<OptionData> subArgumentList = getOptions(subCommand);
					
					if(subArgumentList.size() > 0) {
						subArgumentList.sort((c1, c2) -> Boolean.compare(c2.isRequired(), c1.isRequired()));
						subCommandData.addOptions(subArgumentList);
					}
					
					if(subCommand.commandOptions().length > 0) {
						try {
							subCommandData.addOptions(subCommand.commandOptions());
						}catch (Exception e){
							System.err.println("Error with sub command: " + subCommand);
							Logging.exception(e);
						}
					}
					
					newCommand.addSubcommands(subCommandData);
					command_registry.put(name + "/" + sub_name, subCommand);
				}
			}
			
			for(IBaseSlashCommand subCommandGroup : subSlashCommandGroups){
				CommandGroup sc = subCommandGroup.getClass().getDeclaredAnnotation(CommandGroup.class);
				
				if(sc != null && sc.parent() == command.getClass()){
					String sub_g_name = Utils.limitString(subCommandGroup.getSlashName().toLowerCase(), 32);
					String sub_g_desc = subCommandGroup.getDescription() != null ? Utils.limitString(subCommandGroup.getDescription(), 100) : "Missing description.";
					
					SubcommandGroupData subCommandGroupData = new SubcommandGroupData(sub_g_name, sub_g_desc);
					
					for(ISlashCommand subCommand : subSlashCommands){
						SubCommand sd = subCommand.getClass().getDeclaredAnnotation(SubCommand.class);
						
						if(sd != null && sd.parent() == subCommandGroup.getClass()){
							String sub_name = Utils.limitString(subCommand.getSlashName().toLowerCase(), 32);
							String sub_desc = subCommand.getDescription() != null ? Utils.limitString(subCommand.getDescription(), 100) : "Missing description.";
							
							SubcommandData subCommandData = new SubcommandData(sub_name, sub_desc);
							
							List<OptionData> subGroupArgumentList = getOptions(subCommand);
							
							if(subGroupArgumentList.size() > 0) {
								subGroupArgumentList.sort((c1, c2) -> Boolean.compare(c2.isRequired(), c1.isRequired()));
								subCommandData.addOptions(subGroupArgumentList);
							}
							
							if(subCommand.commandOptions().length > 0) {
								subCommandData.addOptions(subCommand.commandOptions());
							}
							
							subCommandGroupData.addSubcommands(subCommandData);
							command_registry.put(name + "/" + sub_g_name + "/" + sub_name, subCommand);
						}
					}
					
					newCommand.addSubcommandGroups(subCommandGroupData);
				}
			}
			
			commandState.add(newCommand);
			command_registry.put(name, command);
		}
		
		try {
			if (!Startup.debug) {
				CommandListUpdateAction commandUpdate = Startup.discordClient.updateCommands();
				
				if (commandUpdate != null) {
					commandUpdate.addCommands(commandState);
					commandUpdate.queue();
				}
			} else {
				for (Long l : DEV_GUILDS) {
					Guild guild = Startup.discordClient.getGuildById(l);
					
					if (!initDevGuilds.contains(l)) {
						CommandListUpdateAction commandUpdate = guild.updateCommands();
						
						if (commandUpdate != null) {
							commandUpdate.addCommands(commandState);
							commandUpdate.queue();
							
							initDevGuilds.add(l);
						}
					}
				}
			}
		}catch (Exception e){
			Logging.exception(e);
		}
	}
	
	private static List<OptionData> getOptions(Object c){
		ArrayList<OptionData> datalist = new ArrayList<>();
		for(Field fe : c.getClass().getDeclaredFields()){
			if(fe.isAnnotationPresent(SlashArgument.class)){
				SlashArgument argument = fe.getAnnotation(SlashArgument.class);
				
				OptionType type = getOptionType(fe);
				
				if(type != null) {
					OptionData data = getOptionData(fe, argument, type);
					for(OptionData data1 : datalist){
						if(data1.getName().equals(data.getName())){
							System.err.println("Duplicate option! " + c.getClass() + " - " + fe);
						}
					}
					
					datalist.add(data);
				}
			}
		}
		return datalist;
	}
	
	@NotNull
	private static OptionData getOptionData(Field fe, SlashArgument argument, OptionType type)
	{
		if(argument.key().length() > OptionData.MAX_NAME_LENGTH) System.err.println("Name is too long for " + fe);
		if(argument.text().length() > OptionData.MAX_DESCRIPTION_LENGTH) System.err.println("Text is too long for " + fe);
		
		OptionData data = new OptionData(type, argument.key(), argument.text(), argument.required());
		
		if(type == OptionType.NUMBER){
			if(argument.minValue() != -1){
				data.setMinValue(argument.minValue());
			}
			
			if(argument.maxValue() != -1){
				data.setMinValue(argument.maxValue());
			}
		}else if(type == OptionType.INTEGER) {
			if(argument.minValue() != -1){
				data.setMinValue((long)argument.minValue());
			}
			
			if(argument.maxValue() != -1){
				data.setMinValue((long)argument.maxValue());
			}
		}
		
		if(type == OptionType.CHANNEL){
			if(fe.getType().isAssignableFrom(StageChannel.class)){
				data.getChannelTypes().add(ChannelType.STAGE);
			}
			
			if(fe.getType().isAssignableFrom(VoiceChannel.class)){
				data.getChannelTypes().add(ChannelType.VOICE);
			}
			
			if(fe.getType().isAssignableFrom(PrivateChannel.class)){
				data.getChannelTypes().add(ChannelType.PRIVATE);
			}
			
			if(fe.getType().isAssignableFrom(TextChannel.class)){
				data.getChannelTypes().add(ChannelType.TEXT);
			}
			
			if(fe.getType().isAssignableFrom(Category.class)){
				data.getChannelTypes().add(ChannelType.CATEGORY);
			}
		}
		
		if(type.canSupportChoices()){
			if(argument.choices() != null && argument.choices().length > 0 && !argument.choices().toString().isEmpty()){
				
				int l = 0;
				for(String t : argument.choices()){
					if(!t.isEmpty()) {
						if (type == OptionType.STRING) data.addChoice(t, t);
						else data.addChoice(t, l);
						l++;
					}
				}
			}
		}
		
		
		return data;
	}
	
	@Nullable
	private static OptionType getOptionType(Field fe)
	{
		OptionType type = null;
		
		if(fe.getType().isAssignableFrom(Boolean.class)) type = OptionType.BOOLEAN;
		if(fe.getType().isAssignableFrom(Integer.class) || fe.getType().isAssignableFrom(int.class)) type = OptionType.INTEGER;
		if(fe.getType().isAssignableFrom(Float.class) || fe.getType().isAssignableFrom(Double.class) || fe.getType().isAssignableFrom(Long.class)) type = OptionType.NUMBER;
		if(fe.getType().isAssignableFrom(float.class) || fe.getType().isAssignableFrom(double.class) || fe.getType().isAssignableFrom(long.class)) type = OptionType.NUMBER;
		if(fe.getType().isAssignableFrom(String.class)) type = OptionType.STRING;
		
		if(fe.getType().isAssignableFrom(User.class)) type = OptionType.USER;
		if(fe.getType().isAssignableFrom(TextChannel.class)) type = OptionType.CHANNEL;
		if(fe.getType().isAssignableFrom(MessageChannel.class)) type = OptionType.CHANNEL;
		if(fe.getType().isAssignableFrom(GuildChannel.class)) type = OptionType.CHANNEL;
		if(fe.getType().isAssignableFrom(VoiceChannel.class)) type = OptionType.CHANNEL;
		if(fe.getType().isAssignableFrom(StageChannel.class)) type = OptionType.CHANNEL;
		
		if(fe.getType().isAssignableFrom(Role.class)) type = OptionType.ROLE;
		
		if(type == null){
			System.err.println("Didnt find field type for " + fe);
		}
		
		return type;
	}
	
	public static Long[] DEV_GUILDS = new Long[]{265259832259510282L, 188335208788000769L, 815591909606555709L};
	public static CopyOnWriteArrayList<Long> initDevGuilds = new CopyOnWriteArrayList<>();
	
	@EventListener
	public static void guildLoad(GuildReadyEvent event){
		if(Startup.debug) {
			if(command_registry.size() > 0){
				ArrayList<String> list = new ArrayList<>();
				list.addAll(command_registry.keySet());
				
				for (Long id : DEV_GUILDS) {
					if (id == event.getGuild().getIdLong()) {
						if (!initDevGuilds.contains(id)) {
							
							CommandListUpdateAction commandUpdate = event.getGuild().updateCommands();
							
							if (commandUpdate != null) {
								commandUpdate.addCommands(commandState);
								commandUpdate.queue();
								
								initDevGuilds.add(id);
							}
						}
					}
				}
			}
		}
		
		for(Entry<String, ISlashCommand> ent : command_registry.entrySet()){
			CommandPrivilege[] priv = ent.getValue().commandPrivileges(event.getGuild());
			
			String name = Utils.limitString(ent.getValue().getSlashName().toLowerCase(), 32);
			String desc = ent.getValue().getDescription() != null ? Utils.limitString(ent.getValue().getDescription(), 100) : "Missing description.";
			
			HashMap<String, Collection<? extends CommandPrivilege>> map = new HashMap<>();
			
			if(priv != null && priv.length > 0){
				CommandCreateAction ac = event.getGuild().upsertCommand(name, desc);
				net.dv8tion.jda.api.interactions.commands.Command c = ac.setDefaultEnabled(false).complete();
				map.put(c.getId(), Arrays.asList(priv));
			}
		}
	}
	
	@EventListener
	public static void slashCommand(SlashCommandInteractionEvent event){
		if(event.getChannel() == null){
			//TODO Fix this when thread support is added to JDA
			System.err.println("Null channel on slashcommand, probably used in a thread");
			return;
		}
		
		if(command_registry.containsKey(event.getCommandPath())){
			try {
				ISlashCommand slashCommand = command_registry.get(event.getCommandPath()).getClass().newInstance();
				
				String commandName = event.getCommandPath();
				StringBuilder builder = new StringBuilder();
				
				if (!ChatUtils.isPrivate(event.getChannel())) {
					builder.append("[").append(event.getGuild().getName()).append("/").append(event.getChannel().getName()).append("]");
				}
				
				builder.append("[").append(event.getUser().getAsTag()).append("]");
				builder.append("Slash command received: /" + commandName);
				
				for(OptionMapping mapping : event.getOptions()){
					if(mapping != null){
						builder.append(" " + mapping.getName() + ": \"" + mapping.getAsString() + "\"");
					}
				}
				
				SlashCommandMessage message = new SlashCommandMessage(event);
				
				if(slashCommand.getRequiredPermissions() != null && event.getChannel() != null && event.getGuild() != null){
					boolean permission = PermissionsUtils.hasPermissions(event.getUser(), event.getGuild(), slashCommand.getRequiredPermissions());
					
					if(!permission){
						ChatUtils.sendEmbed(message.getChannel(), "You do not have the required permissions to use this command!");
						return;
					}
				}
				
				for(Field fe : slashCommand.getClass().getDeclaredFields()){
					if(fe.isAnnotationPresent(SlashArgument.class)){
						SlashArgument argument = fe.getAnnotation(SlashArgument.class);
						
						OptionMapping option = event.getOption(argument.key());
						
						if(option != null){
							if(option.getType() == OptionType.STRING){
								fe.set(slashCommand, option.getAsString());
								continue;
							}
							
							if(option.getType() == OptionType.BOOLEAN){
								fe.set(slashCommand, option.getAsBoolean());
								continue;
							}
							
							if(option.getType() == OptionType.INTEGER || option.getType() == OptionType.NUMBER){
								Long l = option.getAsLong();
								
								if(fe.getType().isAssignableFrom(Integer.class) || fe.getType().isAssignableFrom(int.class)){
									fe.set(slashCommand, l.intValue());
									continue;
									
								}else if(fe.getType().isAssignableFrom(Float.class) || fe.getType().isAssignableFrom(float.class)){
									fe.set(slashCommand, l.floatValue());
									continue;
									
								}else if(fe.getType().isAssignableFrom(Double.class) || fe.getType().isAssignableFrom(double.class)){
									fe.set(slashCommand, l.doubleValue());
									continue;
									
								}else if(fe.getType().isAssignableFrom(Long.class) || fe.getType().isAssignableFrom(long.class)){
									fe.set(slashCommand, l);
									continue;
								}
							}
							
							if(option.getType() == OptionType.USER){
								fe.set(slashCommand, option.getAsUser());
								continue;
							}
							
							if(option.getType() == OptionType.CHANNEL){
								if(fe.getType().isAssignableFrom(Category.class)){
									fe.set(slashCommand, (Category)option.getAsGuildChannel());
									
								}else if(fe.getType().isAssignableFrom(VoiceChannel.class)){
									fe.set(slashCommand, (VoiceChannel)option.getAsGuildChannel());
									
								}else if(fe.getType().isAssignableFrom(StageChannel.class)) {
									fe.set(slashCommand, (StageChannel)option.getAsGuildChannel());
									
								}else if(fe.getType().isAssignableFrom(GuildChannel.class)){
									fe.set(slashCommand, option.getAsGuildChannel());
									
								}else if(fe.getType().isAssignableFrom(MessageChannel.class)){
									fe.set(slashCommand, option.getAsMessageChannel());
									
								}
								
								continue;
							}
							
							
							System.err.println("Invalid field type! " + fe.getType() + " | " + option.getType());
						}
					}
				}
				
				System.out.println(builder.toString());
				slashCommand.slashCommandExecuted(event, event.getGuild(), event.getUser(), (SlashCommandChannel)message.getChannel(), message);
				
			} catch (InstantiationException | IllegalAccessException e) {
				Logging.exception(e);
			}
		}
	}
}