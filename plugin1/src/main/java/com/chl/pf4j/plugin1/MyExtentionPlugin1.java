package com.chl.pf4j.plugin1;

import com.chl.pf4j.api.MyExtention;
import org.pf4j.Extension;

@Extension
public class MyExtentionPlugin1 implements MyExtention {

	@Override
	public String who() {
		return "I am MyExtentionPlugin1";
	}
}
