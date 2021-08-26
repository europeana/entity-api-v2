package eu.europeana.entity.definitions.exceptions;

public class InvalidProfileException extends Exception {

    /**
     * 
     */
    private static final long serialVersionUID = 2555752837246565147L;
    public static final String DEFAULT_MESSAGE = "Invalid or not supported profile: ";
    public InvalidProfileException(String profileValue) {
        super(DEFAULT_MESSAGE +profileValue);
    }

    public InvalidProfileException(String profileValue, Throwable th) {
        super(DEFAULT_MESSAGE +profileValue, th);
    }
}
