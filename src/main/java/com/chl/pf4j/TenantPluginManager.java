package com.chl.pf4j;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import cn.hutool.core.util.ZipUtil;
import org.pf4j.JarPluginManager;
import org.pf4j.PluginManager;
import org.pf4j.PluginState;

/**
 * 租户插件管理器
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

    /**
     * 系统扩展点集合
     */
    private static volatile Set<Class> extensionPointSet = new HashSet<>();

    public static void addExtensionPoint(Class extensionPoint) {
        extensionPointSet.add(extensionPoint);
    }

    /**
     * 加载租户的插件，租户维度进行同步处理
     * @param tenantId
     */
    public static void loadAndStartPlugin(String tenantId) {
        synchronized (tenantId) {
            stopAndUnloadPlugin(tenantId);
            String path = pluginDir+""+tenantId+"/plugin"+tenantId+".jar";
            String pluginId = pluginManager.loadPlugin(Paths.get(new File(path).toURI()));

            startAndInstanceExtension(tenantId, pluginId);
        }
    }

    private static void startAndInstanceExtension(String tenantId, String pluginId) {
        PluginState pluginState = pluginManager.startPlugin(pluginId);
        // 在系统定义的扩展集合范围内，找到实现的扩展
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

    public static void loadAndStartPluginWithinDependJars(String tenantId)
            throws TenantPluginJarNotFoundException, InvocationTargetException, NoSuchMethodException, IOException,
            IllegalAccessException, InterruptedException {
        synchronized (tenantId) {
            stopAndUnloadPlugin(tenantId);
            // tenant-plugin dir
            String tenantPluginDir = pluginDir+""+tenantId;
            // tenant-plugin jar
            String pluginJarPath = tenantPluginDir+"/plugin.jar";
            // unzip dir
            String pluginRealPath = tenantPluginDir+"/plugin";
            File pluginRealPathFile = new File(pluginRealPath);
            if (!pluginRealPathFile.exists()) {
                pluginRealPathFile.mkdirs();
            }
            // unzip plugin.jar to /plugin
            ZipUtil.unzip(pluginJarPath, pluginRealPath);

            File[] files = pluginRealPathFile.listFiles();
            File pluginFile = null;
            List<File> dependJars = new ArrayList<>();
            for (File file : files) {
                if (file.getName().contains("plugin_self_jars")) {
                    pluginFile = file;
                    continue;
                }
                if (file.getName().endsWith("jar")) {
                    dependJars.add(file);
                    continue;
                }
            }

            if (Objects.isNull(pluginFile)) {
                throw new TenantPluginJarNotFoundException(pluginJarPath);
            }

            String pluginId = pluginManager.loadPlugin(Paths.get(pluginFile.toURI()));
            try {
                ClassLoader pluginClassLoader = pluginManager.getPlugin(pluginId).getPluginClassLoader();
                Method addURL = pluginManager.getPlugin(pluginId).getPluginClassLoader().getClass().getDeclaredMethod("addURL", URL.class);
                addURL.setAccessible(true);
                for (File dependJar : dependJars) {
                    addURL.invoke(pluginClassLoader, dependJar.toURI().toURL());
                }
            } catch (Exception e) {
                throw e;
            }

            startAndInstanceExtension(tenantId, pluginId);
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

    /**
     * 获取具体租户的具体扩展实现
     * @param tenantId
     * @param pointClass
     * @param <T>
     * @return
     * @throws TenantExtensionNotFoundException
     */
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
