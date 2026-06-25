package eu.europeana.entity.web.controller;

import java.util.Arrays;
import java.util.List;

import eu.europeana.api.commons_sb3.definitions.search.enrich.EnrichRequest;
import eu.europeana.api.commons_sb3.definitions.search.result.ResultsPage;
import eu.europeana.api.commons_sb3.error.exceptions.InvalidParamException;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;

import eu.europeana.api.commons_sb3.definitions.search.Query;
import eu.europeana.api.commons_sb3.definitions.search.ResultSet;
import eu.europeana.api.commons_sb3.definitions.vocabulary.CommonApiConstants;
import eu.europeana.api.commons_sb3.error.EuropeanaApiException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import eu.europeana.entity.config.AppConfigConstants;
import eu.europeana.entity.definitions.model.Entity;
import eu.europeana.entity.definitions.model.search.SearchProfiles;
import eu.europeana.entity.definitions.model.vocabulary.EntityTypes;
import eu.europeana.entity.definitions.model.vocabulary.SuggestAlgorithmTypes;
import eu.europeana.entity.definitions.model.vocabulary.WebEntityConstants;
import eu.europeana.entity.solr.exception.InvalidSearchQueryException;
import eu.europeana.entity.solr.service.impl.EntityQueryBuilder;
import eu.europeana.entity.utils.EntityUtils;
import eu.europeana.entity.web.config.EntityWebConfig;
import eu.europeana.entity.web.jsonld.SuggestionSetSerializer;
import eu.europeana.entity.web.model.view.EntityPreview;

import static eu.europeana.api.commons_sb3.definitions.http.HttpHeaders.*;

@Controller
public class SearchController extends BaseRest {
  
    @Resource(name = AppConfigConstants.BEAN_WEB_CONFIG)
    private EntityWebConfig entityWebConfig;

    @RequestMapping(value = { "/entity/suggest", "/entity/suggest.json",  "/entity/suggest.jsonld" }, method = RequestMethod.GET, produces = {
	    CONTENT_TYPE_JSONLD_UTF8, CONTENT_TYPE_JSON_UTF8 })
    public ResponseEntity<String> getSuggestion(
	    @RequestParam(value = CommonApiConstants.QUERY_PARAM_TEXT) String text,
	    @RequestParam(value = CommonApiConstants.QUERY_PARAM_LANGUAGE, defaultValue = WebEntityConstants.PARAM_LANGUAGE_EN) String language,
	    @RequestParam(value = WebEntityConstants.QUERY_PARAM_SCOPE, required = false) String scope,
	    @RequestParam(value = WebEntityConstants.QUERY_PARAM_TYPE, defaultValue = WebEntityConstants.PARAM_TYPE_ALL) String type,
	    @RequestParam(value = CommonApiConstants.QUERY_PARAM_ROWS, defaultValue = WebEntityConstants.PARAM_DEFAULT_ROWS) int rows,
	    @RequestParam(value = WebEntityConstants.ALGORITHM, required = false, defaultValue = WebEntityConstants.SUGGEST_MONOLINGUAL) String algorithm,
	    HttpServletRequest request) throws EuropeanaApiException {
		Authentication auth = null;
		if (isAuthEnabled(webConfig.getApiKeyServiceUrl())) {
		 auth =	verifyReadAccess(request);
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
	    MultiValueMap<String, String> headers = new LinkedMultiValueMap<>(5);
		addRateLimitHeaders(headers, auth);

	    // removed in #EA-763 and specifications
	    // //headers.add(HttpHeaders.VARY, HttpHeaders.ACCEPT);
	    headers.add(ALLOW, ALLOW_GET);

	    ResponseEntity<String> response = new ResponseEntity<>(jsonLd, headers, HttpStatus.OK);
	    return response;
	}

    
    @RequestMapping(value = { "/entity/search", "/entity/search.json", "/entity/search.jsonld" }, method = RequestMethod.GET, produces = {
	    CONTENT_TYPE_JSONLD_UTF8, CONTENT_TYPE_JSON_UTF8, })
    public ResponseEntity<String> search(
	    @RequestParam(value = CommonApiConstants.QUERY_PARAM_QUERY) String queryString,
	    @RequestParam(value = CommonApiConstants.QUERY_PARAM_QF, required = false) String[] qf,
	    @RequestParam(value = WebEntityConstants.QUERY_PARAM_FL, required = false) String fl,
	    @RequestParam(value = CommonApiConstants.QUERY_PARAM_FACET, required = false) String facet,
	    @RequestParam(value = CommonApiConstants.QUERY_PARAM_LANG, required = false) String outLanguage,
	    @RequestParam(value = WebEntityConstants.QUERY_PARAM_TYPE, required = false, defaultValue = WebEntityConstants.PARAM_TYPE_ALL) String type,
	    @RequestParam(value = WebEntityConstants.QUERY_PARAM_SCOPE, required = false) String scope,
	    @RequestParam(value = CommonApiConstants.QUERY_PARAM_SORT, required = false) String sort,
	    @RequestParam(value = CommonApiConstants.QUERY_PARAM_PAGE, required = false, defaultValue = "1") int page,
	    @RequestParam(value = CommonApiConstants.QUERY_PARAM_PAGE_SIZE, required = false, defaultValue = ""
		    + Query.DEFAULT_PAGE_SIZE) int pageSize,
	    @RequestParam(value = CommonApiConstants.QUERY_PARAM_PROFILE, required = false) String profile,
	    HttpServletRequest request) throws EuropeanaApiException {
        Authentication auth = null;
        try {
        	if (isAuthEnabled(webConfig.getApiKeyServiceUrl())) {
				auth = verifyReadAccess(request);
			}
	    // ** Process input params
	    if (StringUtils.isBlank(queryString))
		throw new InvalidParamException(Arrays.asList(CommonApiConstants.QUERY_PARAM_QUERY,
				"query should not be empty !!", queryString));

	    // process scope
	    scope = validateScopeParam(scope);
	    validatePageParam(page);

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
			if (!SearchProfiles.contains(profile)) {
				throw new InvalidParamException(Arrays.asList(CommonApiConstants.QUERY_PARAM_PROFILE,
						Arrays.asList(SearchProfiles.values()).toString(),
						profile));
			} else {
				searchProfile = SearchProfiles.valueOf(profile.toLowerCase());
			}
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
	    headers.add(ALLOW, ALLOW_GET);
		addRateLimitHeaders(headers, auth);

	    ResponseEntity<String> response = new ResponseEntity<String>(jsonLd, headers, HttpStatus.OK);

	    return response;
        } catch (InvalidSearchQueryException e){
				throw new InvalidParamException(Arrays.asList(CommonApiConstants.QUERY_PARAM_QUERY,
						"valid serach query", queryString));
		}
	}

	@RequestMapping(value = { "/entity/enrich"}, method = RequestMethod.GET, produces = {
			CONTENT_TYPE_JSONLD_UTF8, CONTENT_TYPE_JSON_UTF8 })
	public ResponseEntity<String> enrichEntity(
			@RequestParam(value = CommonApiConstants.QUERY_PARAM_TEXT) String text,
			@RequestParam(value = CommonApiConstants.QUERY_PARAM_LANG, required = false) String lang,
			@RequestParam(value = WebEntityConstants.QUERY_PARAM_TYPE, required = false) String type,
			@RequestParam(value = CommonApiConstants.QUERY_PARAM_ROWS, defaultValue = WebEntityConstants.PARAM_DEFAULT_ROWS) int rows,
			HttpServletRequest request)
			throws  EuropeanaApiException {
			if (isAuthEnabled(webConfig.getApiKeyServiceUrl())) {
				verifyReadAccess(request);
			}

			// validate text parameter
			if (StringUtils.isBlank(text))
				throw new InvalidParamException(Arrays.asList(CommonApiConstants.QUERY_PARAM_TEXT,
						"text should not be empty", text));

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
			MultiValueMap<String, String> headers = new LinkedMultiValueMap<>(EXPECTED_SIZE);
			headers.add(ALLOW, ALLOW_GET);
			ResponseEntity<String> response = new ResponseEntity<>(jsonLd, headers, HttpStatus.OK);

			return response;
	}


    @RequestMapping(value = { "/entity/enrich"}, method = RequestMethod.POST, produces = {
            CONTENT_TYPE_JSONLD_UTF8, CONTENT_TYPE_JSON_UTF8 })
    public ResponseEntity<String> enrichEntityPost(
			@RequestBody EnrichRequest enrichRequest, HttpServletRequest request) throws  EuropeanaApiException {
        if (isAuthEnabled(webConfig.getApiKeyServiceUrl())) {
            verifyReadAccess(request);
        }

        // validate mandotory and valid fields
		EntityTypes entityType = validateEntityType(enrichRequest.getType());
		validateEnrichQuery(enrichRequest.getQuery());
		validateRows(enrichRequest.getRows());

        // build query
        EntityQueryBuilder queryBuilder = new EntityQueryBuilder();
        Query searchQuery = queryBuilder.buildSearchQueryForEnrichment(enrichRequest.getQuery(), entityType, enrichRequest.getRows());

        // perform search
        ResultSet<? extends Entity> results = getEntityService().search(searchQuery, null, null,
                null);

        ResultsPage<? extends Entity> resPage = getEntityService().buildResultsPage(searchQuery, results, request);
        String jsonLd = serializeResultsPage(resPage, null, entityWebConfig.getEntityDataEndpoint());

        // build response
        MultiValueMap<String, String> headers = new LinkedMultiValueMap<>(EXPECTED_SIZE);
        headers.add(ALLOW, ALLOW_GET);
        ResponseEntity<String> response = new ResponseEntity<>(jsonLd, headers, HttpStatus.OK);

        return response;
    }
}
