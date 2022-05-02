package Core.Util;

import Core.Objects.Annotation.Fields.JsonExclude;
import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class JsonUtils
{
	public static final GsonBuilder builder_pretty = new GsonBuilder();
	public static final GsonBuilder builder_non_pretty = new GsonBuilder();
	private static final ExclusionStrategy gson_startegy = new ExclusionStrategy()
	{
		public boolean shouldSkipField(FieldAttributes field)
		{
			return field.getAnnotation(JsonExclude.class) != null;
		}
		public boolean shouldSkipClass(Class<?> clazz)
		{
			return clazz.getAnnotation(JsonExclude.class) != null;
		}
	};
	private static Gson gson_pretty = null;
	private static Gson gson_non_pretty = null;
	
	public static void gson_init(){
		gson_pretty = builder_pretty.serializeNulls().addSerializationExclusionStrategy(gson_startegy).setPrettyPrinting().create();
		gson_non_pretty = builder_non_pretty.serializeNulls().addSerializationExclusionStrategy(gson_startegy).create();
	}
	
	public static Gson getGson_pretty()
	{
		return gson_pretty;
	}
	
	public static Gson getGson_non_pretty()
	{
		return gson_non_pretty;
	}
}
