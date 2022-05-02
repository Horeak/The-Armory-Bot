package Core.Util;

import Core.Main.Logging;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

import static Core.Main.Startup.getReflection;

public class ReflectionUtils
{
	public static void invokeMethods(Class c)
	{
		List<Method> methods = getMethods(c);
		System.out.println("Found " + methods.size() + " " + c.getSimpleName() + " method" + (methods.size() > 1 ? "s" : "") + "!");
		
		for (Method ob : methods) {
			try {
				ob.invoke(null);
			} catch (Exception e1) {
				if (e1 instanceof InvocationTargetException) {
					InvocationTargetException e2 = (InvocationTargetException)e1;
					
					if (e2 != null && e2.getCause() != null) {
						Logging.exception(e2.getCause());
					}
				} else {
					Logging.exception(e1);
				}
			}
		}
	}
	
	public static List<Method> getMethods(Class c)
	{
		//Do not cast null to Class, counts as empty list which causes issues
		return getMethods(c, null);
	}
	
	
	public static List<Method> getMethods(Class c,  Class... parameters)
	{
		Set set1 = getReflection().getMethodsAnnotatedWith(c);
		CopyOnWriteArrayList<Method> list = new CopyOnWriteArrayList<>(set1);
		
		for (Method method : list) {
			if (!Modifier.isStatic(method.getModifiers())) {
				System.err.println("Method: " + method + " is not static!");
				list.remove(method);
				continue;
			}
			
			if (!method.isAccessible()) {
				method.setAccessible(true);
			}
			
			Class[] cc = method.getParameterTypes();
			
			if (parameters != null) {
				if (cc.length != parameters.length) {
					list.remove(method);
					continue;
				}
				
				for (int i = 0; i < cc.length; i++) {
					boolean isSame = false;
					
					if (cc[i] == parameters[i]) {
						isSame = true;
					}
					
					if (parameters[i].isAssignableFrom(cc[i]) || cc[i].isAssignableFrom(parameters[i])) {
						isSame = true;
					}
					
					if (!isSame) {
						list.remove(method);
					}
				}
			}
		}
		
		return list;
	}
	
	
	public static <T> List<T> getTypes(Class<T> type, Class c)
	{
		ArrayList<T> list = new ArrayList<>();
		Set<Class<?>> set1 = getReflection().getTypesAnnotatedWith(c);
		
		for (Class cc : set1) {
			try {
				list.add((T)cc.asSubclass(type).newInstance());
			} catch (InstantiationException | IllegalAccessException e) {
				Logging.exception(e);
			}
		}
		
		return list;
	}
	
	
	public static <T> List<T> getSubTypes(Class<T> type)
	{
		ArrayList<T> list = new ArrayList<>();
		Set<Class<? extends T>> set1 = getReflection().getSubTypesOf(type);
		
		for (Class cc : set1) {
			try {
				list.add((T)cc.asSubclass(type).newInstance());
			} catch (InstantiationException | IllegalAccessException e) {
				Logging.exception(e);
			}
		}
		
		return list;
	}
	
	
	public static List<Field> getFields(Class c)
	{
		Set set1 = getReflection().getFieldsAnnotatedWith(c);
		CopyOnWriteArrayList<Field> list = new CopyOnWriteArrayList<>(set1);
		
		for (Field field : list) {
			if (!Modifier.isStatic(field.getModifiers())) {
				System.err.println("Field: " + field + " is not static!");
				list.remove(field);
				continue;
			}
			
			if (!field.isAccessible()) {
				field.setAccessible(true);
			}
		}
		
		return list;
	}
}
