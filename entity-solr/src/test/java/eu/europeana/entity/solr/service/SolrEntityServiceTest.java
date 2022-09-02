package eu.europeana.entity.solr.service;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import javax.annotation.Resource;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.common.params.CommonParams;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import eu.europeana.api.commons.definitions.search.Query;
import eu.europeana.api.commons.definitions.search.ResultSet;
import eu.europeana.api.commons.definitions.search.impl.QueryImpl;
import eu.europeana.entity.config.AppConfigConstants;
import eu.europeana.entity.definitions.exceptions.UnsupportedEntityTypeException;
import eu.europeana.entity.definitions.model.Agent;
import eu.europeana.entity.definitions.model.Entity;
import eu.europeana.entity.definitions.model.vocabulary.WebEntityConstants;
import eu.europeana.entity.solr.config.EntitySolrConfig;
import eu.europeana.entity.solr.exception.EntityRetrievalException;
import eu.europeana.entity.solr.exception.EntitySuggestionException;
import eu.europeana.entity.solr.service.impl.SolrEntityServiceImpl;
import eu.europeana.entity.web.model.view.EntityPreview;

@SpringBootTest(classes = {
    EntitySolrConfig.class, 
    SolrEntityServiceImpl.class})
//@ContextConfiguration({ "/entity-solr-context.xml" })
public class SolrEntityServiceTest {

	
	private String RES_SUGGEST_BY_LABEL = "ResultSet [query=null, results=[entityId: http://data.europeana.eu/agent/base/146951\n" + 
			"preferredLabel: {de=Wolfgang Amadeus Mozart, en=Wolfgang Amadeus Mozart, fr=Wolfgang Amadeus Mozart}, entityId: http://data.europeana.eu/agent/base/145380\n" + 
			"preferredLabel: {de=Moses Mendelssohn, en=Moses Mendelssohn, lv=Mozess Mendelsons, fr=Moses Mendelssohn}, entityId: http://data.europeana.eu/concept/base/45\n" + 
			"preferredLabel: {de=Mosaik, lt=Mozaika, en=Mosaic, fr=Mosaïque}, entityId: http://data.europeana.eu/place/base/216282\n" + 
			"preferredLabel: {de=Birma (Myanmar), ln=Mozambiki, en=Birma (Myanmar), fr=Birmanie}, entityId: http://data.europeana.eu/agent/base/147150\n" + 
			"preferredLabel: {de=Leopold Mozart, en=Leopold Mozart, fr=Leopold Mozart}, entityId: http://data.europeana.eu/place/base/51\n" + 
			"preferredLabel: {de=Mosambik, en=Mozambique, fr=Mozambique}, entityId: http://data.europeana.eu/place/base/134394\n" + 
			"preferredLabel: {de=Woiwodschaft Ermland-Masuren, lt=Varmijos Mozūrų vaivadija, en=Warmian-Masurian Voivodeship, fr=Voïvodie de Varmie-Mazurie}, entityId: http://data.europeana.eu/agent/base/1325\n" + 
			"preferredLabel: {de=Mozart Camargo Guarnieri, en=Camargo Guarnieri, fr=Camargo Guarnieri}, entityId: http://data.europeana.eu/agent/base/55638\n" + 
			"preferredLabel: {de=Leszek Możdżer, en=Leszek Możdżer, fr=Leszek Możdżer}, entityId: http://data.europeana.eu/agent/base/145487\n" + 
			"preferredLabel: {de=Mozi, en=Mozi, fr=Mozi}], facetFields=null, resultSize=10, searchTime=0]";
	
	@Resource(name = AppConfigConstants.ENTITY_SOLR_SERVICE)
	SolrEntityService solrEntityService;

	private final Logger log = LogManager.getLogger(getClass());

	@Test
	public void testSearchByUrl() throws EntityRetrievalException, UnsupportedEntityTypeException {

		// Concept entity =
		// solrEntityService.searchByUrl("\"http://d-nb.info/gnd/4019862-5\"");
		Agent entity = (Agent) solrEntityService.searchByUrl("agent", "http://data.europeana.eu/agent/108974");
		assertNotNull(entity);
		assertTrue("http://data.europeana.eu/agent/108974".equals(entity.getEntityId()));
		assertEquals(10, entity.getPrefLabel().size());
		assertEquals(6, entity.getAltLabel().size());
		assertNotNull(entity.getDateOfBirth());
	}

	@Test
	public void testSearch() throws EntityRetrievalException {

		Query searchQuery = new QueryImpl("\"Giorgos Leonardos\"", 10);
		ResultSet<? extends Entity> rs = solrEntityService.search(searchQuery, new String[] { "en", "de", "el" }, null,
				null);

		assertNotNull(rs.getResults());
		assertTrue(rs.getResultSize() >= 1);
		Agent entity = (Agent) rs.getResults().get(0);
		assertEquals(3, entity.getPrefLabel().size());
                assertEquals(2, entity.getAltLabel().size());
                log.debug("found results:" + rs.getResultSize());
		log.debug(rs.getResults().get(0));
	}

	@Test
	public void testSuggestByLabel() throws EntityRetrievalException, EntitySuggestionException {
		
		SolrQuery solrQuery;
		String[] searchParams;
		String[] fields;
		searchParams = new String[]{CommonParams.SORT, "derived_score desc", "q.op", "AND"};
		fields = new String[]{"id", "payload", "derived_score"};
		String TEST_SUGGESTION_TEXT = "Giorgos Leonardos";
		
		solrQuery = new SolrQuery(
				CommonParams.Q, WebEntityConstants.FIELD_LABEL + ":(" + TEST_SUGGESTION_TEXT + "*)", searchParams);		
		solrQuery.setFields(fields);
		
		
		ResultSet<? extends EntityPreview> resSuggestByLabel = solrEntityService.suggestByLabel(
				TEST_SUGGESTION_TEXT, new String[] { "en", "de", "fr" }, null,
				null, 10);
		
		assertNotNull(resSuggestByLabel.getResults());
		assertTrue(resSuggestByLabel.getResultSize() >= 1);
		log.debug("found results:" + resSuggestByLabel.getResultSize());
		log.debug(resSuggestByLabel.getResults().get(0));
		
//		assertTrue(resSuggestByLabel.toString().equals(RES_SUGGEST_BY_LABEL));
	}

}
