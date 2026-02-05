package eu.europeana.entity.utils.jsonld;

import java.util.Optional;

import eu.europeana.api.commons_sb3.definitions.utils.DateUtils;
import eu.europeana.api.commons_sb3.definitions.vocabulary.CommonLdConstants;
import eu.europeana.entity.definitions.exceptions.UnsupportedEntityTypeException;
import eu.europeana.entity.definitions.model.vocabulary.WebEntityConstants;
import org.apache.commons.lang3.StringUtils;
import org.apache.stanbol.commons.jsonld.JsonLd;
import org.apache.stanbol.commons.jsonld.JsonLdProperty;
import org.apache.stanbol.commons.jsonld.JsonLdPropertyValue;
import org.apache.stanbol.commons.jsonld.JsonLdResource;
import eu.europeana.entity.definitions.model.Agent;
import eu.europeana.entity.definitions.model.Concept;
import eu.europeana.entity.definitions.model.Entity;
import eu.europeana.entity.definitions.model.Organization;
import eu.europeana.entity.definitions.model.Place;
import eu.europeana.entity.definitions.model.TimeSpan;
import eu.europeana.entity.definitions.model.impl.BaseEntity;
import eu.europeana.entity.definitions.model.vocabulary.EntityTypes;
import eu.europeana.entity.utils.EntityUtils;

import static eu.europeana.entity.definitions.model.vocabulary.WebEntityFields.*;

public class EuropeanaEntityLd extends JsonLd {

  JsonLdResource ldResource = new JsonLdResource();
  String entityIdBaseUrl;

  public EuropeanaEntityLd(Entity entity, String entityIdBaseUrl)
      throws UnsupportedEntityTypeException {
    super();
    this.entityIdBaseUrl = entityIdBaseUrl;
    setPropOrderComparator(new EntityJsonComparator());
    registerContainerProperty(BIOGRAPHICAL_INFORMATION);
    registerContainerProperty(PLACE_OF_BIRTH);
    registerContainerProperty(PLACE_OF_DEATH);
    setEntity(entity);
  }

  public JsonLdResource setEntity(Entity entity) throws UnsupportedEntityTypeException {
    setUseTypeCoercion(false);
    setUseCuries(true);
    setUsedNamespaces(namespacePrefixMap);

    ldResource.setSubject("");
    ldResource.putProperty(CommonLdConstants.context, CommonLdConstants.ENTITY_CONTEXT);

    // common EntityProperties
    ldResource.putProperty(ID,
        EntityUtils.replaceBaseUrlInId(entity.getEntityId(), entityIdBaseUrl));
    ldResource.putProperty(TYPE, entity.getType());
    putStringArrayProperty(IDENTIFIER, entity.getIdentifier(), ldResource);
    putStringArrayProperty(SAME_AS, entity.getSameAs(), ldResource);
    putStringArrayProperty(IS_RELATED_TO, entity.getIsRelatedTo(), ldResource);

    if (!StringUtils.isEmpty(entity.getDepiction())) {
      ldResource
          .putProperty(createWikimediaResource(entity.getDepiction(), DEPICTION));
    }

    // common SKOS_Properties
    putMapOfStringProperty(PREF_LABEL, ((BaseEntity) entity).getPrefLabel(), "",
        ldResource);
    putMapOfStringListProperty(ALT_LABEL, entity.getAltLabel(), "", ldResource);
    putMapOfStringListProperty(HIDDEN_LABEL, entity.getHiddenLabel(), "",
        ldResource);
    putMapOfStringListProperty(NOTE, entity.getNote(), "", ldResource);

    // common administrative information (created, modified)
    putAggregationProperty(entity, ldResource);

    // isShownBy
    if (!StringUtils.isEmpty(((BaseEntity) entity).getIsShownById())) {
      ldResource
          .putProperty(createIsShownByResource((BaseEntity) entity, IS_SHOWN_BY));
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
      isShownByValue.putProperty(new JsonLdProperty(ID, entity.getIsShownById()));
      isShownByValue
          .putProperty(new JsonLdProperty(TYPE, WEB_RESOURCE));
    }

    if (!StringUtils.isEmpty(entity.getIsShownBySource()))
      isShownByValue
          .putProperty(new JsonLdProperty(SOURCE, entity.getIsShownBySource()));

    if (!StringUtils.isEmpty(entity.getIsShownByThumbnail()))
      isShownByValue.putProperty(
          new JsonLdProperty(THUMBNAIL, entity.getIsShownByThumbnail()));

    isShownByProperty.addValue(isShownByValue);
    return isShownByProperty;
  }

  private JsonLdProperty createWikimediaResource(String wikimediaCommonsId, String field) {

    JsonLdProperty depictionProperty = new JsonLdProperty(field);
    JsonLdPropertyValue depictionValue = new JsonLdPropertyValue();

    depictionValue.putProperty(new JsonLdProperty(ID, wikimediaCommonsId));
    String sourceValue = EntityUtils.createWikimediaResourceString(wikimediaCommonsId);
    depictionValue.putProperty(new JsonLdProperty(SOURCE, sourceValue));

    depictionProperty.addValue(depictionValue);
    return depictionProperty;
  }

  private void putConceptSpecificProperties(Concept entity, JsonLdResource jsonLdResource) {
    putMapOfStringListProperty(NOTATION, entity.getNotation(), "", jsonLdResource);
    putStringArrayProperty(RELATED, entity.getRelated(), jsonLdResource);
    putStringArrayProperty(BROADER, entity.getBroader(), jsonLdResource);
    putStringArrayProperty(NARROWER, entity.getNarrower(), jsonLdResource);
    putStringArrayProperty(EXACT_MATCH, entity.getExactMatch(), jsonLdResource);
    putStringArrayProperty(CLOSE_MATCH, entity.getCloseMatch(), jsonLdResource);
    putStringArrayProperty(BROAD_MATCH, entity.getBroadMatch(), jsonLdResource);
    putStringArrayProperty(NARROW_MATCH, entity.getNarrowMatch(), jsonLdResource);
    putStringArrayProperty(RELATED_MATCH, entity.getRelatedMatch(), jsonLdResource);
  }

  private void putSpecificProperties(Entity entity, JsonLdResource jsonLdResource)
      throws UnsupportedEntityTypeException {

    EntityTypes entityType = EntityTypes.getByInternalType(entity.getType());

    switch (entityType) {
      case Organization, Aggregator:
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
    putStringArrayProperty(IS_NEXT_IN_SEQUENCE, entity.getIsNextInSequence(),
        jsonLdResource);

    putStringProperty(BEGIN, entity.getBegin(), jsonLdResource);
    putStringProperty(END, entity.getEnd(), jsonLdResource);
  }


  private void putPlaceSpecificProperties(Place entity, JsonLdResource jsonLdResource) {
    putBaseEntityProperties((BaseEntity) entity, jsonLdResource);

    if (entity.getLatitude() != null)
      putStringProperty(LATITUDE, "" + entity.getLatitude(), jsonLdResource);
    if (entity.getLongitude() != null)
      putStringProperty(LONGITUDE, "" + entity.getLongitude(), jsonLdResource);
    if (entity.getAltitude() != null)
      putStringProperty(ALTITUDE, "" + entity.getAltitude(), jsonLdResource);

    putStringArrayProperty(IS_NEXT_IN_SEQUENCE, entity.getIsNextInSequence(),
        jsonLdResource);
  }

  private void putAgentSpecificProperties(Agent entity, JsonLdResource jsonLdResource) {

    putBaseEntityProperties((BaseEntity) entity, jsonLdResource);

    // Agent Props
    putMapOfStringProperty(NAME, entity.getName(), "", jsonLdResource);
    putMapOfReferencesProperty(BIOGRAPHICAL_INFORMATION,
        entity.getBiographicalInformation(), "", jsonLdResource);
    putMapOfReferencesProperty(PROFESSION_OR_OCCUPATION,
        entity.getProfessionOrOccupation(), "", jsonLdResource);

    putStringArrayProperty(DATE_OF_DEATH, entity.getDateOfDeath(), jsonLdResource);
    putStringArrayProperty(DATE_OF_BIRTH, entity.getDateOfBirth(), jsonLdResource);
    if (!StringUtils.isEmpty(entity.getDateOfEstablishment())) {
      ldResource.putProperty(DATE_OF_ESTABLISHMENT,
          entity.getDateOfEstablishment());
    }
    if (!StringUtils.isEmpty(entity.getDateOfTermination())) {
      ldResource.putProperty(DATE_OF_TERMINATION, entity.getDateOfTermination());
    }

    putStringArrayProperty(BEGIN, entity.getBegin(), jsonLdResource);
    putStringArrayProperty(END, entity.getEnd(), jsonLdResource);


    putMapOfReferencesProperty(PLACE_OF_BIRTH, entity.getPlaceOfBirth(), "",
        jsonLdResource);
    putMapOfReferencesProperty(PLACE_OF_DEATH, entity.getPlaceOfDeath(), "",
        jsonLdResource);

  }

  private void putOrganizationSpecificProperties(Organization entity,
      JsonLdResource jsonLdResource) {

    putBaseEntityProperties((BaseEntity) entity, jsonLdResource);

    // Organization properties
    putMapOfStringProperty(DESCRIPTION, entity.getDescription(), "", ldResource);

    putMapOfStringListProperty(ACRONYM, entity.getAcronym(), "", ldResource);

    if (!StringUtils.isEmpty(entity.getLogo())) {
      ldResource.putProperty(createWikimediaResource(entity.getLogo(), FOAF_LOGO));
    }

    if (!StringUtils.isEmpty(entity.getHomepage()))
      ldResource.putProperty(FOAF_HOMEPAGE, entity.getHomepage());

    if (entity.getPhone() != null)
      putListProperty(FOAF_PHONE, entity.getPhone(), jsonLdResource);
    if (entity.getMbox() != null)
      putListProperty(FOAF_MBOX, entity.getMbox(), jsonLdResource);

    if (entity.getEuropeanaRole() != null) {
      putListProperty(EUROPEANA_ROLE, entity.getEuropeanaRole(), jsonLdResource);
    }
    
    if (entity.getAggregatedVia() != null) {
      putListProperty(AGGREGATED_VIA, entity.getAggregatedVia(), jsonLdResource);
    }

    // putMapOfStringProperty(DESCRIPTION, entity.getDescription(), "", ldResource);

    putCountryProperty(entity, ldResource);
    putAddressProperty(entity, ldResource);

  }

  private void putCountryProperty(Organization entity, JsonLdResource ldResource) {

    if (entity.getCountry() == null) {
      return;
    }

    // build country object (the json represenation for the country field)
    JsonLdPropertyValue countryPropValue = new JsonLdPropertyValue();
    // id is extracted from country 
    Optional<String> countryId = entity.getCountry().stream().filter(c -> c.startsWith("http")).findFirst();
    if(countryId.isPresent()) {
      countryPropValue.putProperty(new JsonLdProperty(ID, countryId.get()));
    }
    
    countryPropValue.putProperty(new JsonLdProperty(TYPE, TYPE_PLACE));
    JsonLdProperty prefLabelProp = buildMapOfStringsProperty(PREF_LABEL,
        entity.getCountryLabel(), "");
    
    if(prefLabelProp != null) {
      countryPropValue.putProperty(prefLabelProp); 
    }

    JsonLdProperty country = new JsonLdProperty(COUNTRY);
    country.addValue(countryPropValue);
    ldResource.putProperty(country);
  }


  private void putAddressProperty(Organization entity, JsonLdResource ldResource) {

    // locality or geolocation is expected
    if (StringUtils.isEmpty(entity.getLocality()) && StringUtils.isEmpty(entity.getHasGeo()))
      return;

    // build address object (the (json) value of the hasAddress property)
    JsonLdPropertyValue vcardAddress = new JsonLdPropertyValue();
    // id is mapped to rdf:about
    vcardAddress.putProperty(new JsonLdProperty(ID, entity.getHasAddress()));
    vcardAddress
        .putProperty(new JsonLdProperty(TYPE, ADDRESS_TYPE));

    if (!StringUtils.isEmpty(entity.getStreetAddress()))
      vcardAddress.putProperty(
          new JsonLdProperty(STREET_ADDRESS, entity.getStreetAddress()));
    if (!StringUtils.isEmpty(entity.getLocality()))
      vcardAddress.putProperty(new JsonLdProperty(LOCALITY, entity.getLocality()));
    if (!StringUtils.isEmpty(entity.getRegion()))
      vcardAddress.putProperty(new JsonLdProperty(REGION, entity.getRegion()));
    if (!StringUtils.isEmpty(entity.getPostalCode()))
      vcardAddress
          .putProperty(new JsonLdProperty(POSTAL_CODE, entity.getPostalCode()));
    if (!StringUtils.isEmpty(entity.getCountryName()))
      vcardAddress
          .putProperty(new JsonLdProperty(COUNTRY_NAME, entity.getCountryName()));
    if (!StringUtils.isEmpty(entity.getPostBox()))
      vcardAddress
          .putProperty(new JsonLdProperty(POST_OFFICE_BOX, entity.getPostBox()));

    if (!StringUtils.isEmpty(entity.getHasGeo()))
      vcardAddress.putProperty(
          new JsonLdProperty(HAS_GEO, EntityUtils.toGeoUri(entity.getHasGeo())));

    JsonLdProperty hasAddress = new JsonLdProperty(HAS_ADDRESS);
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
    oreAggregation.putProperty(new JsonLdProperty(ID,
        entity.getEntityId() + "#" + AGGREGATION.toLowerCase()));
    oreAggregation
        .putProperty(new JsonLdProperty(TYPE, AGGREGATION));

    if (entity.getCreated() != null)
      oreAggregation.putProperty(new JsonLdProperty(CREATED,
          DateUtils.convertDateToStr(entity.getCreated())));
    if (entity.getModified() != null)
      oreAggregation.putProperty(new JsonLdProperty(MODIFIED,
          DateUtils.convertDateToStr(entity.getModified())));
    
    oreAggregation.putProperty(new JsonLdProperty(PAGE_RANK, entity.getPageRank()));
    oreAggregation.putProperty(new JsonLdProperty(RECORD_COUNT, entity.getEuropeanaDocCount()));
    oreAggregation.putProperty(new JsonLdProperty(SCORE, entity.getDerivedScore()));

    JsonLdProperty isAggregatedBy = new JsonLdProperty(IS_AGGREGATED_BY);
    isAggregatedBy.addValue(oreAggregation);
    ldResource.putProperty(isAggregatedBy);
  }

  private void putBaseEntityProperties(BaseEntity entity, JsonLdResource jsonLdResource) {
    // COMMON Entity PROPERTIES?
    putStringArrayProperty(IS_PART_OF, entity.getIsPartOf(), jsonLdResource);
    putStringArrayProperty(HAS_PART, entity.getHasPart(), jsonLdResource);
  }

  public JsonLdResource getLdResource() {
    return ldResource;
  }

  @Override
  public boolean isContainerProperty(String property) {
    // TODO Auto-generated method stub, overwrite this method as the super
    // implementation is
    return !IS_PART_OF.equals(property) && super.isContainerProperty(property);
  }
}
