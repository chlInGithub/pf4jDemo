package com.chl.pf4j.plugin1;

import java.util.Date;

import com.chl.pf4j.api.MyExtention;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateFormatUtils;
import org.apache.commons.lang.time.DateUtils;
import org.pf4j.Extension;

@Extension
public class MyExtentionPlugin1 implements MyExtention {

	@Override
	public String doSomeExtention(String param) {
	    // 测试场景： 系统列举出支持的jar集合，插件依赖其中的jar
        String currentClassLoaderDesc = this.getClass().getClassLoader().toString();
        if (StringUtils.isBlank(param)) {
            return "why, no param";
        }
        Date tomorrow = DateUtils.addDays(new Date(), 1);
        String format = DateFormatUtils.format(tomorrow, DateFormatUtils.ISO_DATE_FORMAT.getPattern());
        return currentClassLoaderDesc + ": Tomorrow is " + format;
	}
}
