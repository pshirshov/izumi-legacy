package org.bitbucket.pshirshov.izumitk;


import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation is intended to mark traits/interfaces of those the only
 * one implementation should be instantiated and available in CDI context
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.ANNOTATION_TYPE)
public @interface TargetPoint {}
