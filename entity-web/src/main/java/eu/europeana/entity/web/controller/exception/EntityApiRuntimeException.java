package eu.europeana.entity.web.controller.exception;

import eu.europeana.api.commons.error.EuropeanaApiException;
import org.springframework.http.HttpStatus;

public class EntityApiRuntimeException extends EuropeanaApiException {

    private static final long serialVersionUID = -2506967519765835153L;

    public EntityApiRuntimeException(String msg, Throwable th) {
        super(msg, th);
    }

    @Override
    public boolean doLog() {
        return false;
    }

    @Override
    public HttpStatus getResponseStatus() {
        return HttpStatus.INTERNAL_SERVER_ERROR;
    }

    @Override
    public boolean doLogStacktrace() {
        return false;
    }
}
