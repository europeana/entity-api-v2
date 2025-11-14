package eu.europeana.entity.web.jsonld;

import java.text.SimpleDateFormat;
import java.util.Locale;

import eu.europeana.api.commons.error.EuropeanaApiException;
import eu.europeana.api.commons.error.EuropeanaI18nApiException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.europeana.entity.app.I18nConstants;
import eu.europeana.entity.config.AppConfigConstants;

@Component(AppConfigConstants.BEAN_EM_JSONLD_SERIALIZER)
public class JsonLdSerializer {

    ObjectMapper mapper;
    public static final String DATE_FORMAT  = "yyyy-MM-dd'T'HH:mm:ss'Z'";

    public JsonLdSerializer() {
        mapper = new ObjectMapper();
        SimpleDateFormat df = new SimpleDateFormat(DATE_FORMAT, Locale.ENGLISH);
        mapper.setDateFormat(df);
    }

    public String serializeToJson(Object object) throws EuropeanaApiException {
        try {
            return mapper.writer().writeValueAsString(object);
        } catch (JsonProcessingException e) {
            throw new EuropeanaI18nApiException(
                "Cannot serialize object!",
                I18nConstants.SERVER_ERROR_CANT_SERIALIZE_OBJECT,
                HttpStatus.INTERNAL_SERVER_ERROR,
                I18nConstants.SERVER_ERROR_CANT_SERIALIZE_OBJECT,
                new String[] {object.toString()}, 
                e);
        }
    }
}
