package Core.Main;

import Core.Util.FileUtil;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;

import java.io.File;
import java.io.PrintStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.logging.*;

public class Logging
{
	public static final boolean ERROR_LOGS = true;
	public static final boolean timeStamps = true;
	public static final DateFormat df = new SimpleDateFormat("dd/LLL/yyyy - HH:mm (zzz)", Locale.US);
	public static final DateFormat formatter = new SimpleDateFormat("dd-MM-yyyy", Locale.US);
	public static final DateFormat formatterTime = new SimpleDateFormat("dd/MM/yyyy-HH:mm:ss(zzz)", Locale.US);
	public static Logger out;
	
	public static void activate()
	{
		out = Logger.getLogger("TheArmory_Logger");
		out.setUseParentHandlers(false);
		out.setLevel(Level.ALL);
		
		CustomFormatter formatter = new CustomFormatter();
		CustomConsoleHandler handler = new CustomConsoleHandler();
		CustomPrintStream stream = new CustomPrintStream(out);
		CustomErrorPrintstream stream1 = new CustomErrorPrintstream(out);
		
		handler.setFormatter(formatter);
		out.addHandler(handler);
		
		stream.attachOut();
		stream1.attachOut();
	}
	
	public static void log(String text)
	{
		logInfo(text, "logs");
	}
	
	public static void logInfo(String text, String folder)
	{
		if (Startup.FilePath == null) {
			return;
		}
		
		if (text == null || text.isEmpty() || text.equalsIgnoreCase("\n")) {
			return;
		}
		String date = formatter.format(new Date());
		String fileName = date + ".log";
		String filePath = Startup.FilePath + "/" + folder + "/" + fileName;
		
		File fe = new File(filePath);
		
		if (fe != null) {
			if (!fe.exists()) {
				fe = FileUtil.getFile(filePath);
				text = "## New log file at " + df.format(new Date()) + "\n" + text;
			}
			
			FileUtil.addLineToFile(fe, text);
		}
	}
	
	public static void error(String text)
	{
		logInfo(text, "errorLogs");
	}
	
	public static void exception(Throwable e)
	{
		//TODO Make a better way to handle this!
		if (e instanceof ErrorResponseException) {
			ErrorResponseException ex = (ErrorResponseException)e;
			
			if (ex != null && ex.getErrorCode() == 10008) {
				System.err.println("Unknown Message error! Fix this!");
				return;
			}
		}
		
		Date today = new Date();
		
		String preFix = Startup.debug ? getPrefix(Level.SEVERE, System.currentTimeMillis()) : "";
		StringBuilder builder = new StringBuilder();
		
		builder.append(e + "\n");
		for (StackTraceElement g : e.getStackTrace()) {
			builder.append(preFix).append("\t at ").append(g.toString()).append("\n");
		}
		
		System.err.println(builder);
		
		if (Logging.ERROR_LOGS && Startup.FilePath != null) {
			String timePrefix = "[" + formatterTime.format(today) + "] ";
			String botVersionPrefix = "[Discord Version: " + Startup.getVersion() + "] ";
			String prefix = timePrefix + botVersionPrefix;
			
			File file = FileUtil.getFile(Startup.FilePath + "/errorLogs/" + formatter.format(today) + ".log");
			FileUtil.addLineToFile(file, prefix + e.getClass().getName() + ": " + e.getMessage());
			for (StackTraceElement g : e.getStackTrace()) FileUtil.addLineToFile(file, prefix + "\t at " + g);
			FileUtil.addLineToFile(file, "");
		}
	}
	
	protected static String getPrefix(Level level, Long time)
	{
		StringBuilder preFix = new StringBuilder();
		
		if (timeStamps) {
			preFix.append("[").append(Logging.df.format(new Date(time))).append("]").append(" - ");
		}
		
		if (Startup.debug) {
			preFix.append("[").append(level).append("] - ");
			preFix.append("[").append(Thread.currentThread().getName()).append(":").append(Thread.currentThread().getId()).append("] - ");
			
			
			StackTraceElement[] stacks = Thread.currentThread().getStackTrace();
			for (StackTraceElement stack : stacks) {
				
				if (stack != null) {
					String className = stack.getClassName();
					
					if (stack.getClassName() != null) {
						if (stack.getClassName().startsWith(Logging.class.getPackage().getName()) || stack.isNativeMethod() || className.startsWith("java") || stack.getClassName().startsWith("org.slf4j") || stack.getFileName() != null && stack.getFileName().toLowerCase().contains(
								"slf4jlogger") || stack.getClassName().startsWith("org.eclipse") || stack.getMethodName().equalsIgnoreCase("handleException")) {
							continue;
						}
					}
				}
				
				
				preFix.append("[").append(stack.getFileName()).append("][").append(stack.getMethodName()).append(":").append(stack.getLineNumber()).append("] - ");
				break;
			}
		}
		
		return preFix.toString();
	}
	
	
	public static class CustomFormatter extends Formatter
	{
		
		public String format(LogRecord record)
		{
			String prefix = Logging.getPrefix(record.getLevel(), record.getMillis());
			
			if (record.getMessage() == null || record.getMessage().isEmpty() || record.getMessage().equalsIgnoreCase("\n")) {
				return "";
			}
			
			StringBuilder builder = new StringBuilder();
			
			builder.append(prefix);
			builder.append(formatMessage(record));
			
			if (!record.getMessage().isEmpty()) {
				if (record.getMessage() == null || !record.getMessage().contains("\n")) {
					builder.append("\n");
				}
			}
			
			return builder.toString();
		}
	}
	
	public static class CustomConsoleHandler extends ConsoleHandler
	{
		@Override
		public void publish(LogRecord record)
		{
			try {
				String message = getFormatter().format(record);
				String logMessage = message;
				if (logMessage.endsWith("\n")) {
					logMessage = logMessage.substring(0, logMessage.length() - 1);
				}
				
				Logging.log(logMessage);
				
				if (record.getLevel() == Level.SEVERE || record.getLevel() == Level.WARNING) {
					System.err.write(message.getBytes());
				} else {
					System.out.write(message.getBytes());
				}
				
			} catch (Exception exception) {
				reportError(null, exception, ErrorManager.FORMAT_FAILURE);
			}
		}
	}
	
	public static class CustomPrintStream extends PrintStream
	{
		private final Logger log;
		
		CustomPrintStream(Logger log)
		{
			
			super(System.out, true);
			this.log = log;
		}
		
		void attachOut()
		{
			System.setOut(this);
		}
		
		@Override
		public void print(String s)
		{
			log.log(Level.INFO, s);
		}
		
		@Override
		public void println(String s)
		{
			print(s);
		}
	}
	
	public static class CustomErrorPrintstream extends PrintStream
	{
		private final Logger log;
		
		CustomErrorPrintstream(Logger log)
		{
			super(System.err, true);
			this.log = log;
		}
		
		void attachOut()
		{
			System.setErr(this);
		}
		
		@Override
		public void print(String s)
		{
			log.log(Level.SEVERE, s);
		}
		
		@Override
		public void println(String s)
		{
			print(s);
		}
	}
	
}

