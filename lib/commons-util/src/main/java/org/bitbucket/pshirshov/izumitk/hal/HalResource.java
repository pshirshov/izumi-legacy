package org.bitbucket.pshirshov.izumitk.hal;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Inherited()
public @interface HalResource {
    /**
     * Self-link in the format of {@code path/to/resource/{?parameter}} (leading slash must be omitted)
     * Base URI will be prepended to the value.
     */
    String self() default "";
}
