package eu.europeana.entity.stats.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import javax.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;
import eu.europeana.api.commons.definitions.search.FacetFieldView;
import eu.europeana.api.commons.definitions.search.Query;
import eu.europeana.api.commons.definitions.statistics.entity.EntitiesPerLanguage;
import eu.europeana.api.commons.definitions.statistics.entity.EntityMetric;
import eu.europeana.api.commons.definitions.statistics.entity.EntityStats;
import eu.europeana.entity.config.AppConfigConstants;
import eu.europeana.entity.definitions.model.search.SearchProfiles;
import eu.europeana.entity.definitions.model.vocabulary.EntityTypes;
import eu.europeana.entity.definitions.model.vocabulary.WebEntityConstants;
import eu.europeana.entity.solr.service.SolrEntityService;
import eu.europeana.entity.solr.service.impl.EntityQueryBuilder;
import eu.europeana.entity.stats.exception.UsageStatsException;
import eu.europeana.entity.stats.vocabulary.UsageStatsFields;

/**
 * Usage Statistics Service class
 *
 * @author Srishti Singh (srishti.singh@europeana.eu)
 * @since 2021-09-15
 */
@Service(UsageStatsFields.BEAN_USAGE_SERVICE)
public class UsageStatsService {

    @Resource(name = AppConfigConstants.ENTITY_SOLR_SERVICE)
    SolrEntityService solrEntityService;

    private static final Logger LOG = LogManager.getLogger(UsageStatsService.class);
    private static final List<String> languages = new ArrayList<>(Arrays.asList("en" , "de", "fr", "fi", "it", "es", "sv", "nl", "pl", "pt", "bg",
            "cs", "da", "hu", "ro", "el", "lt", "sk", "et", "hr", "lv", "sl", "ga", "mt"));

    /**
     *  Retrieves the metric response per language per type in percentages.
     *
     * @param metric
     */
    public void getStats(EntityMetric metric) throws UsageStatsException {

        Query perTypeQuery = buildSearchQuery(UsageStatsFields.QUERY_ALL, UsageStatsFields.TYPE_FACET);
        // 1) for total entities per type : query=*&profile=facets&facet=type&pageSize=0
        EntityStats entityPerType = new EntityStats();
        getFacetsResults(perTypeQuery, entityPerType, null);
        // this will only happen if there is no data in DB
        if (!entityPerType.entitiesAvailable()) {
            throw new UsageStatsException(" Entities per type are not present. No stats will be generated");
        }
        metric.setEntitiesPerType(entityPerType);

        // 2) get entities per language and also for inEuropeanaPerLang as well
        List<String> facetNames = new ArrayList<>();
        List<String> facetFields = new ArrayList<>();
        List<String> facetDomainQueries = new ArrayList<>();
        for (String lang : languages) {
          facetNames.add(lang);
          facetFields.add(UsageStatsFields.TYPE_FACET);
          facetDomainQueries.add(UsageStatsFields.QUERY_SKOS_PREF_LABEL_PREFIX + lang + ":*");
        }
        
        Map<String, Map<String, Long>> perLangFacets = solrEntityService.searchWithJsonFacetApi(facetNames, facetFields, facetDomainQueries, null);
        Map<String, Map<String, Long>> perLangInEuropeanaFacets = solrEntityService.searchWithJsonFacetApi(facetNames, facetFields, facetDomainQueries, WebEntityConstants.PARAM_SCOPE_EUROPEANA);
        metric.setEntitiesPerLanguages(getEntitiesPerLang(perLangFacets, null));
        metric.setInEuropeanaPerLanguage(getEntitiesPerLang(perLangInEuropeanaFacets, WebEntityConstants.PARAM_SCOPE_EUROPEANA));

        // 3) getInEuropeanaPerType : query=*&scope=europeana (via API) OR query=*&qf=suggest_filters:europeana (when using Solr directly)
        EntityStats inEuropeanaPerType = new EntityStats();
        getFacetsResults(perTypeQuery, inEuropeanaPerType, WebEntityConstants.PARAM_SCOPE_EUROPEANA);
        metric.setInEuropeanaPerType(inEuropeanaPerType);
    }

    private List<EntitiesPerLanguage> getEntitiesPerLang(Map<String, Map<String, Long>> perLangFacets, String scope)  {
      List<EntitiesPerLanguage> entitiesPerLangList = new ArrayList<>();
      for (Map.Entry<String, Map<String, Long>> perLangFacetsEntry : perLangFacets.entrySet()) {
        EntitiesPerLanguage entitiesPerLang = new EntitiesPerLanguage(perLangFacetsEntry.getKey());
        Map<String, Long> perLangValues = perLangFacetsEntry.getValue();
        setFacetEntities(perLangValues,entitiesPerLang);   
        // LOG is values are not available for the language
        if (!entitiesPerLang.entitiesAvailable() && StringUtils.isBlank(scope)) {
            LOG.error("Entities for lang {} are not available ", perLangFacetsEntry.getKey());
        }
        if (!entitiesPerLang.entitiesAvailable() && WebEntityConstants.PARAM_SCOPE_EUROPEANA.equalsIgnoreCase(scope)) {
            LOG.error("In Europeana entities for lang {} are not available ", perLangFacetsEntry.getKey());
        }

        entitiesPerLangList.add(entitiesPerLang);
      }
      
      return entitiesPerLangList;
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
    private <T extends EntityStats> void getFacetsResults(Query searchQuery, T entityStats, String scope) {
        List<FacetFieldView> facets = solrEntityService.search(searchQuery, null,null, scope).getFacetFields();
        if (!facets.isEmpty()) {
            // fetch the first facet result view
            Map<String, Long> map = facets.get(0).getValueCountMap();
            setFacetEntities(map, entityStats);
        }
    }
    
    private <T extends EntityStats> void setFacetEntities(Map<String, Long> map, T entityStats) {
      for (Map.Entry<String, Long> entry : map.entrySet()) {
          if(entry.getKey().equals(EntityTypes.Agent.getInternalType())) {
              entityStats.setAgents(entry.getValue());
          }
          if(entry.getKey().equals(EntityTypes.Concept.getInternalType())) {
              entityStats.setConcepts(entry.getValue());
          }
          if(entry.getKey().equals(EntityTypes.Place.getInternalType())) {
              entityStats.setPlaces(entry.getValue());
          }
          if(entry.getKey().equals(EntityTypes.TimeSpan.getInternalType())) {
              entityStats.setTimespans(entry.getValue());
          }
          if(entry.getKey().equals(EntityTypes.Organization.getInternalType())) {
              entityStats.setOrganisations(entry.getValue());
          }
      }
      entityStats.setAll(entityStats.getOverall());
  }
    
}
