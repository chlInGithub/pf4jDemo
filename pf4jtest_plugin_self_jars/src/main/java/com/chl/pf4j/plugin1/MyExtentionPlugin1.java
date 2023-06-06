package com.chl.pf4j.plugin1;

import com.chl.pf4j.api.MyExtention;
import org.apache.commons.math3.random.RandomDataGenerator;
import org.pf4j.Extension;

@Extension
public class MyExtentionPlugin1 implements MyExtention {

    @Override
    public String doSomeExtention(String param) {
        // 测试场景： 系统列举出支持的jar集合，插件依赖其中的jar
        String currentClassLoaderDesc = this.getClass().getClassLoader().toString();
        System.out.println("classLoader is " + currentClassLoaderDesc);

        RandomDataGenerator randomDataGenerator = new RandomDataGenerator();
        System.out.println("nextInt : " + randomDataGenerator.nextInt(0, 100));
        System.out.println("nextInt : " + randomDataGenerator.nextInt(0, 100));
        return "done";
    }
}
