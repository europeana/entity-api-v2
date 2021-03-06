package eu.europeana.entity.definitions.model.vocabulary;

public interface WebEntityConstants extends WebEntityFields {

    // REST API query
    public static final String SLASH = "/";
    public static final String PAR_CHAR = "?";

    public static final String PATH_PARAM_TYPE = "type";
    public static final String PATH_PARAM_IDENTIFIER = "identifier";

    public static final String QUERY_PARAM_TYPE = "type";
    public static final String QUERY_PARAM_FIELD = "field";
    public static final String QUERY_PARAM_SCOPE = "scope";
    public static final String QUERY_PARAM_FORMAT = "format";
    public static final String QUERY_PARAM_TEXT = "text";
    public static final String QUERY_PARAM_ALGORITHM = "algorithm";
    public static final String QUERY_PARAM_NAMESPACE = "namespace";
    public static final String QUERY_PARAM_URI = "uri";
    public static final String QUERY_PARAM_FL = "fl";

    public static final String PARAM_TYPE_ALL = "All";
    public static final String PARAM_LANGUAGE_ALL = "all";
    public static final String PARAM_LANGUAGE_EN = "en";

    public static final String PARAM_TYPE_AGENT = "agent";
    public static final String PARAM_TYPE_PLACE = "place";
    public static final String PARAM_TYPE_CONCEPT = "concept";
    public static final String PARAM_SCOPE_EUROPEANA = "europeana";
//	public static final String PARAM_TIMESPAN = "timespan";
    public static final String PARAM_DEFAULT_ROWS = "10";

    // Enrichment Constants
    public static final int ENRICH_MAX_PAGE_SIZE = 50;
    public static final String LANG_FIELD_DELIMITER = ".";
    public static final String COMMA = ",";
    public static final String LANG_REGEX = "[a-zA-Z]+";
    public static final String QUOTE = "\"";
    public static final String BACKSLASH = "\\";
    public static final String SOLR_ESCAPED_QUOTE = "\\\"";
    public static final String SOLR_ESCAPED_BACKSLASH = "\\\\";

    public static final String PARAM_PROFILE_FACETS = "facets";

    public static final String TYPE_BASIC_CONTAINER = "BasicContainer";

    /** URI constants */
    public static final String PROTOCOL_GEO = "geo:";

    /**
     * Solr fields
     */
    public static final String SOLR_INTERNAL_TYPE = "internal_type";
    public static final String FIELD_DELIMITER = ":";
    public static final String SOLR_AND = " AND ";
    public static final String SOLR_OR = " OR ";

    /**
     * Model attribute names
     */
    public static final String AT_CONTEXT = "@context";
    public static final String AT_LANGUAGE = "@language";

//	public static final String ID = "id";
    public static final String ITEMS = "items";

    public static final String TOP_CONCEPT = "topConcept";

    public static final String LDP_CONTEXT = "https://www.w3.org/ns/ldp.jsonld";
    public static final String ENTITY_CONTEXT = "http://www.europeana.eu/schemas/context/entity.jsonld";
    public static final String TOTAL = "total";

    // Page fields
    public static final String PART_OF = "partOf";
    public static final String PREV = "prev";
    public static final String NEXT = "next";
    public static final String FACETS = "facets";

    // Algorithm types
    public static final String ALGORITHM = "algorithm";
    // see SuggestAlgorithmTypes.suggestByLabel
    public static final String SUGGEST_ALGORITHM_DEFAULT = "suggestByLabel";
    public static final String SUGGEST_MONOLINGUAL = "monolingual";
    public static final String FIELD_LABEL = "label";

    // Query definitions
    public static final String HIGHLIGHT_START_MARKER = "<b>";
    public static final String HIGHLIGHT_END_MARKER = "</b>";
    public static final String ROWS = "rows";

    // Defaults
    public static final String USER_ANONYMOUNS = "anonymous";
    public static final String PROFILE_MINIMAL = "minimal";
    public final static String BASE_CONCEPT_SCHEME_URL = "http://data.europeana.eu/scheme/";
    public static final String ENTITY_API_RESOURCE = "entity";

}
