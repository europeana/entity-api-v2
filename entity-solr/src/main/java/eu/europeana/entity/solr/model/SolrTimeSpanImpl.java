package eu.europeana.entity.solr.model;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.solr.client.solrj.beans.Field;

import eu.europeana.entity.definitions.model.TimeSpan;
import eu.europeana.entity.definitions.model.impl.BaseTimeSpan;
import eu.europeana.entity.definitions.model.vocabulary.ConceptSolrFields;

public class SolrTimeSpanImpl extends BaseTimeSpan implements TimeSpan {

    // COMMON FIELDS
    // Need to annotate concept fields as well as this class doesn't extend the
    // SolrConceptImpl
    @Override
    @Field(ConceptSolrFields.ID)
    public void setEntityId(String entityId) {
	super.setEntityId(entityId);
    }

    @Override
    @Field(ConceptSolrFields.TYPE)
    public void setType(String internalType) {
	super.setType(internalType);
    }

    @Override
    @Field(ConceptSolrFields.DEPICTION)
    public void setDepiction(String depiction) {
	super.setDepiction(depiction);
    }

    @Override
    @Field(ConceptSolrFields.IS_SHOWN_BY_ID)
    public void setIsShownById(String isShownById) {
	super.setIsShownById(isShownById);
    }

    @Override
    @Field(ConceptSolrFields.IS_SHOWN_BY_SOURCE)
    public void setIsShownBySource(String isShownBySource) {
	super.setIsShownBySource(isShownBySource);
    }

    @Override
    @Field(ConceptSolrFields.IS_SHOWN_BY_THUMBNAIL)
    public void setIsShownByThumbnail(String isShownByThumbnail) {
	super.setIsShownByThumbnail(isShownByThumbnail);
    }

    @Override
    @Field(ConceptSolrFields.PREF_LABEL_ALL)
    public void setPrefLabel(Map<String, String> prefLabel) {
	Map<String, String> normalizedPrefLabel = SolrUtils.normalizeStringMap(ConceptSolrFields.PREF_LABEL_PREFIX, prefLabel);
	super.setPrefLabel(normalizedPrefLabel);
    }

    @Override
    @Field(ConceptSolrFields.ALT_LABEL_ALL)
    public void setAltLabel(Map<String, List<String>> altLabel) {
	Map<String, List<String>> normalizedAltLabel = SolrUtils.normalizeStringListMap(ConceptSolrFields.ALT_LABEL,
		altLabel);
	super.setAltLabel(normalizedAltLabel);
    }

    @Override
    @Field(ConceptSolrFields.HIDDEN_LABEL_ALL)
    public void setHiddenLabel(Map<String, List<String>> hiddenLabel) {
	Map<String, List<String>> normalizedHiddenLabel = SolrUtils.normalizeStringListMap(ConceptSolrFields.HIDDEN_LABEL,
		hiddenLabel);
	super.setHiddenLabel(normalizedHiddenLabel);
    }
    
    /**
     * Concept fields
     */
    @Override
    @Field(ConceptSolrFields.NOTE_ALL)
    public void setNote(Map<String, List<String>> note) {
	Map<String, List<String>> normalizedNote = SolrUtils.normalizeStringListMap(ConceptSolrFields.NOTE, note);
	super.setNote(normalizedNote);
    }

    @Override
    @Field(ConceptSolrFields.HAS_PART)
    public void setHasPart(String[] hasPart) {
	super.setHasPart(hasPart);
    }

    @Override
    @Field(ConceptSolrFields.IS_PART_OF)
    public void setIsPartOf(String[] isPartOf) {
	super.setIsPartOf(isPartOf);
    }

    @Override
    @Field(ConceptSolrFields.SAME_AS)
    public void setSameAs(String[] sameAs) {
	super.setSameAs(sameAs);
    }

    // SPECIFIC FIELDS
    @Override
    @Field(ConceptSolrFields.IS_NEXT_IN_SEQUENCE)
    public void setIsNextInSequence(String[] isNextInSequence) {
	super.setIsNextInSequence(isNextInSequence);
    }

    @Override
    @Field(ConceptSolrFields.BEGIN)
    public void setBegin(String begin) {
	super.setBegin(begin);
    }
    
    @Override
    @Field(ConceptSolrFields.END)
    public void setEnd(String end) {
        super.setEnd(end);
    }

    // TECHNICAL FIELDS
    @Override
    @Field(ConceptSolrFields.CREATED)
    public void setCreated(Date created) {
	super.setCreated(created);
    }

    @Override
    @Field(ConceptSolrFields.MODIFIED)
    public void setModified(Date modified) {
	super.setModified(modified);
    }

}
