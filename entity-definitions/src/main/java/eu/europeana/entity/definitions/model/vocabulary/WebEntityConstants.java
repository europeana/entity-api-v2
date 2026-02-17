package eu.europeana.entity.definitions.model.vocabulary;

public class WebEntityConstants extends WebEntityFields {

    // LD fields
    public static final  String LDP_CONTEXT = "https://www.w3.org/ns/ldp.jsonld";

    // REST API query
    public static final String SLASH = "/";
    public static final String QUERY_PARAM_TYPE = "type";
    public static final String QUERY_PARAM_FIELD = "field";
    public static final String QUERY_PARAM_SCOPE = "scope";
    public static final String QUERY_PARAM_FORMAT = "format";
    public static final String QUERY_PARAM_TEXT = "text";
    public static final String QUERY_PARAM_ALGORITHM = "algorithm";
    public static final String QUERY_PARAM_NAMESPACE = "namespace";
    public static final String QUERY_PARAM_URI = "uri";
    public static final String QUERY_PARAM_FL = "fl";
    public static final String QUERY_PARAM_PAGE = "page";

    public static final String PARAM_TYPE_ALL = "All";
    public static final String PARAM_LANGUAGE_ALL = "all";
    public static final String PARAM_LANGUAGE_EN = "en";
    public static final String PARAM_SCOPE_EUROPEANA = "europeana";
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
    public static final String ENRICH_LABEL_FIELD = "label_enrich";

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
    public static final String TOP_CONCEPT = "topConcept";

    // profile
    public static final String FACETS = "facets";

    // Algorithm types
    public static final String ALGORITHM = "algorithm";
    public static final String SUGGEST_ALGORITHM_DEFAULT = "suggestByLabel";
    public static final String SUGGEST_MONOLINGUAL = "monolingual";
    public static final String FIELD_LABEL = "label";

    // Query definitions
    public static final String HIGHLIGHT_START_MARKER = "<b>";
    public static final String HIGHLIGHT_END_MARKER = "</b>";
    public static final String ROWS = "rows";

    // Defaults
    public static final String ENTITY_API_RESOURCE = "entity";

}
