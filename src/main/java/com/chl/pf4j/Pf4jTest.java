package com.chl.pf4j;

import java.io.File;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import com.chl.pf4j.api.MyExtention;
import org.pf4j.JarPluginManager;

public class Pf4jTest {

    public static void main(String[] args) {
        mockTenantCall();
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
                System.out.println(" not OK " + e.getMessage());
            }
            System.out.println(tenantId + " : " + extension.who());

            TenantPluginManager.stopAndUnloadPlugin(tenantId);
            try {
                extension = TenantPluginManager.getExtension(tenantId, MyExtention.class);
            } catch (TenantExtensionNotFoundException e) {
                System.out.println("OK " + e.getMessage());
            }
        }
    }

    public static void simpleTest() {
        JarPluginManager pluginManager = new JarPluginManager();

        String pluginId = pluginManager.loadPlugin(Paths.get(new File("D://pf4jplugin//plugin1-1.0-SNAPSHOT.jar").toURI()));
        pluginManager.startPlugin(pluginId);

        List<MyExtention> extensions = pluginManager.getExtensions(MyExtention.class, pluginId);
        for (MyExtention extension : extensions) {
            System.out.println(extension.who());
        }

        pluginManager.stopPlugin(pluginId);
        pluginManager.unloadPlugin(pluginId);
    }
}
