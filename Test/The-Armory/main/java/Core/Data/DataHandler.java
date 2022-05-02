package Core.Data;

import Core.Main.Logging;
import Core.Main.Startup;
import Core.Objects.Annotation.Fields.DataObject;
import Core.Util.FileUtil;
import Core.Util.JsonUtils;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import org.apache.commons.codec.digest.DigestUtils;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.*;

public class DataHandler
{
	private static final CopyOnWriteArrayList<Field> objects = new CopyOnWriteArrayList<>();
	private static final ConcurrentHashMap<Field, String> values = new ConcurrentHashMap<>();
	private static final ConcurrentHashMap<Field, Class> fieldClasses = new ConcurrentHashMap<>();
	private static final ArrayList<Field> toSave = new ArrayList<>();
	private static final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
	private static final CopyOnWriteArrayList<Field> queueDataLoad = new CopyOnWriteArrayList<>();
	public static boolean load_done = false;
	private static String prefix = "";
	
	public static void init(String path)
	{
		prefix = path;
		new SaveDataThread().start();
	}
	
	private static void initFields(Collection<Field> fields)
	{
		for (Field fe : fields) {
			if (Modifier.isStatic(fe.getModifiers())) {
				objects.add(fe);
				
				load(fe);
				Object t = getValue(fe);
				
				if (t != null) {
					updateValue(fe, t);
				}
			} else {
				System.err.println("Field: " + fe + ", is not static!");
			}
		}
	}
	
	private static void check()
	{
		for (Field fe : objects) {
			if (values.containsKey(fe)) {
				String t = genChecksum(getValue(fe));
				String tg = values.get(fe);
				
				if (t != null && !Objects.equals(t, tg) && !t.equals(tg)) {
					differentValue(fe, getValue(fe));
				}
				
			}
		}
	}
	
	public static String genChecksum(Object t)
	{
		return DigestUtils.md5Hex(JsonUtils.getGson_non_pretty().toJson(t));
	}
	
	private static void differentValue(Field fe, Object value)
	{
		updateValue(fe, value);
		
		if (!toSave.contains(fe)) {
			toSave.add(fe);
			
			executor.schedule(() -> {
				toSave.remove(fe);
				save(fe, value);
			}, 1000, TimeUnit.MILLISECONDS);
		}
	}
	
	private static void updateValue(Field fe, Object t)
	{
		String checkSum = genChecksum(t);
		values.put(fe, checkSum);
	}
	
	private static void save(Field fe,  Object value)
	{
		HashMap<String, Object> saveData = new HashMap<>();
		ArrayList<Field> obs = new ArrayList<>();
		
		if (value == null) {
			value = getValue(fe);
		}
		
		DataObject ob = null;
		
		boolean clas = fe.getDeclaringClass() != null && fe.getDeclaringClass().isAnnotationPresent(DataObject.class);
		
		if (clas) {
			ob = fe.getDeclaringClass().getAnnotation(DataObject.class);
			
		} else if (fe.isAnnotationPresent(DataObject.class)) {
			ob = fe.getAnnotation(DataObject.class);
		}
		
		if (ob == null) {
			return;
		}
		
		String key = !ob.name().isEmpty() ? ob.name() : fe.getDeclaringClass().getName() + "|" + fe.getName();
		File fes = FileUtil.getFile((ob.use_prefix() ? prefix + "/" : "") + ob.file_path());
		
		obs.add(fe);
		
		if (clas) {
			HashMap<String, Object> values = new HashMap<>();
			
			for (Field fe1 : fe.getDeclaringClass().getFields()) {
				values.put(fe1.getName(), getValue(fe1));
			}
			
			saveData.put(key, values);
		} else {
			saveData.put(key, value);
		}
		
		boolean pretty = ob.pretty();
		
		for (Field field : objects) {
			if (field.isAnnotationPresent(DataObject.class)) {
				DataObject ob1 = field.getAnnotation(DataObject.class);
				
				if (ob1.file_path().equals(ob.file_path())) {
					if (!obs.contains(field)) {
						obs.add(field);
						
						if (pretty && !ob1.pretty()) {
							pretty = false;
						}
						
						Object t = getValue(field);
						String key1 = !ob1.name().isEmpty() ? ob1.name() : (field.getDeclaringClass().getName() + "|" + field.getName());
						
						if (!saveData.containsKey(key1)) {
							saveData.put(key1, t);
						}
					}
				}
			}
		}
		
		try {
			write(fes.toPath(),
			                  pretty ? JsonUtils.getGson_pretty().toJson(saveData) : JsonUtils.getGson_non_pretty().toJson(
					                  saveData));
		} catch (IOException e) {
			Logging.exception(e);
		}
	}
	
	private static Object getValue(Field field)
	{
		try {
			if (Modifier.isStatic(field.getModifiers())) {
				if (!field.isAccessible()) {
					field.setAccessible(true);
				}
				
				if (fieldClasses.containsKey(field)) {
					return field.get(fieldClasses.get(field));
				} else {
					Class t = field.getDeclaringClass();
					fieldClasses.put(field, t);
					return field.get(t);
				}
			} else {
				System.err.println("Field: " + field + ", is not static!");
			}
		} catch (IllegalAccessException e) {
			Logging.exception(e);
		}
		
		return null;
	}
	
	private static void load(Field fe)
	{
		try {
			if (fe.isAnnotationPresent(DataObject.class)) {
				DataObject ob = fe.getAnnotation(DataObject.class);
				
				File fes = FileUtil.getFile((ob.use_prefix() ? prefix + "/" : "") + ob.file_path());
				
				JsonElement el = JsonUtils.getGson_pretty().fromJson(read(fes.toPath()), JsonElement.class);
				String key = !ob.name().isEmpty() ? ob.name() : fe.getDeclaringClass().getName() + "|" + fe.getName();
				
				
				if (el != null) {
					JsonObject obs = el.getAsJsonObject();
					if (obs.has(key)) {
						try {
							JsonElement t1 = obs.get(key);
							Object tg = JsonUtils.getGson_pretty().fromJson(t1, fe.getGenericType());
							
							setValue(fe, tg);
							updateValue(fe, tg);
						}catch (Exception e){
							Logging.exception(e);
						}
					}
				} else {
					save(fe, null);
				}
			}
		} catch (IOException e) {
			Logging.exception(e);
		}
	}
	
	private static void loadClass(Class fe)
	{
		try {
			if (fe.isAnnotationPresent(DataObject.class)) {
				DataObject ob = (DataObject)fe.getAnnotation(DataObject.class);
				
				File fes = FileUtil.getFile((ob.use_prefix() ? prefix + "/" : "") + ob.file_path());
				
				JsonElement el = JsonUtils.getGson_pretty().fromJson(read(fes.toPath()), JsonElement.class);
				String key = !ob.name().isEmpty() ? ob.name() : fe.getDeclaringClass().getName() + "|" + fe.getName();
				
				
				if (el != null) {
					JsonObject obs = el.getAsJsonObject();
					
					if (obs.has(key)) {
						JsonElement t1 = obs.get(key);
						Map<String, Object> tg = JsonUtils.getGson_pretty().fromJson(t1,
						                                                             new TypeToken<Map<String, Object>>() {}.getType());
						
						for (Field fe1 : fe.getFields()) {
							if (tg.containsKey(fe1.getName())) {
								Object fg = JsonUtils.getGson_non_pretty().fromJson(
										JsonUtils.getGson_non_pretty().toJson(tg.get(fe1.getName())),
										fe1.getGenericType());
								
								setValue(fe1, fg);
								updateValue(fe1, fg);
							} else {
								save(fe1, null);
							}
						}
						
					} else {
						for (Field fe1 : fe.getFields()) {
							save(fe1, null);
						}
					}
				} else {
					for (Field fe1 : fe.getFields()) {
						save(fe1, null);
					}
				}
			}
		} catch (IOException e) {
			Logging.exception(e);
		}
	}
	
	private static void setValue(Field field, Object value)
	{
		try {
			if (Modifier.isStatic(field.getModifiers())) {
				if (!field.isAccessible()) {
					field.setAccessible(true);
				}
				
				Class t;
				
				if (fieldClasses.containsKey(field)) {
					t = fieldClasses.get(field);
				} else {
					t = field.getDeclaringClass();
					fieldClasses.put(field, t);
				}
				
				if (t != null) {
					field.set(t, value);
				}
			} else {
				System.err.println("Field: " + field + ", is not static!");
			}
		} catch (IllegalAccessException e) {
			Logging.exception(e);
		}
	}
	
	private static class SaveDataThread extends Thread
	{
		@Override
		public void run()
		{
			super.run();
			
			Set<Field> fields = Startup.getReflection().getFieldsAnnotatedWith(DataObject.class);
			Set<Class<?>> classes = Startup.getReflection().getTypesAnnotatedWith(DataObject.class);
			
			initFields(fields);
			
			for (Class c : classes) {
				loadClass(c);
				
				for (Field fe : c.getFields()) {
					if (Modifier.isStatic(fe.getModifiers())) {
						objects.add(fe);
						
						Object t = getValue(fe);
						if (t != null) {
							updateValue(fe, t);
						}
					}
				}
			}
			
			load_done = true;
			
			while (isAlive()) {
				if (queueDataLoad.size() > 0) {
					if (Startup.init) {
						for (int i = 0; i < queueDataLoad.size(); i++) {
							Field fe = queueDataLoad.get(i);
							queueDataLoad.remove(i);
						}
					}
				}
				
				//TODO Try and find a more optimized way of checking and saving values in near real time
				check();
				
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					Logging.exception(e);
				}
			}
		}
	}
	public static String read(Path path) throws IOException
	{
		return Files.readString(path, StandardCharsets.UTF_8);
	}
	
	public static void write(Path path, String contents) throws IOException
	{
		Files.write(path, contents.getBytes(StandardCharsets.UTF_8));
	}
}
