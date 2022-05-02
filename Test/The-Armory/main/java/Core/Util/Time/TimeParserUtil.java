package Core.Util.Time;

import Core.Objects.Annotation.Method.Startup.PreInit;
import Core.Util.Utils;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.*;
import org.jsoup.internal.StringUtil;
import org.ocpsoft.prettytime.PrettyTime;
import org.ocpsoft.prettytime.nlp.PrettyTimeParser;
import org.ocpsoft.prettytime.nlp.parse.DateGroup;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.StringJoiner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TimeParserUtil
{
	public static HashMap<String[], TimeRunnable> timeRuns = new HashMap<>();
	public static PrettyTime timeFormat = new PrettyTime();
	
	@PreInit
	public static void preInit(){
		timeRuns.put(new String[]{"second", "sec", "s"}, (num, delay) -> delay += Seconds.seconds(num).toStandardDuration().getMillis());
		timeRuns.put(new String[]{"minute", "minutes", "min", "mins", "m"}, (num, delay) -> delay += Minutes.minutes(num).toStandardDuration().getMillis());
		timeRuns.put(new String[]{"hour", "hours", "h"}, (num, delay) -> delay += Hours.hours(num).toStandardDuration().getMillis());
		timeRuns.put(new String[]{"day", "days", "d"}, (num, delay) -> delay += Days.days(num).toStandardDuration().getMillis());
		timeRuns.put(new String[]{"week", "weeks", "w"}, (num, delay) -> delay += Weeks.weeks(num).toStandardDuration().getMillis());
		
		timeRuns.put(new String[]{"month", "months"}, (num, delay) -> {
			DateTime start = new DateTime();
			DateTime end = start.plus(Months.months(num));
			long millis = end.getMillis() - start.getMillis();
			return delay + millis;
		});
		
		timeRuns.put(new String[]{"year", "years"}, (num, delay) -> {
			DateTime start = new DateTime();
			DateTime end = start.plus(Years.years(num));
			long millis = end.getMillis() - start.getMillis();
			return delay + millis;
		});
	}
	
	public static long getTime(String[] input)
	{
		return getTime(StringUtils.join(input, " "));
	}
	
	public static long getTime(String input)
	{
		try {
			List<DateGroup> parse = new PrettyTimeParser().parseSyntax(input);
			
			if (!input.isEmpty() && !parse.isEmpty()) {
				Date date = parse.get(0).getDates().get(0);
				
				if(date != null){
					return date.getTime() - System.currentTimeMillis();
				}
			}
		}catch (Exception ignored){}
		
		long delay = 0;
		
		for(Entry<String[], TimeRunnable> ent : timeRuns.entrySet()){
			String nameMatcher = StringUtil.join(ent.getKey(), "|");
			String matcher = "(\\d+)[ ]?(" + nameMatcher + ")";
			
			Pattern p = Pattern.compile(matcher, Pattern.CASE_INSENSITIVE);
			Matcher m1 = p.matcher(input);
			
			if(m1.find()){
				String num = m1.group(1);
				
				if(Utils.isInteger(num)){
					delay = ent.getValue().run(Integer.parseInt(num), delay);
				}
				
				input = input.replace(m1.group(), "");
			}
		}
		
		return delay;
	}
	
	
	public static String getTime(Date date){
		return timeFormat.format(date);
	}
	
	public static String getTime(Long date){
		return getTime(new Date(date));
	}
	
	public static String getTimeText(String[] strings)
	{
		return getTimeText(getTime(strings));
	}
	
	public static String getTimeText(long millis)
	{
		return getTimeText(millis, true, true, true, true, true, true, true);
	}
	
	public static String getTimeText(long millis, boolean yearsB, boolean monthsB, boolean weeksB, boolean daysB, boolean hoursB, boolean minsB, boolean secondsB)
	{
		return getTimeText(new Date(System.currentTimeMillis() + millis), yearsB, monthsB, weeksB, daysB, hoursB, minsB,
		                   secondsB);
	}
	
	//Millis is milliseconds from base time, if it gives 1970 add System.currentTimeMillis() to input millis
	public static String getTimeText(Date date1, boolean yearsB, boolean monthsB, boolean weeksB, boolean daysB, boolean hoursB, boolean minsB, boolean secondsB)
	{
		Date date = new Date(date1.getTime() + 1000);
		
		Instant instant1 = Instant.ofEpochMilli(Math.min(date.getTime(), System.currentTimeMillis()));
		Instant instant2 = Instant.ofEpochMilli(Math.max(System.currentTimeMillis(), date.getTime()));
		
		Interval interval = new Interval(instant1, instant2);
		Period period = interval.toPeriod();
		
		StringJoiner joiner = new StringJoiner(", ");
		
		int years = period.getYears();
		if (years > 0 && yearsB) {
			joiner.add(years + " year" + (years > 1 ? "s" : ""));
			period = period.minusYears(years);
		}
		
		int months = period.getMonths();
		if (months > 0 && monthsB) {
			joiner.add(months + " month" + (months > 1 ? "s" : ""));
			period = period.minusMonths(months);
		}
		
		int weeks = period.getWeeks();
		if (weeks > 0 && weeksB) {
			joiner.add(weeks + " week" + (weeks > 1 ? "s" : ""));
			period = period.minusWeeks(weeks);
		}
		
		int days = period.getDays();
		if (days > 0 && daysB) {
			joiner.add(days + " day" + (days > 1 ? "s" : ""));
			period = period.minusDays(days);
		}
		
		int hours = period.getHours();
		if (hours > 0 && hoursB) {
			joiner.add(hours + " hour" + (hours > 1 ? "s" : ""));
			period = period.minusHours(hours);
		}
		
		int minutes = period.getMinutes();
		if (minutes > 0 && minsB) {
			joiner.add(minutes + " minute" + (minutes > 1 ? "s" : ""));
			period = period.minusMinutes(minutes);
		}
		
		int seconds = period.getSeconds();
		if (seconds > 0 && secondsB) {
			joiner.add(seconds + " second" + (seconds > 1 ? "s" : ""));
			period = period.minusSeconds(seconds);
			
		}
		int millis = period.getMillis();
		if (joiner.toString().isEmpty() && millis > 0) {
			joiner.add(seconds + " millisecond" + (millis > 1 ? "s" : ""));
		}
		
		return joiner.toString();
	}
}
