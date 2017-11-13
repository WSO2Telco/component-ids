package com.wso2telco.identity.application.authentication.endpoint;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.jsp.jstl.core.Config;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.wso2telco.core.config.ConfigLoader;
import com.wso2telco.core.config.DataHolder;

public class InitVariables implements ServletContextListener {

	private static Log log = LogFactory.getLog(InitVariables.class);
	private static String locale = null;

	@Override
	public void contextDestroyed(final ServletContextEvent event) {
	}

	@Override
	public void contextInitialized(final ServletContextEvent event) {
		try {
			if (DataHolder.getInstance().getMobileConnectConfig() == null) {
				DataHolder.getInstance().setMobileConnectConfig(
						ConfigLoader.getInstance().getMobileConnectConfig());
			}
			locale = DataHolder.getInstance().getMobileConnectConfig()
					.getLocale();

		} catch (Exception ex) {
			log.error("Error while reading Mobile Connect Configuration");

		}

		Config.set(event.getServletContext(), Config.FMT_LOCALE, locale);

	}

}
