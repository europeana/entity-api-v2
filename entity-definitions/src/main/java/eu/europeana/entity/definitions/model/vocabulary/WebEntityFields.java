package eu.europeana.entity.definitions.model.vocabulary;

/**
 * Class with field values used during json/jsonld serialisation
 */
@SuppressWarnings("java:S115")
public class WebEntityFields {

    /**
     * common Json/Jsonld serialization fields
     * The naming convention is not followed and rather named how the value exactly is
     * This done to avoid confusion with other class fields that have the
     * same constants but with the different value.
     * @See: org.apache.stanbol.commons.jsonld.JsonLdCommon has the same constants
     *       with "@" appended to it
     * This also helps visualize the JSON response more easily,
     * rather than having to navigate to another class to check the exact value of the constant.
     */
    public static final String sameAs           = "sameAs";
    public static final String identifier       = "identifier";
    public static final String hasPart          = "hasPart";
    public static final String isPartOf         = "isPartOf";
    public static final String prefLabel        = "prefLabel";
    public static final String hiddenLabel      = "hiddenLabel";
    public static final String altLabel         = "altLabel";
    public static final String note             = "note";
    public static final String depiction        = "depiction";
    public static final String source           = "source";
    public static final String thumbnail        = "thumbnail";
    public static final String isShownBy        = "isShownBy";
    public static final String pageRank         = "pageRank";
    public static final String recordCount      = "recordCount";
    public static final String score            = "score";
    public static final String created          = "created";
    public static final String modified         = "modified";
    public static final String Aggregation      = "Aggregation";
    public static final String isAggregatedBy   = "isAggregatedBy";

    // concept fields
    public static final String notation         = "notation";
    public static final String related          = "related";
    public static final String broader          = "broader";
    public static final String narrower         = "narrower";
    // match fields
    public static final String exactMatch       = "exactMatch";
    public static final String closeMatch       = "closeMatch";
    public static final String broadMatch       = "broadMatch";
    public static final String narrowMatch      = "narrowMatch";
    public static final String relatedMatch     = "relatedMatch";
    public static final String inScheme         = "inScheme";
    // Agent fields
    public static final String date             = "date";
    public static final String begin            = "begin";
    public static final String end              = "end";
    public static final String hasMet           = "hasMet";
    public static final String isRelatedTo      = "isRelatedTo";
    public static final String name             = "name";
    public static final String biographicalInformation = "biographicalInformation";
    public static final String dateOfBirth         = "dateOfBirth";
    public static final String dateOfDeath         = "dateOfDeath";
    public static final String placeOfBirth        = "placeOfBirth";
    public static final String placeOfDeath        = "placeOfDeath";
    public static final String dateOfEstablishment = "dateOfEstablishment";
    public static final String dateOfTermination   = "dateOfTermination";
    public static final String gender              = "gender";
    public static final String professionOrOccupation = "professionOrOccupation";
    public static final String wasPresentAt         = "wasPresentAt";
    // Place fields
    public static final String lat                  = "lat";
    public static final String _long                = "long";
    public static final String alt                  = "alt";
    public static final String isNextInSequence     = "isNextInSequence";
    public static final String Place                = "Place";
    // Organization fields
    public static final String description          = "description";
    public static final String acronym              = "acronym";
    public static final String country              = "country";
    public static final String organizationDomain   = "organizationDomain";
    public static final String europeanaRole        = "europeanaRole";
    public static final String geographicLevel      = "geographicLevel";
    public static final String FOAF_LOGO            = "logo";
    public static final String FOAF_HOMEPAGE        = "homepage";
    public static final String FOAF_PHONE           = "phone";
    public static final String FOAF_MBOX            = "mbox";
    public static final String aggregatedVia        = "aggregatedVia";
    // Address Fields
    public static final String streetAddress        = "streetAddress";
    public static final String locality             = "locality";
    public static final String region               = "region";
    public static final String postalCode           = "postalCode";
    public static final String countryName          = "countryName";
    public static final String postOfficeBox        = "postOfficeBox";
    public static final String hasGeo               = "hasGeo";
    public static final String hasAddress           = "hasAddress";
    public static final String Address              = "Address";
    public static final String WebResource          = "WebResource";

}
