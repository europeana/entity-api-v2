package eu.europeana.entity.web.exception;

import eu.europeana.api.commons_sb3.error.EuropeanaApiException;
import org.springframework.http.HttpStatus;

/**
 * Exception generated while generating statistics
 */
public class UsageStatsException extends EuropeanaApiException {

    private static final long serialVersionUID = -2506967519765835153L;

    /**
     * Initialise a new exception for which there is no root cause
     *
     * @param message invalid version
     */
    public UsageStatsException(String message) {
        super(message);
    }

    /**
     * Initialise a new exception for which there is no root cause
     *
     * @param message invalid version
     */
    public UsageStatsException(String message, Throwable th) {
        super(message, th);
    }
    /**
     * We don't want to log the stack trace for this exception
     *
     * @return false
     */
    @Override
    public boolean doLog() {
        return false;
    }

    /**
     * We don't want to log the stack trace for this exception
     *
     * @return false
     */
    @Override
    public boolean doLogStacktrace() {
        return true;
    }

    @Override
    public HttpStatus getResponseStatus() {
        return HttpStatus.INTERNAL_SERVER_ERROR;
    }
}
