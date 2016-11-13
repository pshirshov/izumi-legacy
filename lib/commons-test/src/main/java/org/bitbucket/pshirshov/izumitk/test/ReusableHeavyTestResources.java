package org.bitbucket.pshirshov.izumitk.test;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.helpers.MessageFormatter;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 */
@SuppressWarnings("SynchronizationOnStaticField")
public final class ReusableHeavyTestResources {
    private static final Logger LOG = LoggerFactory.getLogger(ReusableHeavyTestResources.class);
    private static final Map<String, ReusableTestResource<?>> RESOURCES = new LinkedHashMap<>();

    private static final AtomicBoolean DESTROY_ON_EXIT;

    static {
        String debugProp = System.getProperty("tests.debug");
        DESTROY_ON_EXIT = new AtomicBoolean(!Boolean.parseBoolean(debugProp));
    }

    private ReusableHeavyTestResources() {
    }


    public static void removeAll() {
        synchronized (RESOURCES) {
            RESOURCES.clear();
        }
    }

    public static void destroyAll() {
        synchronized (RESOURCES) {
            for (Map.Entry<String, ReusableTestResource<?>> resource : RESOURCES.entrySet()) {
                try {
                    LOG.info("Destroying resource {}...", resource.getKey());
                    resource.getValue().destroy();
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            }
            removeAll();
        }

    }

    public static <R> R register(String name, ReusableTestResource<R> resource){
        synchronized (RESOURCES) {
            if (!RESOURCES.containsKey(name)) {
                RESOURCES.put(name, resource);
                return resource.get();
            } else {
                throw new IllegalArgumentException(
                        MessageFormatter.format("Resource {} already exists", name).getMessage());
            }
        }
    }

    public static <R> R update(String name, ReusableTestResource<R> resource){
        synchronized (RESOURCES) {
            RESOURCES.put(name, resource);
            return resource.get();
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> T get(String name){
        synchronized (RESOURCES) {
            ReusableTestResource<T> resource = (ReusableTestResource<T>) RESOURCES.get(name);
            if (resource == null) {
                return null;
            } else {
                return resource.get();
            }
        }
    }

    public static void setDebugMode(Boolean value) {
        DESTROY_ON_EXIT.set(value);
    }

    static {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            if (DESTROY_ON_EXIT.get()) {
                destroyAll();
            } else {
                LOG.warn("Debug mode, leaving resources untouched");
            }
        }));
    }
}
