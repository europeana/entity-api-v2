package eu.europeana.entity.stats.service;

import eu.europeana.api.commons.definitions.search.FacetFieldView;
import eu.europeana.api.commons.definitions.search.Query;
import eu.europeana.api.commons.definitions.statistics.entity.EntitiesPerLanguage;
import eu.europeana.api.commons.definitions.statistics.entity.EntityMetric;
import eu.europeana.api.commons.definitions.statistics.entity.EntityStats;
import eu.europeana.entity.definitions.model.search.SearchProfiles;
import eu.europeana.entity.solr.config.EntitySolrConfig;
import eu.europeana.entity.solr.service.SolrEntityService;
import eu.europeana.entity.solr.service.impl.EntityQueryBuilder;
import eu.europeana.entity.stats.exception.UsageStatsException;
import eu.europeana.entity.stats.vocabulary.UsageStatsFields;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Usage Statistics Service class
 *
 * @author Srishti Singh (srishti.singh@europeana.eu)
 * @since 2021-09-15
 */
@Service(UsageStatsFields.BEAN_USAGE_SERVICE)
public class UsageStatsService {

    @Resource(name = EntitySolrConfig.ENTITY_SOLR_SERVICE)
    SolrEntityService solrEntityService;

    private static final Logger LOG = LogManager.getLogger(UsageStatsService.class);
    private static final List<String> languages = new ArrayList<>(Arrays.asList("en" , "de", "fr", "fi", "it", "es", "sv", "nl", "pl", "pt", "bg",
            "cs", "da", "hu", "ro", "el", "lt", "sk", "et", "hr", "lv", "sl", "ga", "mt"));

    /**
     *  Retrieves the metric response per language per type in percentages.
     *
     * @param metric
     */
    public void getStatsForLang(EntityMetric metric) throws UsageStatsException {
       // for total entities : query=*&profile=facets&facet=type&pageSize=0
       EntityStats entityTotal = getFacetsResults(buildSearchQuery(UsageStatsFields.QUERY_ALL, UsageStatsFields.FACET));
       if (entityTotal == null) {
           throw new UsageStatsException(" There are no entities present for facet=type. No stats will be generated");
       }
        List<EntitiesPerLanguage> entitiesPerLanguageList = new ArrayList<>();
        for(String lang : languages) {
            EntitiesPerLanguage entitiesPerLanguage = getEntityPerLangValues(lang, entityTotal);
            if(entitiesPerLanguage != null) {
                entitiesPerLanguageList.add(entitiesPerLanguage);
            } else {
                LOG.error("No stats found for lang {} in entity api", lang);
            }
        }
        metric.setEntities(entitiesPerLanguageList);
    }

    /**
     * Retrieves Entity stats for the given lang
     * query=skos_prefLabel.<lang>:*&profile=facets&facet=type&pageSize=0
     *
     * @param lang
     * @param entityTotal
     * @return
     * @throws UsageStatsException
     */
    private EntitiesPerLanguage getEntityPerLangValues(String lang, EntityStats entityTotal) throws UsageStatsException {
        EntitiesPerLanguage entityPerLanguage = new EntitiesPerLanguage();
        StringBuilder query = new StringBuilder(UsageStatsFields.QUERY_SKOS_PREF_LABEL);
        query.append(".").append(lang).append(":*");
        // get facet results
        EntityStats entityStatsForLang = getFacetsResults(buildSearchQuery(query.toString(), UsageStatsFields.FACET));
        if (entityStatsForLang != null) {
            // calculate percentage
            calculatePercentageValues(entityStatsForLang, entityTotal, entityPerLanguage);
            entityPerLanguage.setLang(lang);
            return entityPerLanguage;
        }
        return null;
    }

    private static Query buildSearchQuery(String queryString, String facet) {
        EntityQueryBuilder queryBuilder = new EntityQueryBuilder();
        return queryBuilder.buildSearchQuery(queryString, null, queryBuilder.toArray(facet), null, 0, 0,
                SearchProfiles.facets, null);
    }

    /**
     * get the value count from the facets results from Solr
     *
     * @param searchQuery
     * @return
     */
    private EntityStats getFacetsResults(Query searchQuery) {
        List<FacetFieldView> facets = solrEntityService.search(searchQuery, null,null, null ).getFacetFields();
        if (!facets.isEmpty()) {
            EntityStats entityStats = new EntityStats();
            // fetch the first facet result view
            Map<String, Long> map = facets.get(0).getValueCountMap();
            for (Map.Entry<String, Long> entry : map.entrySet()) {
                if(entry.getKey().equals("Agent")) {
                    entityStats.setAgents(entry.getValue());
                }
                if(entry.getKey().equals("Concept")) {
                    entityStats.setConcepts(entry.getValue());
                }
                if(entry.getKey().equals("Place")) {
                    entityStats.setPlaces(entry.getValue());
                }
                if(entry.getKey().equals("Timespan")) {
                    entityStats.setTimespans(entry.getValue());
                }
                if(entry.getKey().equals("Organization")) {
                    entityStats.setOrganisations(entry.getValue());
                }
            }
            entityStats.setTotal(getTotal(entityStats));
            return entityStats;
        }
        return null;
    }

    private static void calculatePercentageValues(EntityStats entityPerLanguage, EntityStats entitystatsTotal, EntitiesPerLanguage entities) throws UsageStatsException {
       entities.setTimespans(getPercentage(entityPerLanguage.getTimespans(), entitystatsTotal.getTimespans()));
       entities.setPlaces(getPercentage(entityPerLanguage.getPlaces(), entitystatsTotal.getPlaces()));
       entities.setConcepts(getPercentage(entityPerLanguage.getConcepts(), entitystatsTotal.getConcepts()));
       entities.setAgents(getPercentage(entityPerLanguage.getAgents(), entitystatsTotal.getAgents()));
       entities.setOrganisations(getPercentage(entityPerLanguage.getOrganisations(), entitystatsTotal.getOrganisations()));
       entities.setTotal(getPercentage(entityPerLanguage.getTotal(), entitystatsTotal.getTotal()));
    }

    private static float getPercentage(float value, float total) throws UsageStatsException {
      try {
          if (total != 0.0) {
              return (value / total) * 100;
          }
        } catch (Exception e) {
            throw new UsageStatsException("Error calculating the percentage values." +e.getMessage());
        }
        return 0.0f;
    }

    private float getTotal(EntityStats entitystats) {
        return (entitystats.getAgents() + entitystats.getConcepts() + entitystats.getOrganisations() + entitystats.getPlaces() + entitystats.getTimespans());
    }
}