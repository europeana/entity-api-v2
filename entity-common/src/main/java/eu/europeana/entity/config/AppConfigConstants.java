package eu.europeana.entity.config;

public class AppConfigConstants {

    private AppConfigConstants() {
        // to hide implicit one
    }

    // beans
    public static final String BEAN_AUTHORIZATION_SERVICE     = "entityAuthorizationService";
    public static final String BEAN_CLIENT_DETAILS_SERVICE    = "entityClientDetailsService";
    public static final String BEAN_ENTITY_SERVICE            = "entityService";
    public static final String BEAN_WEB_CONFIG                = "entityWebConfig";
    public static final String BEAN_XML_SERIALIZER            = "entityXmlSerializer";
    public static final String ENTITY_SOLR_SERVICE            = "entitySolrService";
    public static final String ENTITY_SOLR_CLIENT             = "entitySolrClient";
    public static final String BEAN_EM_JSONLD_SERIALIZER      = "entityJsonLdSerialiser";
    public static final String BEAN_USAGE_SERVICE             = "usageConfig";
}
