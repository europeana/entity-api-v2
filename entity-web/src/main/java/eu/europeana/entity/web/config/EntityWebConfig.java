package eu.europeana.entity.web.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;

@Configuration
@PropertySources({ @PropertySource(value = "classpath:entity.properties", ignoreResourceNotFound = true),
    @PropertySource(value = "classpath:entity.user.properties", ignoreResourceNotFound = true) })

public class EntityWebConfig{

    @Value("${europeana.apikey.jwttoken.siganturekey:#{null}}")
    String jwtSignatureKey;
    @Value("${authorization.api.name:entities}")
    String authorizationApiName;
    @Value("${europeana.apikey.serviceurl:#{null}}")
    String apiKeyServiceUrl;
    @Value("${entity.api.endpoint:#{null}}")
    String entityApiEndpoint;
    @Value("${entity.data.endpoint:#{null}}")
    String entityDataEndpoint;
    
    public String getJwtSignatureKey() {
        return jwtSignatureKey;
    }

    public String getAuthorizationApiName() {
        return authorizationApiName;
    }

    public String getApiKeyServiceUrl() {
        return apiKeyServiceUrl;
    }

    public String getEntityApiEndpoint() {
      return entityApiEndpoint;
    }

    public String getEntityDataEndpoint() {
      return entityDataEndpoint;
    }

}
