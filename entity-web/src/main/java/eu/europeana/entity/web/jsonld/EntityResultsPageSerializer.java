package eu.europeana.entity.web.jsonld;

import eu.europeana.api.commons_sb3.definitions.search.result.ResultsPage;
import eu.europeana.api.commons_sb3.definitions.search.result.ResultsPageSerializer;
import eu.europeana.entity.definitions.exceptions.UnsupportedEntityTypeException;
import eu.europeana.entity.definitions.model.Entity;
import eu.europeana.entity.utils.jsonld.EntityJsonComparator;
import eu.europeana.entity.utils.jsonld.EuropeanaEntityLd;
import eu.europeana.entity.web.exception.FunctionalRuntimeException;
import org.apache.stanbol.commons.jsonld.JsonLdProperty;
import org.apache.stanbol.commons.jsonld.JsonLdPropertyValue;
import org.apache.stanbol.commons.jsonld.JsonLdResource;

import java.util.Map;

import static eu.europeana.api.commons_sb3.definitions.vocabulary.CommonLdConstants.context;
import static eu.europeana.api.commons_sb3.definitions.vocabulary.CommonLdConstants.items;
import static eu.europeana.entity.definitions.model.vocabulary.WebEntityFields.isPartOf;

public class EntityResultsPageSerializer<T extends Entity> extends ResultsPageSerializer<T> {

    String entityIdBaseUrl;

    public EntityResultsPageSerializer(ResultsPage<T> resPage, String contextValue, String typeValue, String entityIdBaseUrl) {
        super(resPage, contextValue, typeValue);
        this.entityIdBaseUrl = entityIdBaseUrl;
        setPropOrderComparator(new EntityJsonComparator());
    }

    @Override
    protected void serializeItems(JsonLdResource jsonLdResource, String profile) {
        registerContainerProperty(items);

        if (getResultsPage().getItems() == null || getResultsPage().getItems().isEmpty())
            return;

        JsonLdProperty itemsProp = new JsonLdProperty(items);

        for (Entity entity : getResultsPage().getItems()) {
            serializeItem(itemsProp, entity);
        }
        jsonLdResource.putProperty(itemsProp);
    }

    private void serializeItem(JsonLdProperty itemsProp, Entity entity) {
        EuropeanaEntityLd entityLd;
        //transform annotation object to json-ld
        try {
            entityLd = new EuropeanaEntityLd(entity, entityIdBaseUrl);
        } catch (UnsupportedEntityTypeException e) {
            throw new FunctionalRuntimeException("Cannot serialize entity of type: " + entity.getType(), e);
        }

        //build property value for the given annotation
        JsonLdPropertyValue propertyValue = new JsonLdPropertyValue();
        Map<String, JsonLdProperty> propertyMap = propertyValue.getPropertyMap();
        Map<String, JsonLdProperty> entityProps = entityLd.getLdResource().getPropertyMap();
        //the context property must not be serialized for individual entities
        entityProps.remove(context);
        propertyMap.putAll(entityProps);
        itemsProp.addValue(propertyValue);
    }

    @Override
    public boolean isContainerProperty(String property) {
        // TODO Auto-generated method stub, overwrite this method as the super
        // implementation is
        return !isPartOf.equals(property) && super.isContainerProperty(property);
    }
}
