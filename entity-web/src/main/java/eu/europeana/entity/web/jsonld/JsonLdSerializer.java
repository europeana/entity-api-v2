package eu.europeana.entity.web.jsonld;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.europeana.entity.app.AppConfigConstants;
import eu.europeana.entity.web.controller.exception.EntityApiRuntimeException;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.Locale;

@Component(AppConfigConstants.BEAN_EM_JSONLD_SERIALIZER)
public class JsonLdSerializer {

    ObjectMapper mapper;
    public static final String DATE_FORMAT  = "yyyy-MM-dd'T'HH:mm:ss'Z'";

    public JsonLdSerializer() {
        mapper = new ObjectMapper();
        SimpleDateFormat df = new SimpleDateFormat(DATE_FORMAT, Locale.ENGLISH);
        mapper.setDateFormat(df);
    }

    public String serializeToJson(Object object) throws EntityApiRuntimeException {
        try {
            return mapper.writer().writeValueAsString(object);
        } catch (JsonProcessingException e) {
            throw new EntityApiRuntimeException("Unexpected exception occurred when serializing!",e);
        }
    }
}
