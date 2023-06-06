package com.chl.pf4j.plugin1;

import org.pf4j.Plugin;

public class Plugin1Plugin extends Plugin {

	@Override
	public void start() {
		System.out.println("Plugin1Plugin start");
	}

	@Override
	public void stop() {
		System.out.println("Plugin1Plugin stop");
	}

	@Override
	public void delete() {
		System.out.println("Plugin1Plugin delete");
	}
}
