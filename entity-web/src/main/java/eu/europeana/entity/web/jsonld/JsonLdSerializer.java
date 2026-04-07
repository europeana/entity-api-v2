package eu.europeana.entity.web.jsonld;

import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Locale;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import eu.europeana.entity.config.AppConfigConstants;

@Component(AppConfigConstants.BEAN_EM_JSONLD_SERIALIZER)
public class JsonLdSerializer {

    public static final String DATE_FORMAT  = "yyyy-MM-dd'T'HH:mm:ss'Z'";

    ObjectMapper mapper;

    public JsonLdSerializer() {
        mapper = new ObjectMapper();
        mapper.findAndRegisterModules();
        SimpleDateFormat df = new SimpleDateFormat(DATE_FORMAT, Locale.ENGLISH);
        mapper.setDateFormat(df);
    }

    /**
     * Serializes the given object to the specified OutputStream in JSON-LD format.
     *
     * @param object the object to serialize
     * @param out the OutputStream to write the serialized JSON-LD data to
     * @throws IOException if an I/O error occurs during writing
     */
    public void write(Object object, OutputStream out) throws IOException {
            mapper.writerFor(object.getClass()).writeValue(out, object);
    }
}
