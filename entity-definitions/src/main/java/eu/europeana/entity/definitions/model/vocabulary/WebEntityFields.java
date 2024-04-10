package eu.europeana.entity.definitions.model.vocabulary;

public interface WebEntityFields {

    // LD fields
    String CONTEXT = "@context";
    String ENTITY_CONTEXT = "http://www.europeana.eu/schemas/context/entity.jsonld";
    String LANGUAGE_EN = "en";

    // common fields
    String ID = "id";
    String TYPE = "type";
    String SAME_AS = "sameAs";
    String IDENTIFIER = "identifier";
    String HAS_PART = "hasPart";
    String IS_PART_OF = "isPartOf";
    String PREF_LABEL = "prefLabel";
    String HIDDEN_LABEL = "hiddenLabel";
    String ALT_LABEL = "altLabel";
    String NOTE = "note";
    String DEPICTION = "depiction";
    String SOURCE = "source";
    String THUMBNAIL = "thumbnail";
    String IS_SHOWN_BY = "isShownBy";

    //Enrichment field
    String ENRICH_LABEL_FIELD = "label_enrich";

    // common administrative information
    String CREATED = "created";
    String MODIFIED = "modified";
    String AGGREGATION = "Aggregation";
    String IS_AGGREGATED_BY = "isAggregatedBy";
    String AGGREGATES = "aggregates";

    // concept fields
    String NOTATION = "notation";
    String RELATED = "related";
    String BROADER = "broader";
    String NARROWER = "narrower";

    // match fields
    String EXACT_MATCH = "exactMatch";
//	String COREF = "coref";
    String CLOSE_MATCH = "closeMatch";
    String BROAD_MATCH = "broadMatch";
    String NARROW_MATCH = "narrowMatch";
    String RELATED_MATCH = "relatedMatch";
    String IN_SCHEME = "inScheme";

    // Agent fields
    String DATE = "date";
    String BEGIN = "begin";
    String END = "end";
    String HAS_MET = "hasMet";
    String IS_RELATED_TO = "isRelatedTo";
    String NAME = "name";
    String BIOGRAPHICAL_INFORMATION = "biographicalInformation";
    String DATE_OF_BIRTH = "dateOfBirth";
    String DATE_OF_DEATH = "dateOfDeath";
    String PLACE_OF_BIRTH = "placeOfBirth";
    String PLACE_OF_DEATH = "placeOfDeath";
    String DATE_OF_ESTABLISHMENT = "dateOfEstablishment";
    String DATE_OF_TERMINATION = "dateOfTermination";
    String GENDER = "gender";
    String PROFESSION_OR_OCCUPATION = "professionOrOccupation";
    String WAS_PRESENT_AT = "wasPresentAt";

    // Place fields
    String LATITUDE = "lat";
    String LONGITUDE = "long";
    String ALTITUDE = "alt";
    String LATITUDE_LONGITUDE = "lat_long";

    String IS_NEXT_IN_SEQUENCE = "isNextInSequence";
    String TYPE_PLACE = "Place";
    

    // Organization fields
    String DESCRIPTION = "description";
    String ACRONYM = "acronym";
    String COUNTRY = "country";
    String ORGANIZATION_DOMAIN = "organizationDomain";
    String EUROPEANA_ROLE = "europeanaRole";
    String GEOGRAPHIC_LEVEL = "geographicLevel";
    String FOAF_LOGO = "logo";
    String FOAF_HOMEPAGE = "homepage";
    String FOAF_PHONE = "phone";
    String FOAF_MBOX = "mbox";
    String AGGREGATED_VIA = "aggregatedVia";

    // Address Fields
    String STREET_ADDRESS = "streetAddress";
    String LOCALITY = "locality";
    String REGION = "region";
    String POSTAL_CODE = "postalCode";
    String COUNTRY_NAME = "countryName";
    String POST_OFFICE_BOX = "postOfficeBox";
    String HAS_GEO = "hasGeo";
    String HAS_ADDRESS = "hasAddress";
    String ADDRESS_TYPE = "Address";

    // ConceptScheme fields
    String TOTAL = "total";

    // Authentication
    String USER_ADMIN = "admin";

    // Type
    String WEB_RESOURCE = "WebResource";

}
