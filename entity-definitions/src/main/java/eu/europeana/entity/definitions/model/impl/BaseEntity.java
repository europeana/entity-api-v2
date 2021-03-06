package eu.europeana.entity.definitions.model.impl;

import java.util.Date;
import java.util.List;
import java.util.Map;

import eu.europeana.entity.definitions.model.Entity;
import eu.europeana.entity.definitions.model.RankedEntity;

public class BaseEntity implements Entity, RankedEntity {

    protected String TMP_KEY = "def";

    // common functional fields
    private String type;
    private String entityId;
    // depiction
    private String depiction;
    private Map<String, List<String>> note;
    private Map<String, String> prefLabel;
    private Map<String, List<String>> altLabel;
    private Map<String, List<String>> hiddenLabel;
//	private Map<String, List<String>> tmpPrefLabel;

    private String identifier[];
    private String[] sameAs;
    private String[] isRelatedTo;

    // hierarchical structure available only for a part of entities. Add set/get
    // methods to the appropriate interfaces
    private String[] hasPart;
    private String[] isPartOf;

    // technical fields
    private Date timestamp;
    private int wikipediaClicks;
    private int europeanaDocCount;
    private float derivedScore;

    // The time at which the Set was created by the user.
    private Date created;

    // The time at which the Set was modified, after creation.
    private Date modified;

    // isShownBy fields
    private String isShownById;
    private String isShownBySource;
    private String isShownByThumbnail;

    public Map<String, String> getPrefLabel() {
        return prefLabel;
    }

    public void setPrefLabel(Map<String, String> prefLabel) {
        this.prefLabel = prefLabel;
    }

    public Map<String, List<String>> getAltLabel() {
        return altLabel;
    }

    public void setAltLabel(Map<String, List<String>> altLabel) {
        this.altLabel = altLabel;
    }

    public Map<String, List<String>> getHiddenLabel() {
        return hiddenLabel;
    }

    public void setHiddenLabel(Map<String, List<String>> hiddenLabel) {
        this.hiddenLabel = hiddenLabel;
    }

    public Map<String, List<String>> getNote() {
        return note;
    }

    public void setNote(Map<String, List<String>> note) {
        this.note = note;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getEntityId() {
        return entityId;
    }

    public void setEntityId(String entityId) {
        this.entityId = entityId;
    }

    public String[] getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String[] identifier) {
        this.identifier = identifier;
    }

    @Deprecated
    public String getAbout() {
        return getEntityId();
    }

    @Deprecated
    public void setAbout(String about) {
        setEntityId(about);
    }

    @Override
    public Date getTimestamp() {
        return timestamp;
    }

    @Override
    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public int getWikipediaClicks() {
        return wikipediaClicks;
    }

    @Override
    public void setWikipediaClicks(int wikipediaClicks) {
        this.wikipediaClicks = wikipediaClicks;
    }

    @Override
    public int getEuropeanaDocCount() {
        return europeanaDocCount;
    }

    @Override
    public void setEuropeanaDocCount(int europeanaDocCount) {
        this.europeanaDocCount = europeanaDocCount;
    }

    @Override
    public float getDerivedScore() {
        return derivedScore;
    }

    @Override
    public void setDerivedScore(float derivedScore) {
        this.derivedScore = derivedScore;
    }

    public String[] getIsRelatedTo() {
        return isRelatedTo;
    }

    public void setIsRelatedTo(String[] isRelatedTo) {
        this.isRelatedTo = isRelatedTo;
    }

//	@Override
//	public ObjectId getId() {
//		// TODO Auto-generated method stub
//		return null;
//	}
//
//	@Override
//	public void setId(ObjectId id) {
//		// TODO Auto-generated method stub
//
//	}

    public String[] getHasPart() {
        return hasPart;
    }

    public void setHasPart(String[] hasPart) {
        this.hasPart = hasPart;
    }

    public String[] getIsPartOf() {
        return isPartOf;
    }

    public void setIsPartOf(String[] isPartOf) {
        this.isPartOf = isPartOf;
    }

    public String getDepiction() {
        return depiction;
    }

    public void setDepiction(String depiction) {
        this.depiction = depiction;
    }

    @Override
    public String[] getSameAs() {
        return sameAs;
    }

    public void setSameAs(String[] sameAs) {
        this.sameAs = sameAs;
    }

//	@Override
//	@Deprecated
//	public void setFoafDepiction(String foafDepiction) {
//		setDepiction(foafDepiction);
//	}

//	@Override
//	public String getFoafDepiction() {
//		return getDepiction();
//	}

//	@Override
//	@Deprecated
//	public ObjectId getId() {
//		// TODO Auto-generated method stub
//		return null;
//	}

//	@Override
//	@Deprecated
//	public void setId(ObjectId id) {
//		// TODO Auto-generated method stub
//	}

//	@Override
//	public Map<String, List<String>> getPrefLabel() {		
//		//if not available
//		if (getPrefLabelStringMap() == null)
//			return null;
//		//if not transformed		
//		if (tmpPrefLabel == null)
//			tmpPrefLabel = fillTmpMapToMap(getPrefLabelStringMap());
//
//		return tmpPrefLabel;
//	}

//	/**
//	 * This method converts List<String> to Map<String, List<String>> 
//	 * @param list of strings
//	 */
//	protected Map<String, List<String>> fillTmpMap(List<String> list) {
//		
//		Map<String, List<String>> tmpMap = new HashMap<String, List<String>>();
//		tmpMap.put(TMP_KEY, list);
//		return tmpMap;
//	}

//	/**
//	 * This method converts  Map<String, String> to Map<String, List<String>> 
//	 * @param map of strings
//	 */
//	protected Map<String, List<String>> fillTmpMapToMap(Map<String, String> mapOfStrings) {
//		
//		Map<String, List<String>> tmpMap = null;	
//		tmpMap = mapOfStrings.entrySet().stream().collect(Collectors.toMap(
//				entry -> entry.getKey(), 
//				entry -> Collections.singletonList(entry.getValue()))
//		);	
//		
//		return tmpMap;
//	}

    public String[] getOwlSameAs() {
        return getSameAs();
    }

    @Override
    public String getEntityIdentifier() {
        String[] splitArray = this.getAbout().split("/");
        return splitArray[splitArray.length - 1];
    }

    @Override
    public Date getCreated() {
        return created;
    }

    @Override
    public void setCreated(Date created) {
        this.created = created;
    }

    @Override
    public Date getModified() {
        return modified;
    }

    @Override
    public void setModified(Date modified) {
        this.modified = modified;
    }

    public String getIsShownById() {
        return isShownById;
    }

    public void setIsShownById(String isShownById) {
        this.isShownById = isShownById;
    }

    public String getIsShownBySource() {
        return isShownBySource;
    }

    public void setIsShownBySource(String isShownBySource) {
        this.isShownBySource = isShownBySource;
    }

    public String getIsShownByThumbnail() {
        return isShownByThumbnail;
    }

    public void setIsShownByThumbnail(String isShownByThumbnail) {
        this.isShownByThumbnail = isShownByThumbnail;
    }

}
