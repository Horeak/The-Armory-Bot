package Core.Main;

import Core.Commands.Voice.LavaLinkClient;
import Core.Data.DataHandler;
import Core.Objects.Annotation.Method.Startup.Init;
import Core.Objects.Annotation.Method.Startup.PostInit;
import Core.Objects.Annotation.Method.Startup.PreInit;
import Core.Objects.Events.BotCloseEvent;
import Core.Util.CustomReflections;
import Core.Util.FileUtil;
import Core.Util.JsonUtils;
import Core.Util.ReflectionUtils;
import com.fasterxml.jackson.annotation.JsonIgnore;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.ApplicationInfo;
import net.dv8tion.jda.api.utils.ChunkingFilter;
import org.apache.commons.io.FileUtils;
import org.reflections.Reflections;
import org.reflections.scanners.FieldAnnotationsScanner;
import org.reflections.scanners.MethodAnnotationsScanner;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.scanners.TypeAnnotationsScanner;
import org.reflections.util.ConfigurationBuilder;
import org.reflections.util.FilterBuilder;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import javax.management.ReflectionException;
import javax.security.auth.login.LoginException;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SpringBootApplication
public class Startup
{
	public static final Permission[] BOT_PERMISSIONS = new Permission[]{
			Permission.VOICE_SPEAK,
			Permission.VOICE_CONNECT
	};
	
	public static final Long startTime = System.currentTimeMillis();
	private static final SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd HH:mm");
	
	public static String botInviteLink = null;
	public static String serverInviteLink = "https://discord.gg/vC25YcMcwe";
	public static JDA discordClient;
	
	public static String FilePath = "";
	public static File baseFilePath;
	public static URL launchDir = null;
	public static File tempFolder = null;
	private static String filePath;
	
	public static ResourceBundle buildFile;
	public static ResourceBundle versionFile;
	
	public static boolean USE_LAVA_LINK = false;
	
	public static boolean debug = false;
	public static boolean jarFile = false;
	
	public static boolean preInit = false;
	public static boolean init = false;
	public static boolean initListeners = false;
	
	private static Date buildDate;
	private static String version = null;
	private static String commandSign = null;
	
	public static ApplicationInfo appInfo = null;
	
	private static CustomReflections customReflections;
	
	public static final ExecutorService executor = Executors.newCachedThreadPool();
	public static final ScheduledThreadPoolExecutor scheduledExecutor = new ScheduledThreadPoolExecutor(128);
	
	
	public static void main(String[] args)
	{
		SpringApplication.run(Startup.class, args);
		
		scheduledExecutor.setKeepAliveTime(10, TimeUnit.SECONDS);
		scheduledExecutor.allowCoreThreadTimeOut(true);
		
		System.setProperty("java.awt.headless", "true");
		
		debug = System.getProperty("debugMode") != null;
		filePath = System.getProperty("filePath");
		
		System.out.println("debug: " + debug);
		
		try {
			initReflection(true);
			System.out.println("jarFile: " + jarFile);
		} catch (IOException | URISyntaxException e) {
			Logging.exception(e);
		}
		//To make HTTPClient stop spamming the console...
		System.setProperty("org.apache.commons.logging.Log", "org.apache.commons.logging.impl.SimpleLog");
		System.setProperty("org.apache.commons.logging.simplelog.log.httpclient.wire", "ERROR");
		System.setProperty("org.apache.commons.logging.simplelog.log.org.apache.http", "ERROR");
		System.setProperty("org.apache.commons.logging.simplelog.log.org.apache.http.headers", "ERROR");
		
		
		try {
			JsonUtils.gson_init();
			initFile();
			
			System.out.println("Starting bot with launch dir: \""  + baseFilePath + "\"");
			
			initVersion();
			initBot();
			init();
			postInit();
		} catch (Exception e) {
			Logging.exception(e);
		}
		
		if(!debug) {
			if (!baseFilePath.toURI().getPath().contains("root")) {
				System.err.println("The bot started in invalid folder! Info: ");
				System.err.println(" - FilePath: " + FilePath);
				System.err.println(" - jarFile: " + jarFile);
				System.err.println(" - launchDir: " + launchDir);
				System.err.println(" - startTime: " + format.format(startTime));
				
				try {
					Runtime.getRuntime().exec("systemctl restart lavalink");
					Runtime.getRuntime().exec("systemctl restart discordbot");
				} catch (IOException e) {
					Logging.exception(e);
				}
				
				System.exit(0);
			}
		}
	}
	
	public static void initReflection(boolean preinit) throws URISyntaxException, MalformedURLException
	{
		
		File fe = new File(Startup.class.getProtectionDomain().getCodeSource().getLocation().toURI());
		jarFile = fe.exists() && fe.isFile();
		
		URL url = new URL("file:" + (jarFile ? fe.getPath() : System.getProperty("user.dir").replace("\\", "/")));
		Predicate<String> filter = new FilterBuilder().includePackage("Core");
		
		launchDir = url;
		
		if (preinit) {
			preInit(url, filter);
		}
		
		ConfigurationBuilder builder = new ConfigurationBuilder();
		
		builder.setInputsFilter(filter::test);
		builder.setUrls(url);
		builder.forPackages(Startup.class.getPackage().getName());
		builder.setScanners(new FieldAnnotationsScanner(), new TypeAnnotationsScanner(), new MethodAnnotationsScanner(),
		                    new SubTypesScanner());
		
		customReflections = new CustomReflections(builder);
	}
	
	private static void preInit(URL url, Predicate<String> filter)
	{
		try {
			ConfigurationBuilder builder = new ConfigurationBuilder();
			
			builder.filterInputsBy(filter::test);
			builder.setUrls(url);
			builder.forPackages(Startup.class.getPackage().getName());
			builder.setScanners(new MethodAnnotationsScanner());
			
			CustomReflections reflections = new CustomReflections(builder);
			Set<Method> methods = reflections.getMethodsAnnotatedWith(PreInit.class);
			
			System.out.println("Found " + methods.size() + " preInit method" + (methods.size() > 1 ? "s" : "") + "!");
			
			for (Method ob : methods) {
				if(ob == null) continue;
				
				if (!ob.isAccessible()) {
					ob.setAccessible(true);
				}
				
				if (!Modifier.isStatic(ob.getModifiers())) {
					System.err.println("preInit method: " + ob + " is not static!");
					continue;
				}
				
				ob.invoke(null);
			}
			
			System.out.println("preInit done.");
		} catch (Exception e) {
			if (!(e instanceof ReflectionException)) {
				Logging.exception(e);
			}
		}
		
		preInit = true;
	}
	
	private static void initFile() throws IOException
	{
		buildFile = ResourceBundle.getBundle(debug ? "build_debug" : "build",
		                                     new ResourceBundle.Control() {
			                                     @Override
			                                     public List<Locale> getCandidateLocales(String name,
					                                     Locale locale) {
				                                     return Collections.singletonList(Locale.ROOT);
			                                     }
		                                     });
		baseFilePath = new File(System.getProperty("user.dir") + "/");
		
		String folder = buildFile.getString("file_path");
		
		String fe = filePath == null ? baseFilePath.getPath() : filePath;
		
		if (filePath != null) {
			System.out.println("Custom filepath given: " + filePath);
		}
		
		FilePath = FileUtil.getFolder(fe + folder).getCanonicalPath();
		tempFolder = FileUtil.getFolder(Startup.FilePath + "/tmp/");
		
		Logging.activate();
		DataHandler.init(FilePath);
		
		System.out.println("File init done.");
	}
	
	private static void initVersion()
	{
		versionFile = ResourceBundle.getBundle(buildFile.getString("version_file"),
		                                       new ResourceBundle.Control() {
			                                       @Override
			                                       public List<Locale> getCandidateLocales(String name,
					                                       Locale locale) {
				                                       return Collections.singletonList(Locale.ROOT);
			                                       }
		                                       });
		String versionFormat = versionFile.getString("version_format");
		
		String pattern = "(\\$)(.*?)(\\$)";
		Pattern r = Pattern.compile(pattern);
		Matcher m = r.matcher(versionFormat);
		
		while (m.find()) {
			String value = versionFile.getString(m.group(2));
			versionFormat = versionFormat.replace(m.group(), value);
		}
		
		String buildDateSt = versionFile.getString("build_date");
		
		if (buildDateSt != null) {
			try {
				Date dt = format.parse(buildDateSt);
				
				if (dt != null) {
					buildDate = dt;
					
					System.out.println("Found build date: " + dt);
				}
				
			} catch (ParseException e) {
				Logging.exception(e);
			}
		}
		
		version = versionFormat;
		
		System.out.println("Version init done.");
	}
	
	private static void init()
	{
		ReflectionUtils.invokeMethods(Init.class);
		System.out.println("Init done.");
		init = true;
	}
	
	private static void postInit()
	{
		ReflectionUtils.invokeMethods(PostInit.class);
		System.out.println("PostInit done.");
	}
	
	@PostInit
	public static void initAppInfo(){
		appInfo = discordClient.retrieveApplicationInfo().complete();
	}
	
	@JsonIgnore
	private static void initBot()
	{
		String token = buildFile.getString("token");
		
		if (token == null) {
			System.err.println("Invalid bot token!");
			System.exit(0);
		}
		
		JDABuilder builder = JDABuilder.createDefault(token);
		builder.setChunkingFilter(ChunkingFilter.NONE);
		
//		builder.enableIntents(GatewayIntent.GUILD_PRESENCES);
//		builder.enableIntents(GatewayIntent.GUILD_MEMBERS); //TODO It would be preferred to not have to use this but it is required for the status on UserInfo. Maybe drop the status part?
		builder.setAutoReconnect(true);
		
		
		//LavaLink stuff
		if(LavaLinkClient.init) {
			builder.addEventListeners(LavaLinkClient.lavalink);
			builder.setVoiceDispatchInterceptor(LavaLinkClient.lavalink.getVoiceInterceptor());
		}else{
			System.err.println("LavaLink failed Init!");
		}

		try {
			discordClient = builder.build();
		} catch (LoginException e) {
			Logging.exception(e);
		}
		
		commandSign = buildFile.getString("command_sign");
		
		System.out.println("Bot init done.");
	}
	
	public static void onBotClose()
	{
		System.err.println("Shutting down bot!");
		discordClient.getEventManager().handle(new BotCloseEvent(discordClient));
		
		try {
			FileUtils.deleteDirectory(tempFolder);
		} catch (IOException e) {
			Logging.exception(e);
		}
		
		
		discordClient = null;
	}
	public static Reflections getReflection()
	{
		return customReflections;
	}
	
	public static String getVersion()
	{
		return version;
	}
	
	public static Date getBuildDate()
	{
		return buildDate;
	}
	
	public static String getCommandSign()
	{
		return commandSign;
	}
	
}