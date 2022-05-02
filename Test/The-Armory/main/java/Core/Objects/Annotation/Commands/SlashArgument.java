package Core.Objects.Annotation.Commands;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention( RetentionPolicy.RUNTIME )
@Target( ElementType.FIELD )
public @interface SlashArgument {
	String key();
	String text();
	
	boolean required() default false;
	
	String[] choices() default "";
	
	float minValue() default -1;
	float maxValue() default -1;
}
