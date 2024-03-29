package eu.europeana.entity.solr.model;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.solr.client.solrj.beans.Field;

import eu.europeana.entity.definitions.model.Agent;
import eu.europeana.entity.definitions.model.impl.BaseAgent;
import eu.europeana.entity.definitions.model.vocabulary.AgentSolrFields;
import eu.europeana.entity.definitions.model.vocabulary.ConceptSolrFields;
import eu.europeana.entity.definitions.model.vocabulary.EntitySolrFields;

public class SolrAgentImpl extends BaseAgent implements Agent {

	@Override
	@Field(AgentSolrFields.SAME_AS)
	public void setSameAs(String[] sameAs) {
		super.setSameAs(sameAs);
	}
	
	@Override
	@Field(AgentSolrFields.TYPE)
	public void setType(String internalType) {
		super.setType(internalType);
	}

	@Override
	@Field(AgentSolrFields.IDENTIFIER)
	public void setIdentifier(String[] identifier) {
		super.setIdentifier(identifier);
	}
	
	@Override
	@Field(AgentSolrFields.ID)
	public void setEntityId(String entityId) {
		super.setEntityId(entityId);
	}
	
	/**
	 * Concept fields
	 */
	@Override
	@Field(AgentSolrFields.HAS_PART)
	public void setHasPart(String[] hasPart) {
		super.setHasPart(hasPart);
	}

	@Override
	@Field(AgentSolrFields.IS_PART_OF)
	public void setIsPartOf(String[] isPartOf) {
		super.setIsPartOf(isPartOf);
	}

	@Override
	@Field(AgentSolrFields.IS_RELATED_TO)
	public void setIsRelatedTo(String[] isRelatedTo) {
		super.setIsRelatedTo(isRelatedTo);
	}

	@Override
	@Field(AgentSolrFields.PREF_LABEL_ALL)
	public void setPrefLabel(Map<String, String> prefLabel) {
		Map<String, String> normalizedPrefLabel = SolrUtils.normalizeStringMap(
				ConceptSolrFields.PREF_LABEL_PREFIX, prefLabel);
		super.setPrefLabel(normalizedPrefLabel);
	}

	@Override
	@Field(AgentSolrFields.ALT_LABEL_ALL)
	public void setAltLabel(Map<String, List<String>> altLabel) {
		Map<String, List<String>> normalizedAltLabel = SolrUtils.normalizeStringListMap(
				ConceptSolrFields.ALT_LABEL, altLabel);
		super.setAltLabel(normalizedAltLabel);
	}

	@Override
	@Field(AgentSolrFields.HIDDEN_LABEL)
	public void setHiddenLabel(Map<String, List<String>> hiddenLabel) {
		Map<String, List<String>> normalizedHiddenLabel = SolrUtils.normalizeStringListMap(
				ConceptSolrFields.HIDDEN_LABEL, hiddenLabel);
		super.setHiddenLabel(normalizedHiddenLabel);
	}

	@Override
	@Field(AgentSolrFields.NOTE_ALL)
	public void setNote(Map<String, List<String>> note) {
		Map<String, List<String>>  normalizedNote = SolrUtils.normalizeStringListMap(
				ConceptSolrFields.NOTE, note);
		super.setNote(normalizedNote);
	}
	
	@Override
	@Field(AgentSolrFields.DEPICTION)
	public void setDepiction(String depiction) {
		super.setDepiction(depiction);
	}
	        
	@Override
	@Field(EntitySolrFields.IS_SHOWN_BY_ID)
	public void setIsShownById(String isShownById) {
		super.setIsShownById(isShownById);
	}
	
	@Override
	@Field(EntitySolrFields.IS_SHOWN_BY_SOURCE)
	public void setIsShownBySource(String isShownBySource) {
		super.setIsShownBySource(isShownBySource);
	}
	
	@Override
	@Field(EntitySolrFields.IS_SHOWN_BY_THUMBNAIL)
	public void setIsShownByThumbnail(String isShownByThumbnail) {
		super.setIsShownByThumbnail(isShownByThumbnail);
	}
	
	// Agent Specific Fields
	@Override
	@Field(AgentSolrFields.BEGIN)
	public void setBegin(String[] begin) {
		super.setBegin(begin);
	}

	@Override
	@Field(AgentSolrFields.END)
	public void setEnd(String[] end) {
		super.setEnd(end);
	}

	@Override
	@Field(AgentSolrFields.HAS_MET)
	public void setHasMet(String[] hasMet) {
		super.setHasMet(hasMet);
	}
	@Override
	@Field(AgentSolrFields.NAME)
	public void setName(Map<String, String> name) {
		super.setName(name);
	}
	
	@Override
	@Field(AgentSolrFields.BIOGRAPHICAL_INFORMATION_ALL)
	public void setBiographicalInformation(Map<String, List<String>> biographicalInformation) {
		Map<String, List<String>> normalizedBiographicalInformation = SolrUtils.normalizeStringListMap(
				AgentSolrFields.BIOGRAPHICAL_INFORMATION, biographicalInformation);
		super.setBiographicalInformation(normalizedBiographicalInformation);
	}

	@Override
	@Field(AgentSolrFields.DATE_OF_BIRTH_ALL)
	public void setDateOfBirth(String[] dateOfBirth) {
		String[] normalizedDateOfBirth = SolrUtils.normalizeStringList(
				AgentSolrFields.DATE_OF_BIRTH_ALL, Arrays.asList(dateOfBirth));
		super.setDateOfBirth(normalizedDateOfBirth);
	}

	@Override
	@Field(AgentSolrFields.DATE_OF_DEATH_ALL)
	public void setDateOfDeath(String[] dateOfDeath) {
		String[] normalizedDateOfDeath = SolrUtils.normalizeStringList(
				AgentSolrFields.DATE_OF_DEATH_ALL, Arrays.asList(dateOfDeath));
		super.setDateOfDeath(normalizedDateOfDeath);
	}

	@Override
	@Field(AgentSolrFields.PLACE_OF_BIRTH_ALL)
	public void setPlaceOfBirth(Map<String, List<String>> placeOfBirth) {
		Map<String, List<String>> normalizedPlaceOfBirth = SolrUtils.normalizeStringListMap(
				AgentSolrFields.PLACE_OF_BIRTH, placeOfBirth);
		super.setPlaceOfBirth(normalizedPlaceOfBirth);
	}

	@Override
	@Field(AgentSolrFields.PLACE_OF_DEATH_ALL)
	public void setPlaceOfDeath(Map<String, List<String>> placeOfDeath) {
		Map<String, List<String>> normalizedPlaceOfDeath = SolrUtils.normalizeStringListMap(
				AgentSolrFields.PLACE_OF_DEATH, placeOfDeath);
		super.setPlaceOfDeath(normalizedPlaceOfDeath);
	}

	@Override
//	@Field(AgentSolrFields.DATE_OF_ESTABLISHMENT)
	public void setDateOfEstablishment(String dateOfEstablishment) {
		super.setDateOfEstablishment(dateOfEstablishment);
	}

	@Field(AgentSolrFields.DATE_OF_ESTABLISHMENT)
        public void setDateOfEstablishmentArray(String[] dateOfEstablishmentArray) {
	    //EA-3041 date of establishment is single value, ensured though the entity management, mapping needs to be updated when the schema will be updated
	    if(dateOfEstablishmentArray != null && dateOfEstablishmentArray.length > 0) {
                super.setDateOfEstablishment(dateOfEstablishmentArray[0]);
	    }
        }
	
	
	@Field(AgentSolrFields.DATE_OF_TERMINATION)
        public void setDateOfTerminationArray(String[] dateOfTerminationArray) {
            //EA-3041 date of establishment is single value, ensured though the entity management, mapping needs to be updated when the schema will be updated
            if(dateOfTerminationArray != null && dateOfTerminationArray.length > 0) {
                super.setDateOfTermination(dateOfTerminationArray[0]);
            }
        }
	
	@Override
	public void setGender(String gender) {
		super.setGender(gender);
	}
	
	@Field(AgentSolrFields.GENDER)
        @Deprecated
        /**
         * @deprecated need to fix cardinality and map the solr field to single valued
         * @param gender
         */
	public void setGender(String[] gender) {
            if(gender != null && gender.length > 0)    
	    this.setGender(gender[0]);
        }
	
	@Override
	@Field(AgentSolrFields.PROFESSION_OR_OCCUPATION_ALL)
	public void setProfessionOrOccupation(Map<String, List<String>> professionOrOccupation) {
		Map<String, List<String>> normalizedProfessionOrOccupation = SolrUtils.normalizeStringListMap(
				AgentSolrFields.PROFESSION_OR_OCCUPATION, professionOrOccupation);
		super.setProfessionOrOccupation(normalizedProfessionOrOccupation);
	}


	// Technical Fields
	@Override
	@Field(ConceptSolrFields.TIMESTAMP)
	public void setTimestamp(Date timestamp) {
		super.setTimestamp(timestamp);
	}

	@Override
	// @Field(SkosAgentSolrFields.WIKIPEDIA_CLICKS)
	public void setWikipediaClicks(int wikipediaClicks) {
		super.setWikipediaClicks(wikipediaClicks);
	}

	@Override
	// @Field(SkosAgentSolrFields.EUROPEANA_DOC_COUNT)
	public void setEuropeanaDocCount(int europeanaDocCount) {
		super.setEuropeanaDocCount(europeanaDocCount);
	}

	@Override
	// @Field(SkosAgentSolrFields.DERIVED_SCORE)
	public void setDerivedScore(float derivedScore) {
		super.setDerivedScore(derivedScore);
	}

	@Override
	@Field(AgentSolrFields.DATE)
	public void setDate(Date date) {
		super.setDate(date);
	}
	
	@Override
	@Field(AgentSolrFields.CREATED)
	public void setCreated(Date created) {
	    	super.setCreated(created);
	}
	
	@Override
	@Field(AgentSolrFields.MODIFIED)
	public void setModified(Date modified) {
	    	super.setModified(modified);
	}
}
