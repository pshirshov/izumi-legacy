package org.bitbucket.pshirshov.izumitk;


import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation is intended to mark traits/interfaces acting like an
 * <i>extension point</i>. All implementations of an extension point
 * would be available in CDI context bound to extension point class.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.ANNOTATION_TYPE)
public @interface ExtensionPoint {}
