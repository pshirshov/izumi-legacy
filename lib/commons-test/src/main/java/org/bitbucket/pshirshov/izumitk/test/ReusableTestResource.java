package org.bitbucket.pshirshov.izumitk.test;

/**
 */
public interface ReusableTestResource<R> {
    R get();

    void destroy() throws Exception;
}
