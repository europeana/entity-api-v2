package eu.europeana.entity.web.service;

import java.util.List;

import eu.europeana.api.commons_sb3.error.EuropeanaI18nApiException;
import jakarta.servlet.http.HttpServletRequest;

import eu.europeana.api.commons_sb3.definitions.search.Query;
import eu.europeana.api.commons_sb3.definitions.search.ResultSet;
import eu.europeana.api.commons_sb3.definitions.search.result.ResultsPage;
import eu.europeana.api.commons_sb3.error.EuropeanaApiException;
import eu.europeana.api.commons_sb3.error.exceptions.InvalidParamException;
import eu.europeana.entity.definitions.model.Entity;
import eu.europeana.entity.definitions.model.vocabulary.EntityTypes;
import eu.europeana.entity.definitions.model.vocabulary.SuggestAlgorithmTypes;
import eu.europeana.entity.web.model.view.EntityPreview;

public interface EntityService {

	/**
	 * Retrieve entity by url
	 * @param type type of entity
	 * @param identifier id of the entity
	 * @return
	 * @throws EuropeanaApiException
	 */
	Entity retrieveByUrl(String type, String identifier) throws EuropeanaApiException;

	/**
	 * This method provides suggestions for auto-completion
	 * 
	 * @param text text to be searched
	 * @param language language value
	 * @param entityTypes types of entity
	 * @param namespace namespace param to be searched
	 * @param rows rows to be fetched
	 * @param algorithm The default algorithm is "suggest" but other types are possible
	 * @return syggested entities
	 * @throws EuropeanaApiException
	 * 
	 * e.g. GET /entity/suggest?text=leonard&language=en
	 */
	<T extends EntityPreview> ResultSet<T> suggest(
			String text, String[] language, List<EntityTypes> entityTypes, String scope, String namespace, int rows, SuggestAlgorithmTypes algorithm) throws EuropeanaApiException;


	/**
	 * This method searches the entities using the provided search query and specific filters
	 * @param query query for the search
	 * @param preferredLanguages languages to be searched
	 * @param entityTypes types of entity
	 * @param scope scope of the search
	 * @return entity based on the query
	 */
	<T extends Entity> ResultSet<T> search(Query query, String[] preferredLanguages, List<EntityTypes> entityTypes, String scope) ;
	
	
	/**
	 * Performs a lookup for the entity in all 4 datasets:
	 * 
	 *    agents, places, concepts and time spans 
	 * 
	 * using an alternative uri for an entity (lookup will happen within the owl:sameAs properties).
	 * 
	 * @param uri uri to be searched
	 * @return a list of found entities or an exception if no entity is found
	 * @throws EuropeanaApiException
	 */
	List<String> resolveByUri(String uri) throws EuropeanaApiException;
	
	
	/**
	 * This method build the results page object for the search results retrieved with the given search query.
	 * @param searchQuery search query
	 * @param results results of which results will be build
	 * @param request request sent
	 * @return result page of the results fetched
	 */
	public <T extends Entity> ResultsPage<T> buildResultsPage(Query searchQuery, ResultSet<T> results, HttpServletRequest request);
	
	/**
	 * @param entityTypes types of entities
	 * @param suggest suggest filter
	 * @return  list of entities
	 * @throws InvalidParamException
	 */
	public List<EntityTypes> validateEntityTypes(List<EntityTypes> entityTypes, boolean suggest) throws InvalidParamException;

	/**
	 * 
	 * @param searchQuery the query to search for entities
	 * @param scope optional parameter to filter only entities used in europeana, see also general search method
	 * @param entityTypes optional parameter to filter results by entity type
	 * @return list of entity ids
	 */
	public List<String> searchEntityIds(Query searchQuery, String scope, List<EntityTypes> entityTypes);

	/**
	 * return the entities from the string
	 * @param commaSepEntityTypes comma seperated list of entities
	 * @return list of entity types
	 * @throws EuropeanaI18nApiException
	 */
	List<EntityTypes> getEntityTypesFromString(String commaSepEntityTypes) throws EuropeanaI18nApiException;
		
}
