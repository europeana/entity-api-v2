package eu.europeana.entity.web.exception;

import eu.europeana.api.commons_sb3.error.EuropeanaI18nApiException;
import org.springframework.http.HttpStatus;
import eu.europeana.entity.config.I18nConstants;

import java.util.List;

public class EntityNotFoundException extends EuropeanaI18nApiException {

    public EntityNotFoundException(List<String> i18params) {
        super(null, null, null, HttpStatus.NOT_FOUND, I18nConstants.CANT_FIND_BY_SAME_AS_URI, i18params);
    }

    @Override
    public HttpStatus getResponseStatus() {
        return HttpStatus.NOT_FOUND;
    }

    /**
     * EA-4372 stop logging this exception
     */
    @Override
    public boolean doLog() {
        return false;
    }

    @Override
    public boolean doLogStacktrace() {
        return false;
    }
}