package eu.europeana.entity.web.service.impl;

import eu.europeana.api.commons_sb3.definitions.search.Query;
import eu.europeana.api.commons_sb3.definitions.search.ResultSet;
import eu.europeana.api.commons_sb3.definitions.search.result.ResultsPage;
import eu.europeana.api.commons_sb3.definitions.search.result.impl.ResultsPageImpl;
import eu.europeana.api.commons_sb3.definitions.vocabulary.CommonApiConstants;
import eu.europeana.api.commons_sb3.error.EuropeanaApiException;
import eu.europeana.api.commons_sb3.error.EuropeanaI18nApiException;
import eu.europeana.api.commons_sb3.error.exceptions.InvalidParamException;
import eu.europeana.api.commons_sb3.error.exceptions.ResourceNotFoundException;
import eu.europeana.api.commons_sb3.oauth2.utils.OAuthUtils;
import eu.europeana.entity.config.I18nConstants;
import eu.europeana.entity.config.AppConfigConstants;
import eu.europeana.entity.definitions.exceptions.UnsupportedEntityTypeException;
import eu.europeana.entity.definitions.model.Entity;
import eu.europeana.entity.definitions.model.vocabulary.EntityTypes;
import eu.europeana.entity.definitions.model.vocabulary.SuggestAlgorithmTypes;
import eu.europeana.entity.definitions.model.vocabulary.WebEntityConstants;
import eu.europeana.entity.solr.exception.EntityRetrievalException;
import eu.europeana.entity.solr.exception.EntitySuggestionException;
import eu.europeana.entity.solr.exception.InvalidSearchQueryException;
import eu.europeana.entity.solr.service.SolrEntityService;
import eu.europeana.entity.utils.EntityUtils;
import eu.europeana.entity.web.config.EntityWebConfig;
import eu.europeana.entity.web.model.view.EntityPreview;
import eu.europeana.entity.web.service.EntityService;
import eu.europeana.entity.web.service.SearchServiceUtils;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.StringUtils;
import org.apache.hc.core5.net.URIBuilder;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static eu.europeana.entity.utils.EntityUtils.isEmptyOrAll;

/**
 * Implementation class
 * @author GrafR
 */
@Service(AppConfigConstants.BEAN_ENTITY_SERVICE)
public class EntityServiceImpl implements EntityService {

    @Resource(name = AppConfigConstants.ENTITY_SOLR_SERVICE)
    SolrEntityService solrEntityService;

    @Resource(name = AppConfigConstants.BEAN_WEB_CONFIG)
    private EntityWebConfig entityWebConfig;

    @Override
    public Entity retrieveByUrl(String type, String identifier) throws EuropeanaApiException {

        String entityUri = buildStoredEntityId(type, identifier);
        Entity result;
        try {
            result = solrEntityService.searchByUrl(type, entityUri);
        } catch (EntityRetrievalException e) {
            throw new EuropeanaI18nApiException(e.getMessage(), null, null, HttpStatus.INTERNAL_SERVER_ERROR,
                    I18nConstants.SERVER_ERROR_CANT_RETRIEVE_URI, Arrays.asList(entityUri), e);
        } catch (UnsupportedEntityTypeException e) {
            throw new EuropeanaI18nApiException(null, null, null, HttpStatus.NOT_FOUND,
                    I18nConstants.UNSUPPORTED_ENTITY_TYPE,
                    Arrays.asList(WebEntityConstants.ENTITY_API_RESOURCE, type), e);
        }
        // if not found send appropriate error message
        if (result == null) {
            throw new ResourceNotFoundException(Entity.class, Arrays.asList(entityUri));
        }
        return result;
    }


    private String buildStoredEntityId(String type, String identifier) {
        StringBuilder stringBuilder = new StringBuilder();
        //TODO: replace by relative path search
        stringBuilder.append(entityWebConfig.getEntityIdBaseUrl());
        if (!entityWebConfig.getEntityIdBaseUrl().endsWith(WebEntityConstants.SLASH)) {
            stringBuilder.append(WebEntityConstants.SLASH);
        }
        if (StringUtils.isNotEmpty(type))
            stringBuilder.append(type.toLowerCase()).append(WebEntityConstants.SLASH);
        if (StringUtils.isNotEmpty(identifier))
            stringBuilder.append(identifier);

        String entityUri = stringBuilder.toString();
        return entityUri;
    }


    /*
     * (non-Javadoc)
     *
     * @see eu.europeana.entity.web.service.EntityService#
     */
    @Override
    public <T extends EntityPreview> ResultSet<T> suggest(String text, String[] language, List<EntityTypes> entityTypes,
                                                      String scope, String namespace, int rows, SuggestAlgorithmTypes algorithm)
            throws EuropeanaI18nApiException {
        ResultSet<? extends EntityPreview> res;
        try {
            switch (algorithm) {
                case suggestByLabel:
                    res = solrEntityService.suggestByLabel(text, language, entityTypes, scope, rows);
                    break;
                case monolingual:
                    res = solrEntityService.suggestByLanguage(text, language, entityTypes, scope, rows);
                    break;
                default:
                    throw new EuropeanaI18nApiException(null, null, null, HttpStatus.BAD_REQUEST,
                            I18nConstants.UNSUPPORTED_ALGORITHM_TYPE, Arrays.asList(algorithm.name()));
            }
        } catch (EntitySuggestionException e) {
            throw new EuropeanaI18nApiException(e.getMessage(), null, null,
					HttpStatus.INTERNAL_SERVER_ERROR, null, null, e);
        }

        return (ResultSet<T>) res;
    }

    @Override
    public List<String> resolveByUri(String uri) {
        List<String> result = solrEntityService.searchByCoref(uri);
        return result;
    }

    @Override
    public <T extends Entity> ResultSet<T> search(Query query, String[] outLanguage, List<EntityTypes> entityTypes,
                                              String scope) throws EuropeanaApiException {
        try {
            return solrEntityService.search(query, outLanguage, entityTypes, scope);
        } catch (EntityRetrievalException e) {
            throw SearchServiceUtils.convertSolrSearchException(query.toString(), e);
        } catch (InvalidSearchQueryException e) {
            throw SearchServiceUtils.convertSolrSearchException(query.toString(), e);
        }
    }

    /**
     * @param entityTypes
     * @param suggest
     * @throws InvalidParamException
     */
    public List<EntityTypes> validateEntityTypes(List<EntityTypes> entityTypes, boolean suggest) throws InvalidParamException {
        // search
        if (!suggest) {
            if (isEmptyOrAll(entityTypes)) {
                //no filtering needed
                return null;
            }
        } else {// suggest

            if (isEmptyOrAll(entityTypes)) {
                if (entityTypes == null)
                    entityTypes = new ArrayList<>();
                entityTypes.clear();
                entityTypes.add(EntityTypes.Concept);
                entityTypes.add(EntityTypes.Agent);
                entityTypes.add(EntityTypes.Place);
                entityTypes.add(EntityTypes.Organization);
                entityTypes.add(EntityTypes.Aggregator);
                entityTypes.add(EntityTypes.TimeSpan);
            }

        }

        return entityTypes;
    }

    public <T extends Entity> ResultsPage<T> buildResultsPage(Query searchQuery, ResultSet<T> results,
                                                              HttpServletRequest request) throws EuropeanaI18nApiException {
        @SuppressWarnings({"rawtypes", "unchecked"})
        ResultsPage<T> resPage = new ResultsPageImpl<>();

        resPage.setItems(results.getResults());
        resPage.setFacetFields(results.getFacetFields());

        resPage.setTotalInPage(results.getResults().size());
        resPage.setTotalInCollection(results.getResultSize());

        String serviceBasePath = "/entity";
        {
            if (entityWebConfig.getEntityApiEndpoint().endsWith("/"))
                //in case that / is present in the endpoint configuration
                serviceBasePath += "/";
        }
        String servicePath = request.getServletPath().replace(serviceBasePath, "");
        StringBuffer methodFullUri = new StringBuffer(entityWebConfig.getEntityApiEndpoint());
        methodFullUri.append(servicePath);

        String collectionUrl = buildCollectionUrl(searchQuery, methodFullUri, request.getQueryString());
        resPage.setCollectionUri(collectionUrl);

        int currentPage = searchQuery.getPageNr();
        String currentPageUrl = buildPageUrl(collectionUrl, currentPage, searchQuery.getPageSize());
        resPage.setCurrentPageUri(currentPageUrl);

        if (currentPage > 1) {
            String prevPage = buildPageUrl(collectionUrl, currentPage - 1, searchQuery.getPageSize());
            resPage.setPrevPageUri(prevPage);
        }

        // if current page is not the last one
        boolean isLastPage = resPage.getTotalInCollection() <= currentPage * searchQuery.getPageSize();
        if (!isLastPage) {
            String nextPage = buildPageUrl(collectionUrl, currentPage + 1, searchQuery.getPageSize());
            resPage.setNextPageUri(nextPage);
        }

        return resPage;
    }

    private String buildPageUrl(String collectionUrl, int page, int pageSize) throws EuropeanaI18nApiException {
        try {
            URIBuilder builder = new URIBuilder(collectionUrl)
                    .addParameter(CommonApiConstants.QUERY_PARAM_PAGE, String.valueOf(page))
                    .addParameter( CommonApiConstants.QUERY_PARAM_PAGE_SIZE, String.valueOf(pageSize));

            return builder.build().toString();
        } catch (URISyntaxException e) {
            throw new EuropeanaI18nApiException("Error creating Page  Urls " +e.getMessage(), null, null, HttpStatus.INTERNAL_SERVER_ERROR, null, null);
        }
    }

    private String buildCollectionUrl(Query searchQuery, StringBuffer requestUrl, String queryString) {
        // remove out of scope parameters
        if (StringUtils.isNotEmpty(queryString)) {
            queryString = removeParam(OAuthUtils.PARAM_WSKEY,queryString);
            queryString = removeParam(CommonApiConstants.QUERY_PARAM_PAGE, queryString);
            queryString = removeParam(CommonApiConstants.QUERY_PARAM_PAGE_SIZE, queryString);

            // avoid duplication of query parameters
            queryString = removeParam(CommonApiConstants.QUERY_PARAM_PROFILE, queryString);
        }

        // add mandatory parameters
        if (StringUtils.isNotBlank(searchQuery.getSearchProfile())) {
            queryString += ("&" + CommonApiConstants.QUERY_PARAM_PROFILE + "=" + searchQuery.getSearchProfile());
        }

        if (StringUtils.isNotEmpty(queryString)) {
            return requestUrl.append("?").append(queryString).toString();
        }
        return requestUrl.toString();
    }

    protected String removeParam(final String queryParam, String queryParams) {
        String tmp;
        // avoid name conflicts search "queryParam="
        int startPos = queryParams.indexOf(queryParam + "=");
        int startEndPos = queryParams.indexOf("&", startPos + 1);

        if (startPos >= 0) {
            // make sure to remove the "&" if not the first param
            if (startPos > 0)
                startPos--;
            tmp = queryParams.substring(0, startPos);

            if (startEndPos > 0)
                tmp += queryParams.substring(startEndPos);
        } else {
            tmp = queryParams;
        }
        return tmp;
    }


    @Override
    public List<String> searchEntityIds(Query searchQuery, String scope, List<EntityTypes> entityTypes) throws EuropeanaApiException {
        List<String> matchingEntityIds = new ArrayList<>();
        ResultSet<? extends Entity> results = search(searchQuery, null, entityTypes, scope);
        for (Entity searchRes : results.getResults()) {
            matchingEntityIds.add(searchRes.getEntityId());
        }
        return matchingEntityIds;
    }


    /**
     * Get entity type string list from comma separated entities string.
     *
     * @param commaSepEntityTypes Comma separated entities string
     * @return Entity types string list
     * @throws InvalidParamException
     */
    @Override
    public List<EntityTypes> getEntityTypesFromString(String commaSepEntityTypes) throws EuropeanaI18nApiException {
        try {
            if (commaSepEntityTypes == null) {
                return null;
            }

            String[] splittedEntityTypes = commaSepEntityTypes.split(",");
            List<EntityTypes> entityTypes = new ArrayList<EntityTypes>();

            EntityTypes entityType = null;
            String typeAsString = null;

            for (int i = 0; i < splittedEntityTypes.length; i++) {
                typeAsString = splittedEntityTypes[i].trim();
                entityType = EntityTypes.getByInternalType(typeAsString);
                entityTypes.add(entityType);
            }

            return entityTypes;
        } catch (UnsupportedEntityTypeException e) {
            throw new EuropeanaI18nApiException(null, null, null,
                    HttpStatus.BAD_REQUEST,
                    I18nConstants.UNSUPPORTED_ENTITY_TYPE,
                    Arrays.asList( WebEntityConstants.ENTITY_API_RESOURCE, commaSepEntityTypes),
                    e);
        }
    }
}
