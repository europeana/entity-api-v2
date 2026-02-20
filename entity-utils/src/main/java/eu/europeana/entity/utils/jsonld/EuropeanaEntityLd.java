package eu.europeana.entity.utils.jsonld;

import java.util.Optional;

import eu.europeana.api.commons_sb3.definitions.utils.DateUtils;
import eu.europeana.api.commons_sb3.definitions.vocabulary.CommonLdConstants;
import eu.europeana.entity.definitions.exceptions.UnsupportedEntityTypeException;
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
import static eu.europeana.api.commons_sb3.definitions.vocabulary.CommonLdConstants.context;
import static eu.europeana.api.commons_sb3.definitions.vocabulary.CommonLdConstants.id;
import static eu.europeana.api.commons_sb3.definitions.vocabulary.CommonLdConstants.type;

public class EuropeanaEntityLd extends JsonLd {

  JsonLdResource ldResource = new JsonLdResource();
  String entityIdBaseUrl;

  public EuropeanaEntityLd(Entity entity, String entityIdBaseUrl)
      throws UnsupportedEntityTypeException {
    super();
    this.entityIdBaseUrl = entityIdBaseUrl;
    setPropOrderComparator(new EntityJsonComparator());
    registerContainerProperty(biographicalInformation);
    registerContainerProperty(placeOfBirth);
    registerContainerProperty(placeOfDeath);
    setEntity(entity);
  }

  public JsonLdResource setEntity(Entity entity) throws UnsupportedEntityTypeException {
    setUseTypeCoercion(false);
    setUseCuries(true);
    setUsedNamespaces(namespacePrefixMap);

    ldResource.setSubject("");
    ldResource.putProperty(context, CommonLdConstants.ENTITY_CONTEXT);

    // common EntityProperties
    ldResource.putProperty(id, EntityUtils.replaceBaseUrlInId(entity.getEntityId(), entityIdBaseUrl));
    ldResource.putProperty(type, entity.getType());
    putStringArrayProperty(identifier, entity.getIdentifier(), ldResource);
    putStringArrayProperty(sameAs, entity.getSameAs(), ldResource);
    putStringArrayProperty(isRelatedTo, entity.getIsRelatedTo(), ldResource);

    if (!StringUtils.isEmpty(entity.getDepiction())) {
      ldResource.putProperty(createWikimediaResource(entity.getDepiction(), depiction));
    }

    // common SKOS_Properties
    putMapOfStringProperty(prefLabel, ((BaseEntity) entity).getPrefLabel(), "",
        ldResource);
    putMapOfStringListProperty(altLabel, entity.getAltLabel(), "", ldResource);
    putMapOfStringListProperty(hiddenLabel, entity.getHiddenLabel(), "",
        ldResource);
    putMapOfStringListProperty(note, entity.getNote(), "", ldResource);

    // common administrative information (created, modified)
    putAggregationProperty(entity, ldResource);

    // isShownBy
    if (!StringUtils.isEmpty(((BaseEntity) entity).getIsShownById())) {
      ldResource
          .putProperty(createIsShownByResource((BaseEntity) entity, isShownBy));
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
      isShownByValue.putProperty(new JsonLdProperty(id, entity.getIsShownById()));
      isShownByValue.putProperty(new JsonLdProperty(type, WebResource));
    }

    if (!StringUtils.isEmpty(entity.getIsShownBySource()))
      isShownByValue.putProperty(new JsonLdProperty(source, entity.getIsShownBySource()));

    if (!StringUtils.isEmpty(entity.getIsShownByThumbnail()))
      isShownByValue.putProperty(new JsonLdProperty(thumbnail, entity.getIsShownByThumbnail()));

    isShownByProperty.addValue(isShownByValue);
    return isShownByProperty;
  }

  private JsonLdProperty createWikimediaResource(String wikimediaCommonsId, String field) {

    JsonLdProperty depictionProperty = new JsonLdProperty(field);
    JsonLdPropertyValue depictionValue = new JsonLdPropertyValue();

    depictionValue.putProperty(new JsonLdProperty(id, wikimediaCommonsId));
    String sourceValue = EntityUtils.createWikimediaResourceString(wikimediaCommonsId);
    depictionValue.putProperty(new JsonLdProperty(source, sourceValue));

    depictionProperty.addValue(depictionValue);
    return depictionProperty;
  }

  private void putConceptSpecificProperties(Concept entity, JsonLdResource jsonLdResource) {
    putMapOfStringListProperty(notation, entity.getNotation(), "", jsonLdResource);
    putStringArrayProperty(related, entity.getRelated(), jsonLdResource);
    putStringArrayProperty(broader, entity.getBroader(), jsonLdResource);
    putStringArrayProperty(narrower, entity.getNarrower(), jsonLdResource);
    putStringArrayProperty(exactMatch, entity.getExactMatch(), jsonLdResource);
    putStringArrayProperty(closeMatch, entity.getCloseMatch(), jsonLdResource);
    putStringArrayProperty(broadMatch, entity.getBroadMatch(), jsonLdResource);
    putStringArrayProperty(narrowMatch, entity.getNarrowMatch(), jsonLdResource);
    putStringArrayProperty(relatedMatch, entity.getRelatedMatch(), jsonLdResource);
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
    putStringArrayProperty(isNextInSequence, entity.getIsNextInSequence(), jsonLdResource);
    putStringProperty(begin, entity.getBegin(), jsonLdResource);
    putStringProperty(end, entity.getEnd(), jsonLdResource);
  }


  private void putPlaceSpecificProperties(Place entity, JsonLdResource jsonLdResource) {
    putBaseEntityProperties((BaseEntity) entity, jsonLdResource);

    if (entity.getLatitude() != null)
      putStringProperty(lat, "" + entity.getLatitude(), jsonLdResource);
    if (entity.getLongitude() != null)
      putStringProperty(_long, "" + entity.getLongitude(), jsonLdResource);
    if (entity.getAltitude() != null)
      putStringProperty(alt, "" + entity.getAltitude(), jsonLdResource);

    putStringArrayProperty(isNextInSequence, entity.getIsNextInSequence(), jsonLdResource);
  }

  private void putAgentSpecificProperties(Agent entity, JsonLdResource jsonLdResource) {

    putBaseEntityProperties((BaseEntity) entity, jsonLdResource);

    // Agent Props
    putMapOfStringProperty(name, entity.getName(), "", jsonLdResource);
    putMapOfReferencesProperty(biographicalInformation,
        entity.getBiographicalInformation(), "", jsonLdResource);
    putMapOfReferencesProperty(professionOrOccupation,
        entity.getProfessionOrOccupation(), "", jsonLdResource);

    putStringArrayProperty(dateOfDeath, entity.getDateOfDeath(), jsonLdResource);
    putStringArrayProperty(dateOfBirth, entity.getDateOfBirth(), jsonLdResource);
    if (!StringUtils.isEmpty(entity.getDateOfEstablishment())) {
      ldResource.putProperty(dateOfEstablishment, entity.getDateOfEstablishment());
    }
    if (!StringUtils.isEmpty(entity.getDateOfTermination())) {
      ldResource.putProperty(dateOfTermination, entity.getDateOfTermination());
    }
    putStringArrayProperty(begin, entity.getBegin(), jsonLdResource);
    putStringArrayProperty(end, entity.getEnd(), jsonLdResource);
    putMapOfReferencesProperty(placeOfBirth, entity.getPlaceOfBirth(), "", jsonLdResource);
    putMapOfReferencesProperty(placeOfDeath, entity.getPlaceOfDeath(), "", jsonLdResource);

  }

  private void putOrganizationSpecificProperties(Organization entity,
      JsonLdResource jsonLdResource) {

    putBaseEntityProperties((BaseEntity) entity, jsonLdResource);

    // Organization properties
    putMapOfStringProperty(description, entity.getDescription(), "", ldResource);

    putMapOfStringListProperty(acronym, entity.getAcronym(), "", ldResource);

    if (!StringUtils.isEmpty(entity.getLogo())) {
      ldResource.putProperty(createWikimediaResource(entity.getLogo(), FOAF_LOGO));
    }

    if (!StringUtils.isEmpty(entity.getHomepage())) {
      ldResource.putProperty(FOAF_HOMEPAGE, entity.getHomepage());
    }

    if (entity.getPhone() != null) {
      putListProperty(FOAF_PHONE, entity.getPhone(), jsonLdResource);
    }
    if (entity.getMbox() != null) {
      putListProperty(FOAF_MBOX, entity.getMbox(), jsonLdResource);
    }

    if (entity.getEuropeanaRole() != null) {
      putListProperty(europeanaRole, entity.getEuropeanaRole(), jsonLdResource);
    }
    if (entity.getAggregatedVia() != null) {
      putListProperty(aggregatedVia, entity.getAggregatedVia(), jsonLdResource);
    }
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
      countryPropValue.putProperty(new JsonLdProperty(id, countryId.get()));
    }
    
    countryPropValue.putProperty(new JsonLdProperty(type, Place));
    JsonLdProperty prefLabelProp = buildMapOfStringsProperty(prefLabel, entity.getCountryLabel(), "");
    
    if(prefLabelProp != null) {
      countryPropValue.putProperty(prefLabelProp); 
    }

    JsonLdProperty countryField = new JsonLdProperty(country);
    countryField.addValue(countryPropValue);
    ldResource.putProperty(countryField);
  }


  private void putAddressProperty(Organization entity, JsonLdResource ldResource) {

    // locality or geolocation is expected
    if (StringUtils.isEmpty(entity.getLocality()) && StringUtils.isEmpty(entity.getHasGeo()))
      return;

    // build address object (the (json) value of the hasAddress property)
    JsonLdPropertyValue vcardAddress = new JsonLdPropertyValue();
    // id is mapped to rdf:about
    vcardAddress.putProperty(new JsonLdProperty(id, entity.getHasAddress()));
    vcardAddress
        .putProperty(new JsonLdProperty(type, Address));

    if (!StringUtils.isEmpty(entity.getStreetAddress()))
      vcardAddress.putProperty(
          new JsonLdProperty(streetAddress, entity.getStreetAddress()));
    if (!StringUtils.isEmpty(entity.getLocality()))
      vcardAddress.putProperty(new JsonLdProperty(locality, entity.getLocality()));
    if (!StringUtils.isEmpty(entity.getRegion()))
      vcardAddress.putProperty(new JsonLdProperty(region, entity.getRegion()));
    if (!StringUtils.isEmpty(entity.getPostalCode()))
      vcardAddress
          .putProperty(new JsonLdProperty(postalCode, entity.getPostalCode()));
    if (!StringUtils.isEmpty(entity.getCountryName()))
      vcardAddress
          .putProperty(new JsonLdProperty(countryName, entity.getCountryName()));
    if (!StringUtils.isEmpty(entity.getPostBox()))
      vcardAddress
          .putProperty(new JsonLdProperty(postOfficeBox, entity.getPostBox()));

    if (!StringUtils.isEmpty(entity.getHasGeo()))
      vcardAddress.putProperty(
          new JsonLdProperty(hasGeo, EntityUtils.toGeoUri(entity.getHasGeo())));

    JsonLdProperty hasAddressField = new JsonLdProperty(hasAddress);
    hasAddressField.addValue(vcardAddress);
    ldResource.putProperty(hasAddressField);
  }

  private void putAggregationProperty(Entity entity, JsonLdResource ldResource) {

    // created or modified is expected
    if (entity.getCreated() == null && entity.getModified() == null)
      return;

    // build aggregation object (the (json) value of the isAggregatedBy property)
    JsonLdPropertyValue oreAggregation = new JsonLdPropertyValue();
    // id is mapped to rdf:about
    oreAggregation.putProperty(new JsonLdProperty(id,
        entity.getEntityId() + "#" + Aggregation.toLowerCase()));
    oreAggregation
        .putProperty(new JsonLdProperty(type, Aggregation));

    if (entity.getCreated() != null)
      oreAggregation.putProperty(new JsonLdProperty(created,
          DateUtils.convertDateToStr(entity.getCreated())));
    if (entity.getModified() != null)
      oreAggregation.putProperty(new JsonLdProperty(modified,
          DateUtils.convertDateToStr(entity.getModified())));
    
    oreAggregation.putProperty(new JsonLdProperty(pageRank, entity.getPageRank()));
    oreAggregation.putProperty(new JsonLdProperty(recordCount, entity.getEuropeanaDocCount()));
    oreAggregation.putProperty(new JsonLdProperty(score, entity.getDerivedScore()));

    JsonLdProperty isAggregatedByField = new JsonLdProperty(isAggregatedBy);
    isAggregatedByField.addValue(oreAggregation);
    ldResource.putProperty(isAggregatedByField);
  }

  private void putBaseEntityProperties(BaseEntity entity, JsonLdResource jsonLdResource) {
    // COMMON Entity PROPERTIES?
    putStringArrayProperty(isPartOf, entity.getIsPartOf(), jsonLdResource);
    putStringArrayProperty(hasPart, entity.getHasPart(), jsonLdResource);
  }

  public JsonLdResource getLdResource() {
    return ldResource;
  }

  @Override
  public boolean isContainerProperty(String property) {
    // TODO Auto-generated method stub, overwrite this method as the super
    // implementation is
    return !isPartOf.equals(property) && super.isContainerProperty(property);
  }
}
