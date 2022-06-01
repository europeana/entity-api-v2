package eu.europeana.entity.solr.service.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.annotation.Resource;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.BaseHttpSolrClient.RemoteSolrException;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.params.CommonParams;
import org.apache.solr.common.util.SimpleOrderedMap;
import org.springframework.stereotype.Service;

import eu.europeana.api.commons.definitions.search.Query;
import eu.europeana.api.commons.definitions.search.ResultSet;
import eu.europeana.entity.config.AppConfigConstants;
import eu.europeana.entity.definitions.exceptions.UnsupportedEntityTypeException;
import eu.europeana.entity.definitions.model.Entity;
import eu.europeana.entity.definitions.model.vocabulary.ConceptSolrFields;
import eu.europeana.entity.definitions.model.vocabulary.EntitySolrFields;
import eu.europeana.entity.definitions.model.vocabulary.EntityTypes;
import eu.europeana.entity.definitions.model.vocabulary.WebEntityConstants;
import eu.europeana.entity.solr.config.EntitySolrConfig;
import eu.europeana.entity.solr.exception.EntityRetrievalException;
import eu.europeana.entity.solr.exception.EntityRuntimeException;
import eu.europeana.entity.solr.exception.EntitySuggestionException;
import eu.europeana.entity.solr.exception.InvalidSearchQueryException;
import eu.europeana.entity.solr.model.factory.EntityObjectFactory;
import eu.europeana.entity.solr.model.vocabulary.SuggestionFields;
import eu.europeana.entity.solr.service.SolrEntityService;
import eu.europeana.entity.web.model.view.EntityPreview;

@Service(AppConfigConstants.ENTITY_SOLR_SERVICE)
public class SolrEntityServiceImpl extends BaseEntityService implements SolrEntityService {

    @Resource(name = AppConfigConstants.ENTITY_SOLR_CLIENT)
    SolrClient solrClient;

    @Resource
    EntitySolrConfig entitySolrConfig;

    SuggestionUtils suggestionHelper = null;

    private final Logger log = LogManager.getLogger(getClass());

    public void setSolrServer(SolrClient solrClient) {
	this.solrClient = solrClient;
    }

    public void setEntityConfiguration(EntitySolrConfig entitySolrConfig) {
	this.entitySolrConfig = entitySolrConfig;
    }

    public Entity searchById(String entityId) throws EntityRetrievalException {
	return null;
    }

    @Override
    public Entity searchByUrl(String type, String entityId)
	    throws EntityRetrievalException, UnsupportedEntityTypeException {

	getLogger().debug("search entity (type:" + type + " ) by id: " + entityId);

	EntityTypes entityType = EntityTypes.getByInternalType(type);

	/**
	 * Construct a SolrQuery
	 */
	SolrQuery query = new SolrQuery();
	query.setQuery(ConceptSolrFields.ID + ":\"" + entityId + "\"");

	getLogger().trace("query: " + query);

	List<? extends Entity> beans = null;

	/**
	 * Query the server
	 */
	try {
	    QueryResponse rsp = solrClient.query(query);

	    Class<? extends Entity> concreteClass = null;
	    // String entityType = getTypeFromEntityId(entityId);
	    concreteClass = EntityObjectFactory.getInstance().getClassForType(entityType);

	    beans = rsp.getBeans(concreteClass);
	} catch (Exception e) {
	    //TODO: implement handling of bad requests
	    throw new EntityRetrievalException(
		    "Unexpected exception occured when searching entities for id: " + entityId, e);
	}

	if (beans == null || beans.size() == 0)
	    return null;
	if (beans.size() != 1)
	    throw new EntityRetrievalException("Expected one result but found: " + beans.size());

	return beans.get(0);
    }

    @Override
    public ResultSet<? extends Entity> search(Query searchQuery, String[] outLanguage, List<EntityTypes> entityTypes,
	    String scope) throws EntityRetrievalException {

	ResultSet<? extends Entity> res = null;
	SolrQuery query = (new EntityQueryBuilder()).toSolrQuery(searchQuery, SolrEntityService.HANDLER_SELECT,
		entityTypes, scope);

	try {
	    getLogger().debug("invoke suggest handler: " + SolrEntityService.HANDLER_SELECT);
	    getLogger().debug("search query: " + query);
	    QueryResponse rsp = solrClient.query(query);
	    res = buildResultSet(rsp, outLanguage);
	    getLogger().debug("search obj res size: " + res.getResultSize());
	} catch (RemoteSolrException e) {
	    RuntimeException ex = handleRemoteSolrException(searchQuery, e);
	    throw ex;
	} catch (IOException | SolrServerException | RuntimeException e) {
	    throw new EntityRetrievalException(
		    "An error occured exception occured when searching entities: " + searchQuery.toString() + "", e);
	}
	return res;
    }

    private RuntimeException handleRemoteSolrException(Query searchQuery, RemoteSolrException e) {
	String remoteMessage = e.getMessage();
	String UNDEFINED_FIELD = "undefined field";
	RuntimeException ex;
	if (remoteMessage.contains(UNDEFINED_FIELD)) {
	    // invalid search field
	    int startPos = remoteMessage.indexOf(UNDEFINED_FIELD) + UNDEFINED_FIELD.length();
	    String fieldName = remoteMessage.substring(startPos);
	    ex = new InvalidSearchQueryException(fieldName, e);
	} else {
	    int separatorPos = remoteMessage.lastIndexOf(':');
	    if (separatorPos > 0) {
		// remove server url from remote message
		remoteMessage = remoteMessage.substring(separatorPos + 1);
	    }
	    ex = new EntityRetrievalException("An error occured when searching entities: " + searchQuery.toString()
		    + ", remote message: " + remoteMessage, e);
	}
	return ex;
    }

    public Logger getLogger() {
	return log;
    }
   

    /*
     * (non-Javadoc)
     * 
     * @see
     * eu.europeana.entity.solr.service.SolrEntityService#suggestByLabel(java.lang.
     * String, java.lang.String[],
     * eu.europeana.entity.definitions.model.vocabulary.EntityTypes[],
     * java.lang.String, int)
     */
    @Override
    public ResultSet<? extends EntityPreview> suggestByLabel(String text, String[] requestedLanguages,
	    List<EntityTypes> entityTypes, String scope, int rows) throws EntitySuggestionException {

	SolrQuery solrQuery = new EntityQueryBuilder().buildSuggestByLabelQuery(text, entityTypes, scope, rows,
	        entitySolrConfig.getSuggesterSnippets(), null);

	return fetchSuggestions(text, requestedLanguages, rows, solrQuery);
    }
    
    @Override
    public ResultSet<? extends EntityPreview> suggestByLanguage(String text, String[] requestedLanguages,
	    List<EntityTypes> entityTypes, String scope, int rows) throws EntitySuggestionException {

	SolrQuery solrQuery = new EntityQueryBuilder().buildSuggesForLanguageQuery(text, entityTypes, scope, rows,
	        entitySolrConfig.getSuggesterSnippets(), Arrays.asList(requestedLanguages));

	return fetchSuggestions(text, requestedLanguages, rows, solrQuery);
    }

    private ResultSet<? extends EntityPreview> fetchSuggestions(String text, String[] requestedLanguages, int rows,
	    SolrQuery solrQuery) throws EntitySuggestionException {
	ResultSet<? extends EntityPreview> res = null;
	try {
	    getLogger().debug("invoke select handler: " + SolrEntityService.HANDLER_SELECT);
	    getLogger().debug("suggest text: " + text);
	    QueryResponse rsp = solrClient.query(solrQuery);

	    res = buildSuggestionSet(text, rsp, requestedLanguages, rows);
	    getLogger().debug("search obj res size: " + res.getResultSize());
	} catch (RuntimeException | SolrServerException | IOException e) {
	    throw new EntitySuggestionException(
		    "Unexpected exception occured when searching entities: " + solrQuery.toString(), e);
	}
	return res;
    }


    @SuppressWarnings({ "rawtypes", "unchecked" })
    protected <T extends EntityPreview> ResultSet<T> buildSuggestionSet(QueryResponse rsp, String[] requestedLanguages,
	    int rows) throws EntitySuggestionException {

	ResultSet<T> resultSet = new ResultSet<>();
	// resultSet.setLanguage(language);

	Map<String, Object> suggest = (Map<String, Object>) rsp.getResponse().get(SuggestionFields.SUGGEST);

	SimpleOrderedMap<?> suggestionsMap = (SimpleOrderedMap) suggest.get(SuggestionFields.PREFIX_SUGGEST_ENTITY);

	List<SimpleOrderedMap<?>> suggestions = null;

	String searchedTerm = "";

	if ((SimpleOrderedMap) suggestionsMap.getVal(0) != null) {
	    suggestions = (List<SimpleOrderedMap<?>>) ((SimpleOrderedMap) suggestionsMap.getVal(0))
		    .get(SuggestionFields.SUGGESTIONS);

	    searchedTerm = suggestionsMap.getName(0);
	}

	List<T> beans = extractBeans(searchedTerm, suggestions, rows, requestedLanguages);

	resultSet.setResults(beans);
	resultSet.setResultSize(beans.size());

	return resultSet;
    }

   
    /**
     * This method builds a preview for an entity object for use case
     * "suggestByLanguage"
     * 
     * @param searchedTerm
     * @param rsp
     * @param requestedLanguages
     * @param rows
     * @return entity preview
     * @throws EntitySuggestionException
     */
    @SuppressWarnings({ "unchecked" })
    protected <T extends EntityPreview> ResultSet<T> buildSuggestionSet(String searchedTerm,
	    QueryResponse rsp, String[] requestedLanguages, int rows) throws EntitySuggestionException {

	ResultSet<T> resultSet = new ResultSet<>();
	List<T> resultList = new ArrayList<T>();
	Map<String, Set<String>> highlightingResultMap = extractHighlightsMap(rsp);

	SolrDocumentList docList = rsp.getResults();
	if (docList != null) {
	    T preview;
	    String payload;
	    String id;
	    Set<String> highlights;

	    for (SolrDocument solrDocument : docList) {
		id = (String) solrDocument.getFieldValue(SuggestionFields.ID);
		highlights = highlightingResultMap.get(id);
		payload = (String) solrDocument.getFieldValue(SuggestionFields.PAYLOAD);
		preview = (T) getSuggestionHelper().parsePayload(payload, requestedLanguages, highlights);
		resultList.add(preview);
	    }
	}

	resultSet.setResults(resultList);
	resultSet.setResultSize(resultList.size());

	return resultSet;
    }

    /**
     * This method extracts highlight terms from response.
     * 
     * @param rsp
     * @return highlight terms
     */
    private Map<String, Set<String>> extractHighlightsMap(QueryResponse rsp) {
	Map<String, Set<String>> highlightingResultMap = new HashMap<String, Set<String>>();

	// retrieve highlighting mapping
	Map<String, Map<String, List<String>>> highlightingMap = rsp.getHighlighting();

	//
	for (Entry<String, Map<String, List<String>>> pair : highlightingMap.entrySet()) {
	    String id = pair.getKey();
	    Set<String> termSet = new HashSet<String>();
	    for (List<String> value : pair.getValue().values()) {
		for (String term : value) {
		    String extractedTerm = extractHighlightedTerm(term);
		    termSet.add(extractedTerm);
		}
		if(!termSet.isEmpty()) {
		    //on multiple highlighted fields only the one mathced has an entry
		    highlightingResultMap.put(id, termSet);
		}
	    }
	}
	return highlightingResultMap;
    }

    /**
     * This method extracts highlight term from response string.
     * 
     * @param highlight
     *            The term extracted from payload
     * @param searchedTerm
     *            The searched term
     * @return highlight term
     * @throws EntitySuggestionException
     */
    private String getHighlightTerm(String highlight, String searchedTerm) throws EntitySuggestionException {
	String highlightTerm;
	if (highlight == null) {
	    throw new EntitySuggestionException(
		    "Suggesting error, no term found in search response for searched term: " + searchedTerm);
	} else if (!highlight.contains(WebEntityConstants.HIGHLIGHT_START_MARKER)) {
	    // highlighter doesn't work, use the searched term for language logic
	    highlightTerm = searchedTerm.toLowerCase();
	} else {
	    highlightTerm = extractHighlightedTerm(highlight);
	}
	return highlightTerm;
    }

    /**
     * This method extracts highlight core term from html syntax
     * 
     * @param highlight
     * @return highlight core term
     */
    private String extractHighlightedTerm(String highlight) {
	String highlightTerm;
	int beginHighlight = highlight.indexOf(WebEntityConstants.HIGHLIGHT_START_MARKER) + 3;
	int endHighlight = highlight.indexOf(WebEntityConstants.HIGHLIGHT_END_MARKER);
	highlightTerm = highlight.substring(beginHighlight, endHighlight);
	return highlightTerm;
    }

    private <T extends EntityPreview> List<T> extractBeans(String searchedTerm, List<SimpleOrderedMap<?>> suggestions,
	    int rows, String[] preferredLanguages) throws EntitySuggestionException {
	List<T> beans = new ArrayList<T>();

	// add exact matches to list
	if (suggestions != null)
	    processSuggestionMap(searchedTerm, suggestions, beans, rows, preferredLanguages);

	return beans;
    }

    private <T extends EntityPreview> void processSuggestionMap(String searchedTerm,
	    List<SimpleOrderedMap<?>> suggestionMap, List<T> beans, int rows, String[] preferredLanguages)
	    throws EntitySuggestionException {

	T preview;

	for (SimpleOrderedMap<?> entry : suggestionMap) {
	    // cut to rows
	    if (beans.size() == rows)
		return;

	    preview = buildEntityPreview(searchedTerm, entry, preferredLanguages);
	    beans.add(preview);
	}
    }

    /**
     * This method builds entity preview
     * 
     * @param searchedTerm
     * @param entry
     * @param preferredLanguages
     * @return entity preview
     * @throws EntitySuggestionException
     */
    @SuppressWarnings("unchecked")
    private <T extends EntityPreview> T buildEntityPreview(String searchedTerm, SimpleOrderedMap<?> entry,
	    String[] preferredLanguages) throws EntitySuggestionException {
	String highlight;
	String payload;
	T preview;
	String highlightTerm;
	Set<String> highlightTerms;

	highlight = (String) entry.get(SuggestionFields.TERM);
	highlightTerm = getHighlightTerm(highlight, searchedTerm);

	payload = (String) entry.get(SuggestionFields.PAYLOAD);
	highlightTerms = new HashSet<>(Arrays.asList(highlightTerm));
	preview = (T) getSuggestionHelper().parsePayload(payload, preferredLanguages, highlightTerms);

	preview.setSearchedTerm(highlight);

	return preview;
    }

    public SuggestionUtils getSuggestionHelper() {
	if (suggestionHelper == null)
	    suggestionHelper = new SuggestionUtils();
	return suggestionHelper;
    }

    @Override
    public String searchByCoref(String uri) {

	getLogger().debug("search entity by coref uri: " + uri);

	/**
	 * Construct a SolrQuery
	 */
	SolrQuery query = new SolrQuery();
	query.setQuery(ConceptSolrFields.COREF + ":\"" + uri + "\"");
	query.set(CommonParams.SORT, EntitySolrFields.DERIVED_SCORE + " " + EntityQueryBuilder.DESC);
	query.addField(ConceptSolrFields.ID);

	try {
	    QueryResponse rsp = solrClient.query(query);
	    SolrDocumentList docs = rsp.getResults();

	    if (docs.getNumFound() == 0)
	      return null;

	    if (docs.getNumFound() == 1)
	      return "\""+docs.get(0).getFieldValue(ConceptSolrFields.ID).toString()+"\"";
	    // TODO: can this return >1 result? should it?
	    else if (docs.getNumFound() > 1) {
	      String multipleIds = "\""+docs.get(0).getFieldValue(ConceptSolrFields.ID).toString()+"\"";
	      for(int i=1;i<docs.getNumFound();i++) {
	        multipleIds += "," + "\""+docs.get(i).getFieldValue(ConceptSolrFields.ID).toString()+"\"";
	      }
	      return multipleIds;
	    }

	} catch (SolrServerException | IOException e) {
	    throw new EntityRuntimeException("Unexpected exception occured when searching Solr entities. ", e);
	}

	return null;
    }


//    /*
//     * (non-Javadoc)
//     * 
//     * @see
//     * eu.europeana.entity.solr.service.SolrEntityService#delete(java.lang.String)
//     */
//    @Override
//    public void delete(String entityUrl) throws EntityServiceException {
//	try {
//	    getLogger().debug("delete concept scheme with ID: " + entityUrl);
//	    UpdateResponse rsp = solrClient.deleteById(entityUrl);
//	    getLogger().trace("delete response: " + rsp.toString());
//	    solrClient.commit();
//	} catch (SolrServerException ex) {
//	    throw new EntityServiceException(
//		    "Unexpected solr server exception occured when deleting concept scheme for: " + entityUrl, ex);
//	} catch (IOException ex) {
//	    throw new EntityServiceException(
//		    "Unexpected IO exception occured when deleting concept scheme for: " + entityUrl, ex);
//	} catch (Throwable th) {
//	    throw new EntityServiceException(
//		    "Unexpected exception occured when deleting concept scheme for: " + entityUrl, th);
//	}
//    }

}
