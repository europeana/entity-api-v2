package eu.europeana.entity.definitions.model.vocabulary;

import eu.europeana.entity.definitions.exceptions.InvalidProfileException;
import static eu.europeana.entity.definitions.model.vocabulary.ProfileKeyword.*;

/**
 * This enumeration is intended for Linked Data profiles
 * 
 * @author GrafR
 *
 */
public enum LdProfiles {

    MINIMAL(VALUE_PREFER_MINIMAL),
    STANDARD(VALUE_PREFER_CONTAINEDIRIS),
    FULL(VALUE_PREFER_FULL);

    private String preferHeaderValue;

    LdProfiles(String preferHeaderValue) {
        this.preferHeaderValue = preferHeaderValue;
    }

    /**
     * Identifying requested profile by Linked Data value.
     * For user-friendliness the comparison is case-insensitive
     * 
     * @param headerValue
     * @return
     * @throws InvalidProfileException
     */
    public static LdProfiles getByHeaderValue(String headerValue) throws InvalidProfileException {
        for (LdProfiles ldType : LdProfiles.values()) {
            if (headerValue.equals(ldType.getHeaderValue())) {
                return ldType;
            }
        }
        throw new InvalidProfileException(headerValue);
    }

    /**
     * 
     * @param name
     * @return
     * @throws InvalidProfileException
     */
    public static LdProfiles getByName(String name) throws InvalidProfileException {

        for (LdProfiles ldType : LdProfiles.values()) {
            if (name.equals(ldType.name().toLowerCase())) {
                return ldType;
            }
        }
        throw new InvalidProfileException(name);
    }

    public String getHeaderValue() {
        return preferHeaderValue;
    }

    public String getPreferHeaderValue() {
        return preferHeaderValue;
    }

    @Override
    public String toString() {
        return getHeaderValue();
    }

}
