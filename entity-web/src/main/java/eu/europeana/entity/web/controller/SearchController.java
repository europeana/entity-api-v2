package eu.europeana.entity.web.controller;

import java.util.List;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import com.fasterxml.jackson.core.JsonProcessingException;
import eu.europeana.api.common.config.swagger.SwaggerSelect;
import eu.europeana.api.commons.definitions.search.Query;
import eu.europeana.api.commons.definitions.search.ResultSet;
import eu.europeana.api.commons.definitions.search.result.ResultsPage;
import eu.europeana.api.commons.definitions.vocabulary.CommonApiConstants;
import eu.europeana.api.commons.web.exception.HttpException;
import eu.europeana.api.commons.web.http.HttpHeaders;
import eu.europeana.entity.app.I18nConstants;
import eu.europeana.entity.config.AppConfigConstants;
import eu.europeana.entity.definitions.exceptions.UnsupportedEntityTypeException;
import eu.europeana.entity.definitions.model.Entity;
import eu.europeana.entity.definitions.model.search.SearchProfiles;
import eu.europeana.entity.definitions.model.vocabulary.EntityTypes;
import eu.europeana.entity.definitions.model.vocabulary.SuggestAlgorithmTypes;
import eu.europeana.entity.definitions.model.vocabulary.WebEntityConstants;
import eu.europeana.entity.solr.exception.EntityRetrievalException;
import eu.europeana.entity.solr.exception.InvalidSearchQueryException;
import eu.europeana.entity.solr.service.impl.EntityQueryBuilder;
import eu.europeana.entity.utils.EntityUtils;
import eu.europeana.entity.web.config.EntityWebConfig;
import eu.europeana.entity.web.exception.InternalServerException;
import eu.europeana.entity.web.exception.ParamValidationException;
import eu.europeana.entity.web.jsonld.SuggestionSetSerializer;
import eu.europeana.entity.web.model.view.EntityPreview;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Controller
@Api(tags = "Discovery API")
@SwaggerSelect
public class SearchController extends BaseRest {
  
    @Resource(name = AppConfigConstants.BEAN_WEB_CONFIG)
    private EntityWebConfig entityWebConfig;

    @ApiOperation(value = "Suggest entities for the given text query. Suported values for type: Agent, Place, Concept, Timespan, All. Supported values for scope: europeana. Supported values for algorithm: monolingual (default), suggestByLabel", nickname = "getSuggestion", response = java.lang.Void.class)
    @RequestMapping(value = { "/entity/suggest", "/entity/suggest.json",  "/entity/suggest.jsonld" }, method = RequestMethod.GET, produces = {
	    HttpHeaders.CONTENT_TYPE_JSONLD_UTF8, HttpHeaders.CONTENT_TYPE_JSON_UTF8 })
    public ResponseEntity<String> getSuggestion(
	    @RequestParam(value = CommonApiConstants.PARAM_WSKEY, required = false) String wskey,
	    @RequestParam(value = CommonApiConstants.QUERY_PARAM_TEXT) String text,
	    @RequestParam(value = CommonApiConstants.QUERY_PARAM_LANGUAGE, defaultValue = WebEntityConstants.PARAM_LANGUAGE_EN) String language,
	    @RequestParam(value = WebEntityConstants.QUERY_PARAM_SCOPE, required = false) String scope,
	    @RequestParam(value = WebEntityConstants.QUERY_PARAM_TYPE, defaultValue = WebEntityConstants.PARAM_TYPE_ALL) String type,
	    @RequestParam(value = CommonApiConstants.QUERY_PARAM_ROWS, defaultValue = WebEntityConstants.PARAM_DEFAULT_ROWS) int rows,
	    @RequestParam(value = WebEntityConstants.ALGORITHM, required = false, defaultValue = WebEntityConstants.SUGGEST_MONOLINGUAL) String algorithm,
	    HttpServletRequest request)
	    throws HttpException {

	try {
	    // Check client access (a valid “wskey” must be provided)
		if (isAuthEnabled(webConfig.getApiKeyServiceUrl())) {
			verifyReadAccess(request);
		}

	    // validate algorithm parameter
	    SuggestAlgorithmTypes suggestType = validateAlgorithmParam(algorithm);

	    // validate text parameter
	    String validatedText = preProcessQuery(text);

	    EntityQueryBuilder queryBuilder = new EntityQueryBuilder();
	    
	    // validate and convert type
	    List<EntityTypes> entityTypes = getEntityService().getEntityTypesFromString(type);
	    entityTypes = getEntityService().validateEntityTypes(entityTypes, true);

	    // validate scope parameter
	    validateScopeParam(scope);

	    // parse language list
	    String[] requestedLanguages = queryBuilder.toArray(language);

	    // perform search
     	    ResultSet<? extends EntityPreview> results = getEntityService().suggest(validatedText, requestedLanguages, entityTypes,
		    scope, null, rows, suggestType);

	    // serialize results
	    SuggestionSetSerializer serializer = new SuggestionSetSerializer(results, entityWebConfig.getEntityDataEndpoint());
	    String jsonLd = serializer.serialize();

	    // build response
	    MultiValueMap<String, String> headers = new LinkedMultiValueMap<String, String>(5);
	    // removed in #EA-763 and specifications
	    // //headers.add(HttpHeaders.VARY, HttpHeaders.ACCEPT);
	    headers.add(HttpHeaders.ALLOW, HttpHeaders.ALLOW_GET);

	    ResponseEntity<String> response = new ResponseEntity<String>(jsonLd, headers, HttpStatus.OK);

	    return response;

	} catch (HttpException e) {
	    // avoid wrapping http exception
	    throw e;
	} catch (RuntimeException e) {
	    // not found ..
	    // System.out.println(e);
	    throw new InternalServerException(e);
	} catch (Exception e) {
	    throw new InternalServerException(e);
	}
    }

    
    @ApiOperation(value = "Search entities for the given text query. By default the search will return all entity fields. "
	    + "The facets profile and the facet param are available for including facets in the response. fl and lang params are used to reduce the amount of data included in the response", nickname = "search", response = java.lang.Void.class)
    @RequestMapping(value = { "/entity/search", "/entity/search.json", "/entity/search.jsonld" }, method = RequestMethod.GET, produces = {
	    HttpHeaders.CONTENT_TYPE_JSONLD_UTF8, HttpHeaders.CONTENT_TYPE_JSON_UTF8, })
    public ResponseEntity<String> search(
	    @RequestParam(value = CommonApiConstants.PARAM_WSKEY, required = false) String wskey,
	    @RequestParam(value = CommonApiConstants.QUERY_PARAM_QUERY) String queryString,
	    @RequestParam(value = CommonApiConstants.QUERY_PARAM_QF, required = false) String[] qf,
	    @RequestParam(value = WebEntityConstants.QUERY_PARAM_FL, required = false) String fl,
	    @RequestParam(value = CommonApiConstants.QUERY_PARAM_FACET, required = false) String facet,
	    @RequestParam(value = CommonApiConstants.QUERY_PARAM_LANG, required = false) String outLanguage,
	    @RequestParam(value = WebEntityConstants.QUERY_PARAM_TYPE, required = false, defaultValue = WebEntityConstants.PARAM_TYPE_ALL) String type,
	    @RequestParam(value = WebEntityConstants.QUERY_PARAM_SCOPE, required = false) String scope,
	    @RequestParam(value = CommonApiConstants.QUERY_PARAM_SORT, required = false) String sort,
	    @RequestParam(value = CommonApiConstants.QUERY_PARAM_PAGE, required = false, defaultValue = "0") int page,
	    @RequestParam(value = CommonApiConstants.QUERY_PARAM_PAGE_SIZE, required = false, defaultValue = ""
		    + Query.DEFAULT_PAGE_SIZE) int pageSize,
	    @RequestParam(value = CommonApiConstants.QUERY_PARAM_PROFILE, required = false) String profile,
	    HttpServletRequest request) throws HttpException {
        
        try {
        	if (isAuthEnabled(webConfig.getApiKeyServiceUrl())) {
				verifyReadAccess(request);
			}
	    // ** Process input params
	    if (StringUtils.isBlank(queryString))
		throw new ParamValidationException(I18nConstants.EMPTY_PARAM_MANDATORY,
			CommonApiConstants.QUERY_PARAM_QUERY, queryString);

	    // process scope
	    scope = validateScopeParam(scope);

	    // process type
	    EntityQueryBuilder queryBuilder = new EntityQueryBuilder();
	    List<EntityTypes> entityTypes;
	    entityTypes = getEntityService().getEntityTypesFromString(type);
	    entityTypes = getEntityService().validateEntityTypes(entityTypes, false);

	    // process lang
	    String[] preferredLanguages = null;
	    if (outLanguage != null && !outLanguage.contains(WebEntityConstants.PARAM_LANGUAGE_ALL))
		preferredLanguages = queryBuilder.toArray(outLanguage);

	    // process profile
	    SearchProfiles searchProfile = null;
	    if (profile != null) {
		if (!SearchProfiles.contains(profile))
		    throw new ParamValidationException(CommonApiConstants.QUERY_PARAM_PROFILE, profile);
		else
		    searchProfile = SearchProfiles.valueOf(profile.toLowerCase());
	    }

	    // process fl
	    String[] retFields = queryBuilder.toArray(fl);

	    // process facet
	    String[] facets = queryBuilder.toArray(facet);

	    // process sort param, convert multiple sort criteria to string array 
	    String[] sortCriteria = queryBuilder.toArray(sort); 
	    
	    // perform search
	    Query searchQuery = queryBuilder.buildSearchQuery(queryString, qf, facets, sortCriteria, page, pageSize,
		    searchProfile, retFields);
	    ResultSet<? extends Entity> results = getEntityService().search(searchQuery, preferredLanguages, entityTypes,
		    scope);

	    ResultsPage<? extends Entity> resPage = getEntityService().buildResultsPage(searchQuery, results, request);
	    String jsonLd = serializeResultsPage(resPage, searchProfile, entityWebConfig.getEntityDataEndpoint());

	    // build response
	    MultiValueMap<String, String> headers = new LinkedMultiValueMap<String, String>(5);
	    // removed in #EA-763 and specifications
	    // //headers.add(HttpHeaders.VARY, HttpHeaders.ACCEPT);
	    headers.add(HttpHeaders.ALLOW, HttpHeaders.ALLOW_GET);

	    ResponseEntity<String> response = new ResponseEntity<String>(jsonLd, headers, HttpStatus.OK);

	    return response;

	} catch (UnsupportedEntityTypeException e) {
		 throw new ParamValidationException(I18nConstants.INVALID_PARAM_VALUE, WebEntityConstants.QUERY_PARAM_TYPE,
			 type);
	} catch (InvalidSearchQueryException e) {
	    throw new ParamValidationException(I18nConstants.INVALID_PARAM_VALUE, CommonApiConstants.QUERY_PARAM_QUERY,
		    e.getMessage());
	} catch (EntityRetrievalException e) {
	    throw new InternalServerException(e.getMessage(), e);
	} catch (HttpException e) {
	    // avoid wrapping http exception
	    throw e;
	} catch (JsonProcessingException e) {
	    // not found ..
	    // System.out.println(e);
	    throw new InternalServerException(e);
	} catch (RuntimeException e) {
	    // not found ..
	    // System.out.println(e);
	    throw new InternalServerException(e);
	}
	
    }

	@ApiOperation(value = "Performs a text based lookup for entities to inform enrichment services on Metis. " +
			"Suported values for type: Agent, Place, Concept, Timespan, All. Supported values for scope: europeana",
			nickname = "enrichEntity", response = java.lang.Void.class)
	@RequestMapping(value = { "/entity/enrich"}, method = RequestMethod.GET, produces = {
			HttpHeaders.CONTENT_TYPE_JSONLD_UTF8, HttpHeaders.CONTENT_TYPE_JSON_UTF8 })
	public ResponseEntity<String> enrichEntity(
			@RequestParam(value = CommonApiConstants.PARAM_WSKEY, required = false) String wskey,
			@RequestParam(value = CommonApiConstants.QUERY_PARAM_TEXT) String text,
			@RequestParam(value = CommonApiConstants.QUERY_PARAM_LANG, required = false) String lang,
			@RequestParam(value = WebEntityConstants.QUERY_PARAM_TYPE, required = false) String type,
			@RequestParam(value = CommonApiConstants.QUERY_PARAM_ROWS, defaultValue = WebEntityConstants.PARAM_DEFAULT_ROWS) int rows,
			HttpServletRequest request)
			throws HttpException {
		try {
			if (isAuthEnabled(webConfig.getApiKeyServiceUrl())) {
				verifyReadAccess(request);
			}

			// validate text parameter
			if (StringUtils.isBlank(text))
				throw new ParamValidationException(I18nConstants.EMPTY_PARAM_MANDATORY,
						CommonApiConstants.QUERY_PARAM_QUERY, text);

			// escape the quotes
			String validatedText = EntityUtils.escapeBackslashAndQuotes(text, WebEntityConstants.BACKSLASH, WebEntityConstants.QUOTE);

			// validate language
			validateLanguage(lang);

			// validate type
			List<EntityTypes> entityTypes = getEntityService().getEntityTypesFromString(type);

			// build query
			EntityQueryBuilder queryBuilder = new EntityQueryBuilder();
			Query searchQuery = queryBuilder.buildSearchQueryForEnrichment(validatedText, lang, entityTypes, rows);

			// perform search
			ResultSet<? extends Entity> results = getEntityService().search(searchQuery, null, null,
					null);

                        ResultsPage<? extends Entity> resPage = getEntityService().buildResultsPage(searchQuery, results, request);
			String jsonLd = serializeResultsPage(resPage, null, entityWebConfig.getEntityDataEndpoint());

			// build response
			MultiValueMap<String, String> headers = new LinkedMultiValueMap<>(5);
			headers.add(HttpHeaders.ALLOW, HttpHeaders.ALLOW_GET);
			ResponseEntity<String> response = new ResponseEntity<>(jsonLd, headers, HttpStatus.OK);

			return response;
		} catch (UnsupportedEntityTypeException e) {
			throw new ParamValidationException(I18nConstants.UNSUPPORTED_ENTITY_TYPE, WebEntityConstants.ENTITY_API_RESOURCE,
					e.getMessage());
		} catch (JsonProcessingException e) {
			throw new InternalServerException(e);
		} catch (RuntimeException e) {
			throw new InternalServerException(e);
		}
	}
}
