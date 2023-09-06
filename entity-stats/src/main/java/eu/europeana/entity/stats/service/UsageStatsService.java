package eu.europeana.entity.stats.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import javax.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.math3.util.Precision;
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
        
        // 2) getInEuropeanaPerType : query=*&scope=europeana (via API) OR query=*&qf=suggest_filters:europeana (when using Solr directly)
        EntityStats inEuropeanaPerType = new EntityStats();
        getFacetsResults(perTypeQuery, inEuropeanaPerType, WebEntityConstants.PARAM_SCOPE_EUROPEANA);
        metric.setInEuropeanaPerType(inEuropeanaPerType);

        // 3) get entities per language and also for inEuropeanaPerLang as well
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
        metric.setEntitiesPerLanguages(getEntitiesPerLang(perLangFacets, entityPerType, null));
        metric.setInEuropeanaPerLanguage(getEntitiesPerLang(perLangInEuropeanaFacets, inEuropeanaPerType, WebEntityConstants.PARAM_SCOPE_EUROPEANA));

    }

    private List<EntitiesPerLanguage> getEntitiesPerLang(Map<String, Map<String, Long>> perLangFacets, EntityStats entitystatsTotal, String scope) throws UsageStatsException  {
      List<EntitiesPerLanguage> entitiesPerLangList = new ArrayList<>();
      for (Map.Entry<String, Map<String, Long>> perLangFacetsEntry : perLangFacets.entrySet()) {
        EntitiesPerLanguage entitiesPerLang = new EntitiesPerLanguage(perLangFacetsEntry.getKey());
        Map<String, Long> perLangValues = perLangFacetsEntry.getValue();
        setFacetEntities(perLangValues,entitiesPerLang);   
        // LOG if values are not available for the language
        if (!entitiesPerLang.entitiesAvailable() && StringUtils.isBlank(scope)) {
            LOG.error("Entities for lang {} are not available ", perLangFacetsEntry.getKey());
        }
        if (!entitiesPerLang.entitiesAvailable() && WebEntityConstants.PARAM_SCOPE_EUROPEANA.equalsIgnoreCase(scope)) {
            LOG.error("In Europeana entities for lang {} are not available ", perLangFacetsEntry.getKey());
        }
        calculatePercentageValues(entitiesPerLang, entitystatsTotal);

        entitiesPerLangList.add(entitiesPerLang);
      }
      
      return entitiesPerLangList;
    }
    
    /**
    *
    * @param entityPerLang entity values per language to be adjusted to percentages
    * @param entitystatsTotal entity values per type
    *
    * @throws UsageStatsException
    */
   private void calculatePercentageValues(EntitiesPerLanguage entitiesPerLang, EntityStats entitystatsTotal) throws UsageStatsException {
     entitiesPerLang.setTimespans(getPercentage(entitiesPerLang.getTimespans(), entitystatsTotal.getTimespans()));
     entitiesPerLang.setPlaces(getPercentage(entitiesPerLang.getPlaces(), entitystatsTotal.getPlaces()));
     entitiesPerLang.setConcepts(getPercentage(entitiesPerLang.getConcepts(), entitystatsTotal.getConcepts()));
     entitiesPerLang.setAgents(getPercentage(entitiesPerLang.getAgents(), entitystatsTotal.getAgents()));
     entitiesPerLang.setOrganisations(getPercentage(entitiesPerLang.getOrganisations(), entitystatsTotal.getOrganisations()));
     entitiesPerLang.setAll(getPercentage(entitiesPerLang.getAll(), entitystatsTotal.getAll()));
   }

   /**
    * Calculates percentage :
    * example : count : 25 entities for language 'en' for type 'Agent'
    *           totalCount : 100 entities for type 'Agent'
    * @param count value of the entity per language and per type
    * @param totalCount total entity value per type
    *
    * @return
    * @throws UsageStatsException
    */
   private float getPercentage(float count, float totalCount) throws UsageStatsException {
     try {
       if (totalCount > 0) {
          return Precision.round((count / totalCount) * 100, 4);
       }
     }
     catch (Exception e) {
       throw new UsageStatsException("Error calculating the percentage values." +e.getMessage());
     }
     return 0.0f;
   }

    private Query buildSearchQuery(String queryString, String facet) {
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
