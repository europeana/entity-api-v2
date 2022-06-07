package eu.europeana.entity.web.service;

import java.util.List;

import eu.europeana.api.commons.definitions.search.Query;
import eu.europeana.api.commons.definitions.search.ResultSet;
import eu.europeana.api.commons.definitions.search.result.ResultsPage;
import eu.europeana.api.commons.web.exception.HttpException;
import eu.europeana.entity.definitions.exceptions.UnsupportedEntityTypeException;
import eu.europeana.entity.definitions.model.Entity;
import eu.europeana.entity.definitions.model.vocabulary.EntityTypes;
import eu.europeana.entity.definitions.model.vocabulary.SuggestAlgorithmTypes;
import eu.europeana.entity.web.exception.ParamValidationException;
import eu.europeana.entity.web.model.view.EntityPreview;

public interface EntityService {

	Entity retrieveByUrl(String type, String identifier) throws HttpException;

	/**
	 * This method provides suggestions for auto-completion
	 * 
	 * @param text
	 * @param language
	 * @param entityType
	 * @param namespace
	 * @param rows
	 * @param algorithm The default algorithm is "suggest" but other types are possible
	 * @return
	 * @throws HttpException
	 * 
	 * e.g. GET /entity/suggest?text=leonard&language=en
	 */
	ResultSet<? extends EntityPreview> suggest(
			String text, String[] language, List<EntityTypes> entityTypes, String scope, String namespace, int rows, SuggestAlgorithmTypes algorithm) throws HttpException;


	/**
	 * This method searches the entities using the provided search query and specific filters
	 * @param query
	 * @param preferredLanguages
	 * @param entityTypes
	 * @param scope
	 * @return
	 * @throws HttpException
	 */
	public ResultSet<? extends Entity> search(Query query, String[] preferredLanguages, List<EntityTypes> entityTypes, String scope) throws HttpException;
	
	
	/**
	 * Performs a lookup for the entity in all 4 datasets:
	 * 
	 *    agents, places, concepts and time spans 
	 * 
	 * using an alternative uri for an entity (lookup will happen within the owl:sameAs properties).
	 * 
	 * @param uri
	 * @return a list of found entities or an exception if no entity is found
	 * @throws HttpException
	 */
	List<String> resolveByUri(String uri) throws HttpException;
	
	
	/**
	 * This method build the results page object for the search results retrieved with the given search query.
	 * @param searchQuery
	 * @param results
	 * @param requestUrl
	 * @param reqParams
	 * @return
	 */
	public <T extends Entity> ResultsPage<T> buildResultsPage(Query searchQuery, ResultSet<T> results,
			StringBuffer requestUrl, String reqParams);
	
		
	
	/**
	 * @param entityTypes
	 * @param suggest
	 * @return 
	 * @throws ParamValidationException
	 */
	public List<EntityTypes> validateEntityTypes(List<EntityTypes> entityTypes, boolean suggest) throws ParamValidationException;

	/**
	 * 
	 * @param searchQuery the query to search for entities
	 * @param scope optional parameter to filter only entities used in europeana, see also general search method
	 * @param entityTypes optional parameter to filter results by entity type
	 * @return
	 * @throws HttpException
	 */
	public List<String> searchEntityIds(Query searchQuery, String scope, List<EntityTypes> entityTypes) throws HttpException;

	
	List<EntityTypes> getEntityTypesFromString(String commaSepEntityTypes) throws UnsupportedEntityTypeException;
		
}
