package eu.europeana.entity.solr.service;

import java.util.List;
import eu.europeana.api.commons_sb3.definitions.search.Query;
import eu.europeana.api.commons_sb3.definitions.search.ResultSet;
import eu.europeana.entity.definitions.exceptions.UnsupportedEntityTypeException;
import eu.europeana.entity.definitions.model.Entity;
import eu.europeana.entity.definitions.model.vocabulary.ConceptSolrFields;
import eu.europeana.entity.definitions.model.vocabulary.EntityTypes;
import eu.europeana.entity.solr.exception.EntityRetrievalException;
import eu.europeana.entity.solr.exception.EntitySuggestionException;
import eu.europeana.entity.web.model.view.EntityPreview;

public interface SolrEntityService {

	String HANDLER_SELECT = "/select";
	
	/**
	 * This method retrieves available Entities by searching the given id.
	 * 
	 * @param id The SOLR entity_id
	 * @return
	 * @throws EntityRetrievalException 
	 */
//	public Entity searchById(String entityId) throws EntityRetrievalException;

	/**
	 * This method retrieves available Entities by searching the given entity URI.
	 * @param entityUri - See {@link ConceptSolrFields#ID}
	 * @param type type of entity
	 * @return entity with the url and type matching
	 * @throws EntityRetrievalException exception while retrieving entity
	 * @throws UnsupportedEntityTypeException if the type is invalid
	 */
	Entity searchByUrl(String type, String entityUri) throws EntityRetrievalException, UnsupportedEntityTypeException;
	
	/**
	 * This method retrieves available Entities for the search query.
	 * @param searchQuery The search query
	 * @param outLanguage the output language
	 * @param entityTypes types of entity
	 * @param scope scope of the search
	 * @return ResultSet of entities matching the search
	 * @throws EntityRetrievalException exception while retrieving entity
	 */
	<T extends Entity> ResultSet<T> search(Query searchQuery, String[] outLanguage,
											  List<EntityTypes> entityTypes, String scope) throws EntityRetrievalException;
	
	/**
	 * This method retrieves available Entities that meet the query criteria using search by label algorithm
	 * @param text The query text
	 * @param requestedLanguages languages requested
	 * @param entityTypes types of entity
	 * @param scope scope of the search
	 * @param rows number of rows requested
	 * @return ResultSet of entities matching the search
	 * @throws EntityRetrievalException exception while retrieving entity
	 * @throws EntitySuggestionException exception while suggesting and entity
	 */
	<T extends EntityPreview> ResultSet<T> suggestByLabel(String text, String[] requestedLanguages,
														  List<EntityTypes> entityTypes, String scope, int rows) throws EntitySuggestionException;

	/**
	 * This method retrieves available Entities that meet the query criteria using search 
	 * by language algorithm
	 * @param text The query text
	 * @param requestedLanguages languages requested
	 * @param entityTypes types of entity
	 * @param scope scope of the search
	 * @param rows number of rows requested
	 * @return ResultSet of entities matching the search
	 * @throws EntityRetrievalException exception while retrieving entity
	 * @throws EntitySuggestionException exception while suggesting and entity
	 */
	<T extends EntityPreview> ResultSet<T> suggestByLanguage(String text, String[] requestedLanguages,
															 List<EntityTypes> entityTypes, String scope,  int rows) throws EntitySuggestionException;

	
	/**
	 * Performs a lookup for the entity in all 4 datasets:
	 * 
	 *    agents, places, concepts and time spans 
	 * 
	 * using an alternative uri for an entity (lookup will happen within the coref property).
	 * 
	 * @param uri url sent in the request
	 * @return and empty list or a list of found entities
	 */
	List<String> searchByCoref(String uri);
	

}
