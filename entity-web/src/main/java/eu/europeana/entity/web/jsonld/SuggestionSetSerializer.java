package eu.europeana.entity.web.jsonld;

import eu.europeana.api.commons_sb3.definitions.search.ResultSet;
import eu.europeana.api.commons_sb3.error.EuropeanaI18nApiException;
import eu.europeana.entity.config.I18nConstants;
import eu.europeana.entity.definitions.exceptions.UnsupportedEntityTypeException;
import eu.europeana.entity.definitions.model.ResourcePreview;
import eu.europeana.entity.definitions.model.vocabulary.EntityTypes;
import eu.europeana.entity.definitions.model.vocabulary.WebEntityConstants;
import eu.europeana.entity.utils.EntityUtils;
import eu.europeana.entity.utils.jsonld.EntityJsonComparator;
import eu.europeana.entity.web.model.view.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.stanbol.commons.jsonld.JsonLd;
import org.apache.stanbol.commons.jsonld.JsonLdProperty;
import org.apache.stanbol.commons.jsonld.JsonLdPropertyValue;
import org.apache.stanbol.commons.jsonld.JsonLdResource;
import org.springframework.http.HttpStatus;

import java.util.Arrays;
import java.util.List;

import static eu.europeana.api.commons_sb3.definitions.vocabulary.CommonLdConstants.*;
import static eu.europeana.entity.definitions.model.vocabulary.WebEntityFields.*;

public class SuggestionSetSerializer extends JsonLd {

    public SuggestionSetSerializer() {
        super();
        setPropOrderComparator(new EntityJsonComparator());
    }

    ResultSet<? extends EntityPreview> entitySet;
    String entityIdBaseUrl;

    public ResultSet<? extends EntityPreview> getEntitySet() {
        return entitySet;
    }

    public void setConceptSet(ResultSet<? extends EntityPreview> entitySet) {
        this.entitySet = entitySet;
    }

    /**
     *
     */
    public SuggestionSetSerializer(ResultSet<? extends EntityPreview> entitySet, String entityIdBaseUrl) {
        super();
        this.entityIdBaseUrl = entityIdBaseUrl;
        setPropOrderComparator(new EntityJsonComparator());
        registerContainerProperty(isPartOf);
        registerContainerProperty(items);
        setConceptSet(entitySet);
    }

    /**
     * Adds the given concept to this JsonLd object using the resource's subject as
     * key. If the key is NULL and there does not exist a resource with an empty
     * String as key the resource will be added using an empty String ("") as key.
     *
     * @return serialised string
     * @throws EuropeanaI18nApiException exception thrown
     */
    public String serialize() throws EuropeanaI18nApiException {

        setUseTypeCoercion(false);
        setUseCuries(true);
        setUsedNamespaces(namespacePrefixMap);

        JsonLdResource jsonLdResource = new JsonLdResource();
        jsonLdResource.setSubject("");

        JsonLdProperty contextProperty = new JsonLdProperty(context);
        contextProperty.getValues().add(new JsonLdPropertyValue(WebEntityConstants.LDP_CONTEXT));
        contextProperty.getValues().add(new JsonLdPropertyValue(ENTITY_CONTEXT));

        jsonLdResource.putProperty(contextProperty);

        // TODO: update JSONLD output and add the @language:en to context

        jsonLdResource.putProperty(type, ResultPage);
        jsonLdResource.putProperty(total, getEntitySet().getResultSize());

        serializeItems(jsonLdResource);

        put(jsonLdResource);

        return toString(4);
    }

    protected void serializeItems(JsonLdResource jsonLdResource) throws EuropeanaI18nApiException {

        // do not serialize if empty
        if (getEntitySet().isEmpty())
            return;

        JsonLdProperty containsProperty = new JsonLdProperty(items);
        JsonLdPropertyValue propertyValue;

        for (EntityPreview entityPreview : getEntitySet().getResults()) {
            propertyValue = buildEntityPreviewPropertyValue(entityPreview);
            containsProperty.addValue(propertyValue);
        }

        jsonLdResource.putProperty(containsProperty);

    }

    /**
     * This method constructs isShownBy property
     *
     * @param entity
     * @param field
     * @return jsonLd property for isShownBy field
     */
    private JsonLdProperty createIsShownByResource(EntityPreview entity, String field) {

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

    private JsonLdPropertyValue buildEntityPreviewPropertyValue(EntityPreview entityPreview) throws EuropeanaI18nApiException {

        JsonLdPropertyValue entityPreviewPropValue = new JsonLdPropertyValue();

        // id
        String entityIdAdjusted = EntityUtils.replaceBaseUrlInId(entityPreview.getEntityId(), entityIdBaseUrl);
        entityPreviewPropValue.putProperty(new JsonLdProperty(id, entityIdAdjusted));

        JsonLdProperty prefLabelProp = buildMapOfStringsProperty(prefLabel,
                entityPreview.getPreferredLabel(), "");
        if (prefLabelProp != null) {
            entityPreviewPropValue.putProperty(prefLabelProp);
        }

        // altLabel
        if (entityPreview.getAltLabel() != null && !entityPreview.getAltLabel().isEmpty()) {
            JsonLdProperty altLabelProp = buildMapProperty(altLabel,
                    entityPreview.getAltLabel(), "");
            entityPreviewPropValue.putProperty(altLabelProp);
        }

        // hiddenLabel
        if (entityPreview.getHiddenLabel() != null && !entityPreview.getHiddenLabel().isEmpty()) {
            JsonLdProperty hiddenLabelProp = buildMapProperty(hiddenLabel,
                    entityPreview.getHiddenLabel(), "");
            entityPreviewPropValue.putProperty(hiddenLabelProp);
        }

        // depiction
        if (entityPreview.getDepiction() != null)
            entityPreviewPropValue
                    .putProperty(new JsonLdProperty(depiction, entityPreview.getDepiction()));
        //isShownBy
        if (!StringUtils.isEmpty((entityPreview).getIsShownById())) {
            entityPreviewPropValue.putProperty(createIsShownByResource(
                    entityPreview, isShownBy));
        }

        String type = entityPreview.getType();
        EntityTypes entityType = null;
        try {
            entityType = EntityTypes.getByInternalType(type);
        } catch (UnsupportedEntityTypeException e) {
            throw new EuropeanaI18nApiException(null, null, null, HttpStatus.NOT_FOUND,
					I18nConstants.UNSUPPORTED_ENTITY_TYPE,
					Arrays.asList(WebEntityConstants.ENTITY_API_RESOURCE, "", type),
					e);
        }

        if (entityType != null) {
            entityPreviewPropValue
                    .putProperty(new JsonLdProperty(type, entityType.getInternalType()));

            switch (entityType) {
                case Organization:
                case Aggregator:
                    putOrganizationSpecificProperties((OrganizationPreview) entityPreview, entityPreviewPropValue);
                    break;

                case Concept:
                    // add top concept, when available
                    break;

                case Agent:
                    putAgentSpecificProperties((AgentPreview) entityPreview, entityPreviewPropValue);
                    break;

                case Place:
                    putPlaceSpecificProperties((PlacePreview) entityPreview, entityPreviewPropValue);
                    break;

                case TimeSpan:
                    putTimeSpanSpecificProperties((TimeSpanPreview) entityPreview, entityPreviewPropValue);
                    break;

                default:
                    break;
            }
        }
        return entityPreviewPropValue;
    }

    private void putTimeSpanSpecificProperties(TimeSpanPreview entityPreview,
                                               JsonLdPropertyValue entityPreviewPropValue) {
        if (entityPreview.getBegin() != null)
            entityPreviewPropValue.putProperty(new JsonLdProperty(begin, entityPreview.getBegin()));

        if (entityPreview.getEnd() != null)
            entityPreviewPropValue.putProperty(new JsonLdProperty(end, entityPreview.getEnd()));
    }

    private void putPlaceSpecificProperties(PlacePreview entityPreview, JsonLdPropertyValue entityPreviewPropValue) {

        List<ResourcePreview> partOfList = entityPreview.getIsPartOf();
        JsonLdProperty prefLabelProp;

        if (partOfList != null && !partOfList.isEmpty()) {
            JsonLdProperty isPartOfProp = new JsonLdProperty(isPartOf);
            JsonLdPropertyValue propValue;
            for (ResourcePreview resourcePreview : partOfList) {
                propValue = new JsonLdPropertyValue();
                propValue.getValues().put(id, resourcePreview.getHttpUri());
                prefLabelProp = buildMapOfStringsProperty(prefLabel, resourcePreview.getPrefLabel(),
                        "");
                propValue.getPropertyMap().put(prefLabel, prefLabelProp);

                isPartOfProp.addValue(propValue);
            }

            entityPreviewPropValue.putProperty(isPartOfProp);
        }
    }

    private void putAgentSpecificProperties(AgentPreview entityPreview, JsonLdPropertyValue entityPreviewPropValue) {
        if (entityPreview.getDateOfBirth() != null) {
            entityPreviewPropValue
                    .putProperty(new JsonLdProperty(dateOfBirth, entityPreview.getDateOfBirth()));
        }

        if (entityPreview.getDateOfDeath() != null){
            entityPreviewPropValue
                    .putProperty(new JsonLdProperty(dateOfDeath, entityPreview.getDateOfDeath()));
        }

        if (entityPreview.getProfessionOrOccuation() != null && !entityPreview.getProfessionOrOccuation().isEmpty()) {
            entityPreviewPropValue.putProperty(buildMapProperty(professionOrOccupation,
                    entityPreview.getProfessionOrOccuation(), ""));
        }
    }

    private void putOrganizationSpecificProperties(OrganizationPreview entityPreview,
                                                   JsonLdPropertyValue entityPreviewPropValue) {
        if (entityPreview.getAcronym() != null && !entityPreview.getAcronym().isEmpty()) {
            entityPreviewPropValue.putProperty(buildMapProperty(acronym, entityPreview.getAcronym(), null));
        }

        if (entityPreview.getCountry() != null) {
            entityPreviewPropValue.putProperty(new JsonLdProperty(country, entityPreview.getCountry()));
        }

        if (entityPreview.getOrganizationDomain() != null) {
            entityPreviewPropValue.putProperty(new JsonLdProperty(organizationDomain, entityPreview.getOrganizationDomain()));
        }

    }
}