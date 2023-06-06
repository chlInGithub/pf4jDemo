package com.chl.pf4j;

import java.io.File;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import com.chl.pf4j.api.MyExtention;
import org.pf4j.JarPluginManager;

public class Pf4jTest {

    public static void main(String[] args) {
        //mockTenantCall();
        mockTenantCallWithinDependJars();
    }

    public static void mockTenantCallWithinDependJars(){
        TenantPluginManager.addExtensionPoint(MyExtention.class);

        List<String> tenantIds = Arrays.asList("456");
        for (String tenantId : tenantIds) {
            try {
                TenantPluginManager.loadAndStartPluginWithinDependJars(tenantId);
                MyExtention extension = null;
                extension = TenantPluginManager.getExtension(tenantId, MyExtention.class);
                System.out.println(tenantId + " : " + extension.doSomeExtention("some param"));
            } catch (Exception e) {
                System.out.println("插件启动了，没有找到扩展，不符合预期。" + e.getMessage());
            }
        }
        for (String tenantId : tenantIds) {
            TenantPluginManager.stopAndUnloadPlugin(tenantId);
            try {
                TenantPluginManager.getExtension(tenantId, MyExtention.class);
            } catch (TenantExtensionNotFoundException e) {
                System.out.println("插件卸载了，没有找到扩展，符合预期。 " + e.getMessage());
            }
        }
    }

    public static void mockTenantCall() {

        TenantPluginManager.addExtensionPoint(MyExtention.class);

        List<String> tenantIds = Arrays.asList("123", "456");
        for (String tenantId : tenantIds) {
            TenantPluginManager.loadAndStartPlugin(tenantId);
            MyExtention extension = null;
            try {
                extension = TenantPluginManager.getExtension(tenantId, MyExtention.class);
            } catch (TenantExtensionNotFoundException e) {
                System.out.println("插件启动了，没有找到扩展，不符合预期。" + e.getMessage());
            }
            System.out.println(tenantId + " : " + extension.doSomeExtention("some param"));
        }

        for (String tenantId : tenantIds) {
            TenantPluginManager.stopAndUnloadPlugin(tenantId);
            try {
                TenantPluginManager.getExtension(tenantId, MyExtention.class);
            } catch (TenantExtensionNotFoundException e) {
                System.out.println("插件卸载了，没有找到扩展，符合预期。 " + e.getMessage());
            }
        }
    }

    public static void simpleTest() {
        JarPluginManager pluginManager = new JarPluginManager();

        String pluginId = pluginManager.loadPlugin(Paths.get(new File("D://pf4jplugin//plugin1-1.0-SNAPSHOT.jar").toURI()));
        pluginManager.startPlugin(pluginId);

        List<MyExtention> extensions = pluginManager.getExtensions(MyExtention.class, pluginId);
        for (MyExtention extension : extensions) {
            System.out.println(extension.doSomeExtention("some param"));
        }

        pluginManager.stopPlugin(pluginId);
        pluginManager.unloadPlugin(pluginId);
    }
}
