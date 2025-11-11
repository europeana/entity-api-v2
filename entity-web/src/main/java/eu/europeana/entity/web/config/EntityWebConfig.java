package eu.europeana.entity.web.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;

@Configuration
@PropertySources({ 
  @PropertySource(value = "classpath:entity.properties", ignoreResourceNotFound = true),
  @PropertySource(value = "classpath:entity-solr.properties", ignoreResourceNotFound = true),
  @PropertySource(value = "entity.user.properties", ignoreResourceNotFound = true)
  })
public class EntityWebConfig {

  @Value("${europeana.apikey.jwttoken.siganturekey:#{null}}")
  String jwtSignatureKey;
  @Value("${authorization.api.name:entities}")
  String authorizationApiName;

  @Value("${europeana.apikey.serviceurl:#{null}}")
  String apiKeyServiceUrl;

  @Value("${keycloak.token.endpoint}")
  private String tokenEndpoint;

  @Value("${keycloak.token.grant.params}")
  private String grantParams;

  @Value("${entity.api.endpoint:#{null}}")
  String entityApiEndpoint;
  @Value("${entity.data.endpoint:#{null}}")
  String entityDataEndpoint;
  @Value("${entity.id.baseurl:http://data.europeana.eu}")
  String entityIdBaseUrl;


  public String getEntityIdBaseUrl() {
    return entityIdBaseUrl;
  }

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

  public String getTokenEndpoint() {
    return tokenEndpoint;
  }

  public String getGrantParams() {
    return grantParams;
  }
}
