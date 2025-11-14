package eu.europeana.entity.web.exception;

import eu.europeana.api.commons.error.EuropeanaI18nApiException;
import org.springframework.http.HttpStatus;
import eu.europeana.entity.app.I18nConstants;

public class ParamValidationException extends EuropeanaI18nApiException {

	private static final long serialVersionUID = 3664526076494279093L;

	public ParamValidationException(String[] i18params){
		super(null, null, I18nConstants.INVALID_PARAM_VALUE, i18params);
	}
	
	public ParamValidationException(String i18nKey, String[] i18params){
		super(null, null, i18nKey, i18params);
	}

	public ParamValidationException(String message, String i18nKey, String[] i18params){
		super(message, null, i18nKey, i18params);
	}
	public ParamValidationException(String i18nKey, String[] i18params, Throwable th){
		super(null, null, HttpStatus.BAD_REQUEST, i18nKey, i18params, th);
	}

	@Override
	public HttpStatus getResponseStatus() {
		return HttpStatus.BAD_REQUEST;
	}
}
