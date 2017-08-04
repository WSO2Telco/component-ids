package com.wso2telco.identity.application.authentication.endpoint;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import com.wso2telco.core.config.DataHolder;

public class InitVariables implements ServletContextListener {

	@Override
	public void contextDestroyed(final ServletContextEvent event) {
	}

	@Override
	public void contextInitialized(final ServletContextEvent event) {

		final String locale_prop = "javax.servlet.jsp.jstl.fmt.locale";
		String locale = DataHolder.getInstance().getMobileConnectConfig()
				.getLocale();
		System.setProperty(locale_prop, locale);

	}
}
