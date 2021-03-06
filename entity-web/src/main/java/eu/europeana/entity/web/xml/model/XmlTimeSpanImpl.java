package eu.europeana.entity.web.xml.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import eu.europeana.entity.definitions.model.TimeSpan;

@JacksonXmlRootElement(localName = XmlConstants.XML_EDM_TIMESPAN)
@JsonInclude(value = JsonInclude.Include.NON_EMPTY)
@JsonPropertyOrder({
    XmlConstants.XML_RDF_ABOUT, XmlConstants.XML_FOAF_DEPICTION, XmlConstants.XML_EDM_IS_SHOWN_BY,  
    XmlConstants.XML_SKOS_PREF_LABEL, XmlConstants.XML_SKOS_ALT_LABEL, XmlConstants.XML_SKOS_HIDDEN_LABEL,
    XmlConstants.XML_EDM_BEGIN,XmlConstants.XML_EDM_END,XmlConstants.XML_SKOS_NOTE, 
    XmlConstants.XML_DCTERMS_HAS_PART, XmlConstants.XML_DCTERMS_IS_PART_OF, 
    XmlConstants.XML_EDM_IS_NEXT_IN_SEQUENCE, XmlConstants.XML_OWL_SAME_AS, XmlConstants.XML_EDM_WEB_RESOURCE})
public class XmlTimeSpanImpl extends XmlBaseEntityImpl {
    	
    	public XmlTimeSpanImpl(TimeSpan timespan) {
    	    	super(timespan);
    	}

	@JacksonXmlElementWrapper(useWrapping=false)
	@JacksonXmlProperty(localName = XmlConstants.XML_DCTERMS_IS_PART_OF)
	public List<RdfResource> getIsPartOf() {
	    	return RdfXmlUtils.convertToRdfResource(((TimeSpan)entity).getIsPartOf());
	}
	
	@JacksonXmlElementWrapper(useWrapping=false)
	@JacksonXmlProperty(localName = XmlConstants.XML_EDM_BEGIN)
	public String getBegin() {
	    	return ((TimeSpan)entity).getBegin();
	}

	@JacksonXmlElementWrapper(useWrapping=false)
	@JacksonXmlProperty(localName = XmlConstants.XML_EDM_END)
	public String getEnd() {
	    	return ((TimeSpan)entity).getEnd();
	}
	
	@JacksonXmlElementWrapper(useWrapping=false)
	@JacksonXmlProperty(localName = XmlConstants.XML_SKOS_HIDDEN_LABEL)
	public List<XmlMultilingualString> getHiddenLabel() {
		return RdfXmlUtils.convertToXmlMultilingualString(entity.getHiddenLabel());
	}

	@JacksonXmlElementWrapper(useWrapping=false)
	@JacksonXmlProperty(localName = XmlConstants.XML_EDM_IS_NEXT_IN_SEQUENCE)
	public List<RdfResource> getIsNextInSequence() {
	    	return RdfXmlUtils.convertToRdfResource(((TimeSpan)entity).getIsNextInSequence());
	}

	
}
