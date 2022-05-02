package Core.Util;

import Core.Main.Logging;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
public class ConnectionUtils
{
	public static URLConnection getConnection( String url)
	{
		if (url == null || url.isEmpty()) {
			return null;
		}
		
		try {
			URLConnection connection = new URL(url).openConnection();
			connection.setRequestProperty("User-Agent",
			                              "Mozilla/5.0 (compatible; Googlebot/2.1; +http://www.google.com/bot.html)");
			
			connection.setConnectTimeout(60000);
			connection.setReadTimeout(60000);
			connection.connect();
			
			return connection;
			
		} catch (Exception e1) {
			Logging.exception(e1);
		}
		return null;
	}
	
	public static Document getDocument(String url) throws IOException
	{
		HttpClient client = new DefaultHttpClient();
		HttpGet request = new HttpGet(url);
		HttpResponse response = client.execute(request);
		InputStream stream = response.getEntity().getContent();
		
		String encoding = null;
		
		if(response.getEntity() != null && response.getEntity().getContentEncoding() != null){
			if(response.getEntity().getContentEncoding().getValue() != null){
				encoding =  response.getEntity().getContentEncoding().getValue();
			}
		}
		
		encoding = encoding == null ? "UTF-8" : encoding;
		
		if (stream != null) {
			String html = IOUtils.toString(stream, encoding);
			
			if (html != null && !html.isEmpty()) {
				return Jsoup.parseBodyFragment(html);
			}
		}
		
		return null;
	}
	
	public static List<String> extractUrls(String text)
	{
		List<String> containedUrls = new ArrayList<String>();
		String urlRegex = "((https?|ftp|gopher|telnet|file):((//)|(\\\\))+[\\w\\d:#@%/;$~_?\\+-=\\\\\\.&]*)";
		Pattern pattern = Pattern.compile(urlRegex, Pattern.CASE_INSENSITIVE);
		Matcher urlMatcher = pattern.matcher(text);
		while (urlMatcher.find()) {
			containedUrls.add(text.substring(urlMatcher.start(0), urlMatcher.end(0)));
		}
		return containedUrls;
	}
	
	public static File downloadFileWithAgent(String url, String fileType) throws IOException
	{
		URLConnection connection = new URL(url).openConnection();
		connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.11 (KHTML, like Gecko) Chrome/23.0.1271.95 Safari/537.11");
		connection.setConnectTimeout(10000);
		connection.setReadTimeout(10000);
		connection.connect();
		
		return getFileFromStream(connection.getInputStream(), fileType);
	}
	
	public static File downloadFileAsBot(String url, String fileType) throws IOException
	{
		URLConnection connection = new URL(url).openConnection();
		connection.setRequestProperty("User-Agent", "Mozilla/5.0 (compatible; Googlebot/2.1; +http://www.google.com/bot.html)");
		connection.setConnectTimeout(10000);
		connection.setReadTimeout(10000);
		connection.connect();
		
		return getFileFromStream(connection.getInputStream(), fileType);
	}
	
	public static File getFileFromStream(InputStream stream, String suffix) throws IOException
	{
		File file = File.createTempFile("tempData", suffix);
		FileUtils.copyInputStreamToFile(stream, file);
		return file;
	}
}
