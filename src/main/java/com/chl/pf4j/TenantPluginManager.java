package com.chl.pf4j;

import java.io.File;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.pf4j.JarPluginManager;
import org.pf4j.PluginManager;
import org.pf4j.PluginState;

/**
 * 支持租户插件
 */
public class TenantPluginManager {
    private static volatile PluginManager pluginManager = new JarPluginManager();

    /**
     * 租户 ： pluginId
     */
    private static Map<String, String> tenantMapPluginId = new ConcurrentHashMap();

    /**
     * 租户 : 扩展点 : 扩展实例
     */
    private static Map<String, Map<String, Object>> tenantMapPointMapExtension = new ConcurrentHashMap<>();

    private static final String pluginDir = "D:/pf4jplugin/";

    private static volatile Set<Class> extensionPointSet = new HashSet<>();

    public static void addExtensionPoint(Class extensionPoint) {
        extensionPointSet.add(extensionPoint);
    }

    public static void loadAndStartPlugin(String tenantId) {
        synchronized (tenantId) {
            stopAndUnloadPlugin(tenantId);
            String path = pluginDir+""+tenantId+"/plugin"+tenantId+".jar";
            String pluginId = pluginManager.loadPlugin(Paths.get(new File(path).toURI()));
            PluginState pluginState = pluginManager.startPlugin(pluginId);
            for (Class extensionPoint : extensionPointSet) {
                List extensions = pluginManager.getExtensions(extensionPoint, pluginId);
                if (Objects.isNull(extensions)) {
                    continue;
                }
                Map<String, Object> pointMapExtension = tenantMapPointMapExtension.get(tenantId);
                if (Objects.isNull(pointMapExtension)) {
                    pointMapExtension = new ConcurrentHashMap<>();
                    tenantMapPointMapExtension.put(tenantId, pointMapExtension);
                }
                pointMapExtension.put(extensionPoint.getSimpleName(), extensions.get(0));
            }
            tenantMapPluginId.put(tenantId, pluginId);
        }
    }

    public static void stopAndUnloadPlugin(String tenantId) {
        synchronized (tenantId) {
            String pluginId = tenantMapPluginId.remove(tenantId);
            if (Objects.isNull(pluginId)) {
                return;
            }
            Map<String, Object> pointMapExtension = tenantMapPointMapExtension.remove(tenantId);
            pointMapExtension.clear();
            pluginManager.stopPlugin(pluginId);
            pluginManager.unloadPlugin(pluginId);
        }
    }

    public static <T> T getExtension(String tenantId, Class<T> pointClass) throws TenantExtensionNotFoundException {
        String pointName = pointClass.getSimpleName();
        Map<String, Object> pointAndExtension = tenantMapPointMapExtension.get(tenantId);
        if (Objects.isNull(pointAndExtension)) {
            throw new TenantExtensionNotFoundException(tenantId);
        }
        Object extension = pointAndExtension.get(pointName);
        if (Objects.isNull(extension)) {
            throw new TenantExtensionNotFoundException(tenantId + " " + pointName);
        }
        return (T)extension;
    }
}
