package com.ur.urcap.sample.scriptwrapper.impl;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import com.ur.urcap.api.contribution.ProgramNodeService;


/**
 * Script Wrapper activator for the OSGi bundle URCAPS contribution
 *
 */
public class Activator implements BundleActivator {
	@Override
	public void start(BundleContext bundleContext) throws Exception {
		System.out.println("Activator says good day, to the Wrapper!");
		
		bundleContext.registerService(ProgramNodeService.class, new ScriptWrapperProgramNodeService(), null);
	}

	@Override
	public void stop(BundleContext bundleContext) throws Exception {
		System.out.println("Activator says Auf Wiedersehen to the Wrapper!");
	}
}

