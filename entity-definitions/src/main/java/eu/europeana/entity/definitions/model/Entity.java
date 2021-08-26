package eu.europeana.entity.definitions.model;

import java.util.Date;
import java.util.List;
import java.util.Map;

public interface Entity {

    /**
     * Retrieves the Preferable Label for a Contextual Class (language,value) format
     * 
     * @return A Map<String,List<List<String>>> for the Preferable Labels of a
     *         contextual class (one per language)
     */
    Map<String, String> getPrefLabel();

    /**
     * Retrieves the Alternative Label for a Contextual Class (language,value)
     * format
     * 
     * @return A Map<String,List<List<String>>> for the Alternative Labels of a
     *         contextual class (one per language)
     */
    Map<String, List<String>> getAltLabel();

    /**
     * Retrieves the skos:note fields of a Contextual Class
     * 
     * @return A string array with notes for the Contextual Class
     */
    Map<String, List<String>> getNote();

    /**
     * Set the altLabel for a Contextual Class
     * 
     * @param altLabel A Map<String,List<List<String>>> for the Alternative Labels
     *                 of a contextual class (one per language)
     */
    void setAltLabel(Map<String, List<String>> altLabel);

    /**
     * Set the notes for a Contextual Class
     * 
     * @param note A String array with notes for the Contextual Class
     */
    void setNote(Map<String, List<String>> note);

    /**
     * Set the prefLabel for a Contextual Class
     * 
     * @param prefLabel A Map<String,String> for the Preferable Labels of a
     *                  contextual class (one per language)
     */
    void setPrefLabel(Map<String, String> prefLabel);

    /**
     * sets the skos:hiddenLabel for a contextual class
     * 
     * @param hiddenLabel
     */
    void setHiddenLabel(Map<String, List<String>> hiddenLabel);

    /**
     * 
     * @return the skos:hiddenLabel for the contextual class
     */
    Map<String, List<String>> getHiddenLabel();

//	/**
//	 * sets the foaf:depiction for contextual class
//	 * @param foafDepiction
//	 */
//	void setFoafDepiction(String foafDepiction);
//
//	/**
//	 * 
//	 * @return the foaf:depiction for cotextual class
//	 */
//	String getFoafDepiction();

    /**
     * 
     * @return the identifier part (last part) of the getAbout url
     */
    String getEntityIdentifier();

    public String[] getIdentifier();

    public void setIdentifier(String[] identifier);

    public String[] getIsRelatedTo();

    public void setIsRelatedTo(String[] isRelatedTo);

    public String getEntityId();

    public void setEntityId(String enitityId);

    public String getType();

    public String[] getSameAs();

    public String getDepiction();

    public void setDepiction(String depiction);

//	/**
//	 * Retrieves the preferable label for a contextual class (language,value)
//	 * 
//	 * @return A Map<String, String> for the preferable labels of a contextual class 
//	 *         (one per language)
//	 */
//	public Map<String, String> getPrefLabelStringMap();

    /**
     * @return
     */
    Date getCreated();

    void setCreated(Date created);

    /**
     * @return
     */
    Date getModified();

    void setModified(Date modified);

}
