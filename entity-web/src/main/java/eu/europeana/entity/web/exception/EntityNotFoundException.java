package eu.europeana.entity.web.exception;

import org.springframework.http.HttpStatus;
import eu.europeana.api.commons.error.EuropeanaI18nApiException;
import eu.europeana.entity.app.I18nConstants;

public class EntityNotFoundException extends EuropeanaI18nApiException {

    public EntityNotFoundException(String[] i18params) {
        super(null, null, I18nConstants.CANT_FIND_BY_SAME_AS_URI, i18params);
    }

    @Override
    public HttpStatus getResponseStatus() {
        return HttpStatus.NOT_FOUND;
    }
}
