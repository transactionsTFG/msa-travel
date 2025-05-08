package integration.producer.qualifiers;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.inject.Qualifier;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE,ElementType.FIELD, ElementType.PARAMETER, ElementType.CONSTRUCTOR})
@Qualifier
public @interface TypeUserQueue {
    
}
