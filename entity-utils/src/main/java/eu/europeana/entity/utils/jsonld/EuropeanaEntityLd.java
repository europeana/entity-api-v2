package eu.europeana.entity.utils.jsonld;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.stanbol.commons.jsonld.JsonLd;
import org.apache.stanbol.commons.jsonld.JsonLdProperty;
import org.apache.stanbol.commons.jsonld.JsonLdPropertyValue;
import org.apache.stanbol.commons.jsonld.JsonLdResource;

import eu.europeana.api.commons.definitions.utils.DateUtils;
import eu.europeana.entity.definitions.exceptions.UnsupportedEntityTypeException;
import eu.europeana.entity.definitions.model.Agent;
import eu.europeana.entity.definitions.model.Concept;
import eu.europeana.entity.definitions.model.Entity;
import eu.europeana.entity.definitions.model.Organization;
import eu.europeana.entity.definitions.model.Place;
import eu.europeana.entity.definitions.model.TimeSpan;
import eu.europeana.entity.definitions.model.impl.BaseEntity;
import eu.europeana.entity.definitions.model.vocabulary.EntityTypes;
import eu.europeana.entity.definitions.model.vocabulary.WebEntityConstants;
import eu.europeana.entity.definitions.model.vocabulary.WebEntityFields;
import eu.europeana.entity.utils.EntityUtils;

public class EuropeanaEntityLd extends JsonLd {

    JsonLdResource ldResource = new JsonLdResource();
    String entityIdBaseUrl;

    public EuropeanaEntityLd(Entity entity, String entityIdBaseUrl) throws UnsupportedEntityTypeException {
	super();
	this.entityIdBaseUrl = entityIdBaseUrl;
	setPropOrderComparator(new EntityJsonComparator());
	registerContainerProperty(WebEntityConstants.BIOGRAPHICAL_INFORMATION);
	registerContainerProperty(WebEntityConstants.PLACE_OF_BIRTH);
	registerContainerProperty(WebEntityConstants.PLACE_OF_DEATH);
	// always string array, not container
//		registerContainerProperty(WebEntityConstants.IS_PART_OF);
	setEntity(entity);
    }

    public JsonLdResource setEntity(Entity entity) throws UnsupportedEntityTypeException {
	setUseTypeCoercion(false);
	setUseCuries(true);
	setUsedNamespaces(namespacePrefixMap);

	ldResource.setSubject("");
	ldResource.putProperty(WebEntityFields.CONTEXT, WebEntityFields.ENTITY_CONTEXT);

	// common EntityProperties
	ldResource.putProperty(WebEntityFields.ID, EntityUtils.replaceBaseUrlInId(entity.getEntityId(), entityIdBaseUrl));
	ldResource.putProperty(WebEntityFields.TYPE, entity.getType());
	putStringArrayProperty(WebEntityFields.IDENTIFIER, entity.getIdentifier(), ldResource);
	putStringArrayProperty(WebEntityFields.SAME_AS, entity.getSameAs(), ldResource);
	// jsonLdResource.putProperty(WebEntityFields.RDF_ABOUT, entity.getAbout());
	putStringArrayProperty(WebEntityFields.IS_RELATED_TO, entity.getIsRelatedTo(), ldResource);

	if (!StringUtils.isEmpty(entity.getDepiction())) {
	    ldResource.putProperty(createWikimediaResource(entity.getDepiction(), WebEntityFields.DEPICTION));
	}

	// common SKOS_Properties
	putMapOfStringProperty(WebEntityFields.PREF_LABEL, ((BaseEntity) entity).getPrefLabel(), "",
		ldResource);
	putMapOfStringListProperty(WebEntityFields.ALT_LABEL, entity.getAltLabel(), "", ldResource);
	putMapOfStringListProperty(WebEntityFields.HIDDEN_LABEL, entity.getHiddenLabel(), "", ldResource);
	putMapOfStringListProperty(WebEntityFields.NOTE, entity.getNote(), "", ldResource);

	// common administrative information (created, modified)
	putAggregationProperty(entity, ldResource);

	// isShownBy
	if (!StringUtils.isEmpty(((BaseEntity) entity).getIsShownById())) {
	    ldResource.putProperty(createIsShownByResource((BaseEntity) entity, WebEntityFields.IS_SHOWN_BY));
	}

	// specific properties (by entity type)
	putSpecificProperties(entity, ldResource);

	put(ldResource);

	return ldResource;
    }

    /**
     * This method constructs isShownBy property
     * 
     * @param entity
     * @param field
     * @return jsonLd property for isShownBy field
     */
    private JsonLdProperty createIsShownByResource(BaseEntity entity, String field) {

	JsonLdProperty isShownByProperty = new JsonLdProperty(field);
	JsonLdPropertyValue isShownByValue = new JsonLdPropertyValue();

	if (!StringUtils.isEmpty(entity.getIsShownById())) {
	    isShownByValue.putProperty(new JsonLdProperty(WebEntityFields.ID, entity.getIsShownById()));
	    isShownByValue.putProperty(new JsonLdProperty(WebEntityFields.TYPE, WebEntityFields.WEB_RESOURCE));
	}

	if (!StringUtils.isEmpty(entity.getIsShownBySource()))
	    isShownByValue.putProperty(new JsonLdProperty(WebEntityFields.SOURCE, entity.getIsShownBySource()));

	if (!StringUtils.isEmpty(entity.getIsShownByThumbnail()))
	    isShownByValue.putProperty(new JsonLdProperty(WebEntityFields.THUMBNAIL, entity.getIsShownByThumbnail()));

	isShownByProperty.addValue(isShownByValue);
	return isShownByProperty;
    }

    private JsonLdProperty createWikimediaResource(String wikimediaCommonsId, String field) {

	JsonLdProperty depictionProperty = new JsonLdProperty(field);
	JsonLdPropertyValue depictionValue = new JsonLdPropertyValue();

	depictionValue.putProperty(new JsonLdProperty(WebEntityFields.ID, wikimediaCommonsId));
	String sourceValue = EntityUtils.createWikimediaResourceString(wikimediaCommonsId);
	depictionValue.putProperty(new JsonLdProperty(WebEntityFields.SOURCE, sourceValue));

	depictionProperty.addValue(depictionValue);
	return depictionProperty;
    }

    private void putConceptSpecificProperties(Concept entity, JsonLdResource jsonLdResource) {
	putMapOfStringListProperty(WebEntityFields.NOTATION, entity.getNotation(), "", jsonLdResource);
	putStringArrayProperty(WebEntityFields.RELATED, entity.getRelated(), jsonLdResource);
	putStringArrayProperty(WebEntityFields.BROADER, entity.getBroader(), jsonLdResource);
	putStringArrayProperty(WebEntityFields.NARROWER, entity.getNarrower(), jsonLdResource);
	putStringArrayProperty(WebEntityFields.EXACT_MATCH, entity.getExactMatch(), jsonLdResource);
	putStringArrayProperty(WebEntityFields.CLOSE_MATCH, entity.getCloseMatch(), jsonLdResource);
	putStringArrayProperty(WebEntityFields.BROAD_MATCH, entity.getBroadMatch(), jsonLdResource);
	putStringArrayProperty(WebEntityFields.NARROW_MATCH, entity.getNarrowMatch(), jsonLdResource);
	putStringArrayProperty(WebEntityFields.RELATED_MATCH, entity.getRelatedMatch(), jsonLdResource);
    }

    private void putSpecificProperties(Entity entity, JsonLdResource jsonLdResource)
	    throws UnsupportedEntityTypeException {

	EntityTypes entityType = EntityTypes.getByInternalType(entity.getType());

	switch (entityType) {
	case Organization:
	    putOrganizationSpecificProperties((Organization) entity, jsonLdResource);
	    break;

	case Concept:
	    putConceptSpecificProperties((Concept) entity, jsonLdResource);
	    break;

	case Agent:
	    putAgentSpecificProperties((Agent) entity, jsonLdResource);
	    break;

	case Place:
	    putPlaceSpecificProperties((Place) entity, jsonLdResource);
	    break;

	case TimeSpan:
	    putTimeSpanSpecificProperties((TimeSpan) entity, jsonLdResource);
	    break;

	default:
	    break;
	}

    }

    private void putTimeSpanSpecificProperties(TimeSpan entity, JsonLdResource jsonLdResource) {
	putBaseEntityProperties((BaseEntity) entity, jsonLdResource);
	putStringArrayProperty(WebEntityFields.IS_NEXT_IN_SEQUENCE, entity.getIsNextInSequence(), jsonLdResource);
	
	putStringProperty(WebEntityFields.BEGIN, entity.getBegin(), jsonLdResource);
	putStringProperty(WebEntityFields.END, entity.getEnd(), jsonLdResource);
    }


    private void putPlaceSpecificProperties(Place entity, JsonLdResource jsonLdResource) {
	putBaseEntityProperties((BaseEntity) entity, jsonLdResource);

	if (entity.getLatitude() != null)
	    putStringProperty(WebEntityFields.LATITUDE, "" + entity.getLatitude(), jsonLdResource);
	if (entity.getLongitude() != null)
	    putStringProperty(WebEntityFields.LONGITUDE, "" + entity.getLongitude(), jsonLdResource);
	if (entity.getAltitude() != null)
	    putStringProperty(WebEntityFields.ALTITUDE, "" + entity.getAltitude(), jsonLdResource);

	putStringArrayProperty(WebEntityFields.IS_NEXT_IN_SEQUENCE, entity.getIsNextInSequence(), jsonLdResource);
    }

    private void putAgentSpecificProperties(Agent entity, JsonLdResource jsonLdResource) {

	putBaseEntityProperties((BaseEntity) entity, jsonLdResource);

	// Agent Props
	putMapOfStringProperty(WebEntityFields.NAME, entity.getName(), "", jsonLdResource);
	putMapOfReferencesProperty(WebEntityFields.BIOGRAPHICAL_INFORMATION, entity.getBiographicalInformation(), "",
		jsonLdResource);
	putMapOfReferencesProperty(WebEntityFields.PROFESSION_OR_OCCUPATION, entity.getProfessionOrOccupation(), "",
		jsonLdResource);

	putStringArrayProperty(WebEntityFields.DATE_OF_DEATH, entity.getDateOfDeath(), jsonLdResource);
	putStringArrayProperty(WebEntityFields.DATE_OF_BIRTH, entity.getDateOfBirth(), jsonLdResource);
	if (!StringUtils.isEmpty(entity.getDateOfEstablishment())) {
            ldResource.putProperty(WebEntityFields.DATE_OF_ESTABLISHMENT, entity.getDateOfEstablishment());
        }
	if (!StringUtils.isEmpty(entity.getDateOfTermination())) {
            ldResource.putProperty(WebEntityFields.DATE_OF_TERMINATION, entity.getDateOfTermination());
        }
	
	putStringArrayProperty(WebEntityFields.BEGIN, entity.getBegin(), jsonLdResource);
	putStringArrayProperty(WebEntityFields.END, entity.getEnd(), jsonLdResource);
	

	putMapOfReferencesProperty(WebEntityFields.PLACE_OF_BIRTH, entity.getPlaceOfBirth(), "", jsonLdResource);
	putMapOfReferencesProperty(WebEntityFields.PLACE_OF_DEATH, entity.getPlaceOfDeath(), "", jsonLdResource);

    }

    private void putOrganizationSpecificProperties(Organization entity, JsonLdResource jsonLdResource) {

	putBaseEntityProperties((BaseEntity) entity, jsonLdResource);

	// Organization properties
	putMapOfStringProperty(WebEntityFields.DESCRIPTION, entity.getDescription(), "", ldResource);

	putMapOfStringListProperty(WebEntityFields.ACRONYM, entity.getAcronym(), "", ldResource);

	if (!StringUtils.isEmpty(entity.getLogo())) {
	    ldResource.putProperty(createWikimediaResource(entity.getLogo(), WebEntityFields.FOAF_LOGO));
	}

	if (!StringUtils.isEmpty(entity.getHomepage()))
	    ldResource.putProperty(WebEntityFields.FOAF_HOMEPAGE, entity.getHomepage());

	if (entity.getPhone() != null)
	    putListProperty(WebEntityFields.FOAF_PHONE, entity.getPhone(), jsonLdResource);
	if (entity.getMbox() != null)
	    putListProperty(WebEntityFields.FOAF_MBOX, entity.getMbox(), jsonLdResource);

	if (entity.getEuropeanaRole() != null) {
	    // "en" is mandatory
	    List<String> europeanaRole = entity.getEuropeanaRole().get(WebEntityFields.LANGUAGE_EN);
	    putListProperty(WebEntityFields.EUROPEANA_ROLE, europeanaRole, ldResource);
	}

	if (entity.getOrganizationDomain() != null) {
	    // "en" is mandatory
	    List<String> europeanaDomain = entity.getOrganizationDomain().get(WebEntityFields.LANGUAGE_EN);
	    putListProperty(WebEntityFields.ORGANIZATION_DOMAIN, europeanaDomain, ldResource);
	}

	if (entity.getGeographicLevel() != null) {
	    // "en" is mandatory
	    String geoLevel = entity.getGeographicLevel().get(WebEntityFields.LANGUAGE_EN);
	    ldResource.putProperty(WebEntityFields.GEOGRAPHIC_LEVEL, geoLevel);
	}

	if (!StringUtils.isEmpty(entity.getCountry()))
	    ldResource.putProperty(WebEntityFields.COUNTRY, entity.getCountry());

	putAddressProperty(entity, ldResource);

    }

    private void putAddressProperty(Organization entity, JsonLdResource ldResource) {

	// locality or geolocation is expected
	if (StringUtils.isEmpty(entity.getLocality()) && StringUtils.isEmpty(entity.getHasGeo()))
	    return;

	// build address object (the (json) value of the hasAddress property)
	JsonLdPropertyValue vcardAddress = new JsonLdPropertyValue();
	// id is mapped to rdf:about
	vcardAddress.putProperty(new JsonLdProperty(WebEntityFields.ID, entity.getHasAddress()));
	vcardAddress.putProperty(new JsonLdProperty(WebEntityFields.TYPE, WebEntityFields.ADDRESS_TYPE));

	if (!StringUtils.isEmpty(entity.getStreetAddress()))
	    vcardAddress.putProperty(new JsonLdProperty(WebEntityFields.STREET_ADDRESS, entity.getStreetAddress()));
	if (!StringUtils.isEmpty(entity.getLocality()))
	    vcardAddress.putProperty(new JsonLdProperty(WebEntityFields.LOCALITY, entity.getLocality()));
	if (!StringUtils.isEmpty(entity.getRegion()))
	    vcardAddress.putProperty(new JsonLdProperty(WebEntityFields.REGION, entity.getRegion()));
	if (!StringUtils.isEmpty(entity.getPostalCode()))
	    vcardAddress.putProperty(new JsonLdProperty(WebEntityFields.POSTAL_CODE, entity.getPostalCode()));
	if (!StringUtils.isEmpty(entity.getCountryName()))
	    vcardAddress.putProperty(new JsonLdProperty(WebEntityFields.COUNTRY_NAME, entity.getCountryName()));
	if (!StringUtils.isEmpty(entity.getPostBox()))
	    vcardAddress.putProperty(new JsonLdProperty(WebEntityFields.POST_OFFICE_BOX, entity.getPostBox()));

	if (!StringUtils.isEmpty(entity.getHasGeo()))
	    vcardAddress
		    .putProperty(new JsonLdProperty(WebEntityFields.HAS_GEO, EntityUtils.toGeoUri(entity.getHasGeo())));

	JsonLdProperty hasAddress = new JsonLdProperty(WebEntityFields.HAS_ADDRESS);
	hasAddress.addValue(vcardAddress);
	ldResource.putProperty(hasAddress);
    }

    private void putAggregationProperty(Entity entity, JsonLdResource ldResource) {

	// created or modified is expected
	if (entity.getCreated() == null && entity.getModified() == null)
	    return;

	// build aggregation object (the (json) value of the isAggregatedBy property)
	JsonLdPropertyValue oreAggregation = new JsonLdPropertyValue();
	// id is mapped to rdf:about
	oreAggregation.putProperty(new JsonLdProperty(WebEntityFields.ID,
		entity.getEntityId() + "#" + WebEntityFields.AGGREGATION.toLowerCase()));
	oreAggregation.putProperty(new JsonLdProperty(WebEntityFields.TYPE, WebEntityFields.AGGREGATION));

	if (entity.getCreated() != null)
	    oreAggregation.putProperty(
		    new JsonLdProperty(WebEntityFields.CREATED, DateUtils.convertDateToStr(entity.getCreated())));
	if (entity.getModified() != null)
	    oreAggregation.putProperty(
		    new JsonLdProperty(WebEntityFields.MODIFIED, DateUtils.convertDateToStr(entity.getModified())));

	oreAggregation.putProperty(new JsonLdProperty(WebEntityFields.AGGREGATES, entity.getEntityId()));

	JsonLdProperty isAggregatedBy = new JsonLdProperty(WebEntityFields.IS_AGGREGATED_BY);
	isAggregatedBy.addValue(oreAggregation);
	ldResource.putProperty(isAggregatedBy);
    }

    private void putBaseEntityProperties(BaseEntity entity, JsonLdResource jsonLdResource) {
	// COMMON Entity PROPERTIES?
	putStringArrayProperty(WebEntityFields.IS_PART_OF, entity.getIsPartOf(), jsonLdResource);
	putStringArrayProperty(WebEntityFields.HAS_PART, entity.getHasPart(), jsonLdResource);
    }

    public JsonLdResource getLdResource() {
	return ldResource;
    }

    @Override
    public boolean isContainerProperty(String property) {
	// TODO Auto-generated method stub, overwrite this method as the super
	// implementation is
	return !WebEntityFields.IS_PART_OF.equals(property) && super.isContainerProperty(property);
    }
}
