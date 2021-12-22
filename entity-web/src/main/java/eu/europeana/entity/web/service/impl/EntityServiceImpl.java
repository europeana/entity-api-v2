package eu.europeana.entity.web.service.impl;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import eu.europeana.api.commons.definitions.search.Query;
import eu.europeana.api.commons.definitions.search.ResultSet;
import eu.europeana.api.commons.definitions.search.result.ResultsPage;
import eu.europeana.api.commons.definitions.search.result.impl.ResultsPageImpl;
import eu.europeana.api.commons.definitions.vocabulary.CommonApiConstants;
import eu.europeana.api.commons.web.exception.HttpException;
import eu.europeana.entity.app.I18nConstants;
import eu.europeana.entity.config.AppConfigConstants;
import eu.europeana.entity.definitions.exceptions.UnsupportedEntityTypeException;
import eu.europeana.entity.definitions.model.Entity;
import eu.europeana.entity.definitions.model.vocabulary.EntityTypes;
import eu.europeana.entity.definitions.model.vocabulary.SuggestAlgorithmTypes;
import eu.europeana.entity.definitions.model.vocabulary.WebEntityConstants;
import eu.europeana.entity.solr.exception.EntityRetrievalException;
import eu.europeana.entity.solr.exception.EntitySuggestionException;
import eu.europeana.entity.solr.service.SolrEntityService;
import eu.europeana.entity.web.exception.InternalServerException;
import eu.europeana.entity.web.exception.ParamValidationException;
import eu.europeana.entity.web.model.view.EntityPreview;
import eu.europeana.entity.web.service.EntityService;

/**
 * @author GrafR
 *
 */
@Service(AppConfigConstants.BEAN_ENTITY_SERVICE)
public class EntityServiceImpl extends BaseEntityServiceImpl implements EntityService {

    public final String BASE_URL_DATA = "http://data.europeana.eu/";

    @Resource(name = AppConfigConstants.ENTITY_SOLR_SERVICE)
    SolrEntityService solrEntityService;

    @Override
    public Entity retrieveByUrl(String type, String identifier) throws HttpException {

	StringBuilder stringBuilder = new StringBuilder();

	stringBuilder.append(BASE_URL_DATA);
	if (StringUtils.isNotEmpty(type))
	    stringBuilder.append(type.toLowerCase() + "/");
	if (StringUtils.isNotEmpty(identifier))
	    stringBuilder.append(identifier);

	String entityUri = stringBuilder.toString();
	Entity result;
	try {
	    result = solrEntityService.searchByUrl(type, entityUri);
	} catch (EntityRetrievalException e) {
	    throw new HttpException(e.getMessage(), I18nConstants.SERVER_ERROR_CANT_RETRIEVE_URI,
		    new String[] { entityUri }, HttpStatus.INTERNAL_SERVER_ERROR, e);
	} catch (UnsupportedEntityTypeException e) {
	    throw new HttpException(null, I18nConstants.UNSUPPORTED_ENTITY_TYPE, new String[] { 
		    WebEntityConstants.ENTITY_API_RESOURCE, type },
		    HttpStatus.NOT_FOUND, null);
	}
	// if not found send appropriate error message
	if (result == null)
	    throw new HttpException(null, I18nConstants.RESOURCE_NOT_FOUND, new String[] { 
		    WebEntityConstants.ENTITY_API_RESOURCE, entityUri },
		    HttpStatus.NOT_FOUND, null);

	return result;
    }


    /*
     * (non-Javadoc)
     * 
     * @see eu.europeana.entity.web.service.EntityService#
     */
    @Override
    public ResultSet<? extends EntityPreview> suggest(String text, String[] language, List<EntityTypes> entityTypes,
	    String scope, String namespace, int rows, SuggestAlgorithmTypes algorithm)
	    throws InternalServerException, ParamValidationException {

//	Query query = null;
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
		throw new ParamValidationException(WebEntityConstants.ALGORITHM, "" + algorithm);

	    }
	} catch (EntitySuggestionException e) {
	    throw new InternalServerException(e);
	}

	return res;
    }

    @Override
    public String resolveByUri(String uri) throws HttpException {
	String result;
	try {
	    result = solrEntityService.searchByCoref(uri);
	} catch (EntityRetrievalException e) {
	    throw new HttpException(e.getMessage(), I18nConstants.SERVER_ERROR_CANT_RESOLVE_SAME_AS_URI,
		    new String[] { uri }, HttpStatus.INTERNAL_SERVER_ERROR);
	}
	// if not found send appropriate error message
	if (result == null)
	    throw new HttpException(null, I18nConstants.CANT_FIND_BY_SAME_AS_URI, new String[] { 
		    WebEntityConstants.ENTITY_API_RESOURCE, uri },
		    HttpStatus.NOT_FOUND);

	return result;
    }

    @Override
    public ResultSet<? extends Entity> search(Query query, String[] outLanguage, List<EntityTypes> entityTypes,
	    String scope) throws HttpException {

	return solrEntityService.search(query, outLanguage, entityTypes, scope);
    }

    /**
     * @param entityTypes
     * @param suggest
     * @throws ParamValidationException
     */
    public List<EntityTypes> validateEntityTypes(List<EntityTypes> entityTypes, boolean suggest) throws ParamValidationException {
	// search
	if (!suggest) {
	    if (isEmptyOrAll(entityTypes)) {
		//no filtering needed
		return null;
	    }
	} else {// suggest

	    if (isEmptyOrAll(entityTypes)) {
		if (entityTypes == null)
		    entityTypes = new ArrayList<EntityTypes>(); 
		entityTypes.clear();
		entityTypes.add(EntityTypes.Concept);
		entityTypes.add(EntityTypes.Agent);
		entityTypes.add(EntityTypes.Place);
		entityTypes.add(EntityTypes.Organization);
		entityTypes.add(EntityTypes.TimeSpan);
	    }
	    
	}
	
	return entityTypes;
    }

    private boolean isEmptyOrAll(List<EntityTypes> entityTypes) {
	return entityTypes == null || entityTypes.isEmpty() || entityTypes.contains(EntityTypes.All);
    }

    // TODO: consider usage of a helper class for helper methods
    public <T extends Entity> ResultsPage<T> buildResultsPage(Query searchQuery, ResultSet<T> results,
	    StringBuffer requestUrl, String reqParams) {
	@SuppressWarnings({ "rawtypes", "unchecked" })
	ResultsPage<T> resPage = new ResultsPageImpl();

	resPage.setItems(results.getResults());
	resPage.setFacetFields(results.getFacetFields());

	resPage.setTotalInPage(results.getResults().size());
	resPage.setTotalInCollection(results.getResultSize());

	String collectionUrl = buildCollectionUrl(searchQuery, requestUrl, reqParams);
	resPage.setCollectionUri(collectionUrl);

	int currentPage = searchQuery.getPageNr();
	String currentPageUrl = buildPageUrl(collectionUrl, currentPage, searchQuery.getPageSize());
	resPage.setCurrentPageUri(currentPageUrl);

	if (currentPage > 0) {
	    String prevPage = buildPageUrl(collectionUrl, currentPage - 1, searchQuery.getPageSize());
	    resPage.setPrevPageUri(prevPage);
	}

	// if current page is not the last one
	boolean isLastPage = resPage.getTotalInCollection() <= (currentPage + 1) * searchQuery.getPageSize();
	if (!isLastPage) {
	    String nextPage = buildPageUrl(collectionUrl, currentPage + 1, searchQuery.getPageSize());
	    resPage.setNextPageUri(nextPage);
	}

	return resPage;
    }

    private String buildPageUrl(String collectionUrl, int page, int pageSize) {
	StringBuilder builder = new StringBuilder(collectionUrl);
	builder.append("&").append(CommonApiConstants.QUERY_PARAM_PAGE).append("=").append(page);

	builder.append("&").append(CommonApiConstants.QUERY_PARAM_PAGE_SIZE).append("=").append(pageSize);

	return builder.toString();
    }

    private String buildCollectionUrl(Query searchQuery, StringBuffer requestUrl, String queryString) {

	// queryString = removeParam(WebAnnotationFields.PARAM_WSKEY,
	// queryString);

	// remove out of scope parameters
	queryString = removeParam(CommonApiConstants.QUERY_PARAM_PAGE, queryString);
	queryString = removeParam(CommonApiConstants.QUERY_PARAM_PAGE_SIZE, queryString);

	// avoid duplication of query parameters
	queryString = removeParam(CommonApiConstants.QUERY_PARAM_PROFILE, queryString);

	// add mandatory parameters
	if (StringUtils.isNotBlank(searchQuery.getSearchProfile())) {
	    queryString += ("&" + CommonApiConstants.QUERY_PARAM_PROFILE + "=" + searchQuery.getSearchProfile());
	}

	return requestUrl.append("?").append(queryString).toString();
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
    public List<String> searchEntityIds(Query searchQuery, String scope, List<EntityTypes> entityTypes)
	    throws HttpException {

	List<String> matchingEntityIds = new ArrayList<String>();
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
     * @throws UnsupportedEntityTypeException
     * @throws ParamValidationException
     */
    @Override
    public List<EntityTypes> getEntityTypesFromString(String commaSepEntityTypes)
	    throws UnsupportedEntityTypeException {

	if(StringUtils.isBlank(commaSepEntityTypes)) {
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
    }
}
