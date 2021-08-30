package eu.europeana.entity.web.controller.exception;

import javax.annotation.Resource;

import org.springframework.web.bind.annotation.ControllerAdvice;

import eu.europeana.api.commons.config.i18n.I18nService;
import eu.europeana.api.commons.web.controller.exception.AbstractExceptionHandlingController;
import eu.europeana.entity.web.config.EntityAppConfig;

@ControllerAdvice
public class GlobalExceptionHandler extends AbstractExceptionHandlingController {

	@Resource(name = EntityAppConfig.BEAN_I18N_SERVICE)
	I18nService i18nService;

	protected I18nService getI18nService() {
		return i18nService;
	}
	
	

	
}
