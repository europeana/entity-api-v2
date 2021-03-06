package eu.europeana.entity.definitions.model.vocabulary;

import eu.europeana.entity.definitions.exceptions.InvalidProfileException;

/**
 * This enumeration is intended for Linked Data profiles
 * 
 * @author GrafR
 *
 */
public enum LdProfiles implements ProfileKeyword {

    MINIMAL(VALUE_PREFER_MINIMAL), STANDARD(VALUE_PREFER_CONTAINEDIRIS), FULL(VALUE_PREFER_FULL);

    private String preferHeaderValue;

    LdProfiles(String preferHeaderValue) {
        this.preferHeaderValue = preferHeaderValue;
    }

    /**
     * Identifying requested profile by Linked Data value. For user friendliness the
     * the comparison is case insensitive
     * 
     * @param ldValue
     * @return
     * @throws ConceptSchemeProfileValidationException
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
     * @throws ConceptSchemeProfileValidationException
     */
    public static LdProfiles getByName(String name) throws InvalidProfileException {

        for (LdProfiles ldType : LdProfiles.values()) {
            if (name.equals(ldType.name().toLowerCase())) {
                return ldType;
            }
        }
        throw new InvalidProfileException(name);
    }

    @Override
    public String getHeaderValue() {
        return preferHeaderValue;
    }

    @Override
    public String toString() {
        return getHeaderValue();
    }

    public String getPreferHeaderValue() {
        return preferHeaderValue;
    }

}
