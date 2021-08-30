package eu.europeana.entity.web.service.impl;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public abstract class BaseEntityServiceImpl {

	
	Logger logger = LogManager.getLogger(getClass());

	public Logger getLogger() {
		return logger;
	}

	public void setLogger(Logger logger) {
		this.logger = logger;
	}

}
