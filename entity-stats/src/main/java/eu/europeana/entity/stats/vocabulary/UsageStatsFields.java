package eu.europeana.entity.stats.vocabulary;

import eu.europeana.entity.definitions.model.vocabulary.EntitySolrFields;

/**
 * Usage Statistics Fields Constants
 *
 * @author Srishti Singh (srishti.singh@europeana.eu)
 * @since 2021-09-15
 */
public class UsageStatsFields {

    public static final String BEAN_USAGE_SERVICE = "usageConfig";

    // query constants
    public static final String TYPE_FACET = "type";
    public static final String QUERY_SKOS_PREF_LABEL_PREFIX =  EntitySolrFields.PREF_LABEL_PREFIX  + ".";
    public static final String QUERY_ALL = "*";

    // Metric Constants
    public static final String OVERALL_TOTAL_TYPE = "OverallTotal";

}
