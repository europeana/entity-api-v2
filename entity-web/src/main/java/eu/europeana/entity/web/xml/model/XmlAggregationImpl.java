package eu.europeana.entity.web.xml.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import eu.europeana.entity.definitions.model.Entity;;

@JacksonXmlRootElement(localName = XmlConstants.XML_ORE_AGGREGATION)
@JsonInclude(value = JsonInclude.Include.NON_EMPTY)
@JsonPropertyOrder({XmlConstants.XML_DCTERMS_CREATED, XmlConstants.XML_DCTERMS_MODIFIED, XmlConstants.XML_ORE_AGGREGATES})
public class XmlAggregationImpl {

    	@JsonIgnore
    	Entity entity;
    	@JsonIgnore
    	String id;
    	@JsonIgnore
    	private final String AGGREGATION_TAG = "#aggregation";
    	
    	public XmlAggregationImpl(Entity entity) {
    	    this.entity = entity;
    	    id = entity.getEntityId() + AGGREGATION_TAG;
    	}
    	
	@JacksonXmlProperty(isAttribute= true, localName = XmlConstants.XML_RDF_ABOUT)
	public String getAbout() {
		return id;
	}
    	
	@JacksonXmlProperty(localName = XmlConstants.XML_DCTERMS_CREATED)
	public RdfTypedElement getCreated() {
	    	if(entity.getCreated() == null)
	    	    return null;
		return new RdfTypedElement(entity.getCreated());
	}
	
	@JacksonXmlProperty(localName = XmlConstants.XML_DCTERMS_MODIFIED)
	public RdfTypedElement getModified() {
	    	if(entity.getModified() == null)
	    	    return null;
		return new RdfTypedElement(entity.getModified());
	}
	
	@JacksonXmlProperty(localName = XmlConstants.XML_ORE_AGGREGATES)
	public RdfResource getAggregates() {
		return new RdfResource(entity.getEntityId());
	}
	
}
