package org.bitbucket.pshirshov.izumitk.test;

/**
 * <p>Special annotation allowing class to be exposed from test scope for
 * classpaths of testscopes of dependent artifacts.</p>
 *
 * <p>Note that for now exposing check is substring-based, so you should mention this annotation in every <i>source file</i>
 * you want to expose</p>
 * */
public @interface ExposedTestScope {
}
