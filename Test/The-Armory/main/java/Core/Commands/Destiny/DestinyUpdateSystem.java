package Core.Commands.Destiny;

import Core.Commands.Destiny.DestinyDataBaseObjects.Destiny.Destiny1ItemSystem;
import Core.Commands.Destiny.DestinyDataBaseObjects.Destiny2.Destiny2ItemSystem;
import Core.Main.Logging;
import Core.Main.Startup;
import Core.Objects.Annotation.Fields.DataObject;
import Core.Objects.Annotation.Method.Interval;
import Core.Objects.Annotation.Method.Startup.Init;
import org.apache.commons.io.FileUtils;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.concurrent.TimeUnit;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class DestinyUpdateSystem
{
	private static final String folderName = "destiny";
	private static final String name_d1 = "world_d1.db";
	private static final String name_d2 = "world_d2.db";
	public static File infoFile_d2;
	public static File infoFile_d1;
	
	@DataObject( file_path = folderName + "/destinyInfo.json", name = "d1_manifest" )
	public static String cur_d1_manifest = null;
	
	@DataObject( file_path = folderName + "/destinyInfo.json", name = "d2_manifest" )
	public static String cur_d2_manifest = null;
	
	@DataObject( file_path = folderName + "/destinyInfo.json", name = "d1_version" )
	public static String cur_d1_version = null;
	
	@DataObject( file_path = folderName + "/destinyInfo.json", name = "d2_version" )
	public static String cur_d2_version = null;
	
	@DataObject( file_path = folderName + "/destinyInfo.json", name = "last_d1_update" )
	public static Long last_d1_update = null;
	
	@DataObject( file_path = folderName + "/destinyInfo.json", name = "last_d2_update" )
	public static Long last_d2_update = null;
	private static boolean updatedQueued = false;
	
	@Init
	public static void DestinyItemCommandInit()
	{
		infoFile_d1 = new File(Startup.FilePath + "/" + folderName + "/" + name_d1);
		infoFile_d2 = new File(Startup.FilePath + "/" + folderName + "/" + name_d2);
		
		if (!updatedQueued) {
			updatedQueued = true;
		}
	}
	
	@Interval(time_interval = 6, time_unit = TimeUnit.HOURS)
	public static void updateCheck()
	{
		if(!updatedQueued){
			DestinyItemCommandInit();
		}
		
		boolean d1System = false;
		boolean d2System = false;
		
		try {
			JSONObject d1_object = DestinySystem.getResponse("https://www.bungie.net/Platform/Destiny/Manifest/");
			JSONObject d2_object = DestinySystem.getResponse("https://www.bungie.net/Platform/Destiny2/Manifest/");
			
			if (d1_object.has("Response")) {
				JSONObject ob1 = d1_object.getJSONObject("Response");
				String version = ob1.getString("version");
				
				if (ob1.has("mobileWorldContentPaths")) {
					String manifest = ob1.getJSONObject("mobileWorldContentPaths").get("en").toString();
					
					if (cur_d1_manifest == null || !cur_d1_manifest.equalsIgnoreCase(
							manifest) && (cur_d1_version == null || !cur_d1_version.equalsIgnoreCase(version))) {
						System.out.println("New D1 manifest version: " + version + ", Old: " + cur_d1_version);
						
						updateManifest(manifest, 1, cur_d1_version, version);
						cur_d1_version = version;
						d1System = true;
					}
				}
			}
			
			if (d2_object.has("Response")) {
				JSONObject ob2 = d2_object.getJSONObject("Response");
				String version = ob2.getString("version");
				
				if (ob2.has("mobileWorldContentPaths")) {
					JSONObject object = ob2.getJSONObject("mobileWorldContentPaths");
					
					if(object.has("en")) {
						String manifest = object.get("en").toString();
						
						if (manifest != null) {
							if (cur_d2_manifest == null || !cur_d2_manifest.equals(manifest) && (cur_d2_version == null || !cur_d2_version.equalsIgnoreCase(version))) {
								System.out.println("New D2 manifest version: " + version + ", Old: " + cur_d2_version);
								
								updateManifest(manifest, 2, cur_d2_version, version);
								cur_d2_version = version;
								d2System = true;
							}
						}
					}
				}
			}
			
		} catch (IOException e) {
			Logging.exception(e);
		}
		
		try {
			if (!d1System) {
				Destiny1ItemSystem.init();
			}
		} catch (Exception e) {
			Logging.exception(e);
		}
		
		try {
			if (!d2System) {
				Destiny2ItemSystem.init();
			}
		} catch (Exception e) {
			Logging.exception(e);
		}
	}
	
	private static void updateManifest(String manifest, int version, String oldVersion, String newVersion)
	throws IOException
	{
		System.out.println("Destiny " + version + " manifest update begin");
		
		File fe = new File(Startup.FilePath + "/" + folderName + "/" + "tempD" + version + ".zip");
		fe.createNewFile();
		
		File oldFile = new File(
				Startup.FilePath + "/" + folderName + "/" + "oldVersion-" + (version == 1 ? infoFile_d1 : infoFile_d2).getName());
		oldFile.createNewFile();
		
		FileUtils.copyFile((version == 1 ? infoFile_d1 : infoFile_d2), oldFile);
		
		URL website = new URL(DestinySystem.BASE_RESOURCE_URL + manifest);
		FileUtils.copyURLToFile(website, fe);
		
		unZipIt(fe, version == 1 ? infoFile_d1 : infoFile_d2);
		fe.delete();
		
		if (version == 1) {
			cur_d1_manifest = manifest;
			last_d1_update = System.currentTimeMillis();
			
			Destiny1ItemSystem.reInit();
		}
		
		if (version == 2) {
			cur_d2_manifest = manifest;
			last_d2_update = System.currentTimeMillis();
			
			Destiny2ItemSystem.reInit();
		}
		
		oldFile.delete();
		oldFile.deleteOnExit();
		
		System.out.println("Destiny " + version + " manifest update done");
	}
	
	public static void unZipIt( File in,  File out)
	{
		if (in == null || out == null) return;
		
		byte[] buffer = new byte[1024];
		try {
			FileInputStream stream = new FileInputStream(in);
			ZipInputStream zis = new ZipInputStream(stream);
			ZipEntry ze = zis.getNextEntry();
			
			while (ze != null) {
				FileOutputStream fos = new FileOutputStream(out);
				
				int len;
				while ((len = zis.read(buffer)) > 0) {
					fos.write(buffer, 0, len);
				}
				
				fos.close();
				ze = zis.getNextEntry();
			}
			
			zis.closeEntry();
			zis.close();
			
			stream.close();
			
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}
}
