package eu.europeana.entity.web.config;

import eu.europeana.api.commons_sb3.auth.AuthenticationBuilder;
import eu.europeana.api.commons_sb3.auth.AuthenticationConfig;
import eu.europeana.api.commons_sb3.error.config.ErrorConfig;
import eu.europeana.api.commons_sb3.error.i18n.I18nService;
import eu.europeana.api.commons_sb3.error.i18n.I18nServiceImpl;
import eu.europeana.api.commons_sb3.oauth2.service.impl.EuropeanaClientDetailsService;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import eu.europeana.entity.config.AppConfigConstants;
import java.nio.charset.StandardCharsets;

@Configuration
public class EntityAppConfig extends AppConfigConstants {

    private static final Logger LOG = LogManager.getLogger(EntityAppConfig.class);

    @Resource(name = AppConfigConstants.BEAN_WEB_CONFIG)
    private EntityWebConfig entityWebConfig;

    @Bean(name = BEAN_CLIENT_DETAILS_SERVICE)
    public EuropeanaClientDetailsService getClientDetailsService() {
        EuropeanaClientDetailsService clientDetailsService = new EuropeanaClientDetailsService();
        clientDetailsService.setApiKeyServiceUrl(entityWebConfig.getApiKeyServiceUrl());
        // Set authentication handler if values are not empty
        if (StringUtils.isNotEmpty(entityWebConfig.getTokenEndpoint()) && StringUtils.isNotEmpty(entityWebConfig.getGrantParams())) {
            AuthenticationConfig config = new AuthenticationConfig(entityWebConfig.getTokenEndpoint(), entityWebConfig.getGrantParams());
            clientDetailsService.setAuthHandler(AuthenticationBuilder.newAuthentication(config));
        } else {
            LOG.error("Keycloak token endpoint and parameters NOT set !!");
        }
        return clientDetailsService;
    }

    @Bean(name = ErrorConfig.BEAN_I18nService)
    public I18nService getI18nService() {
        ReloadableResourceBundleMessageSource messageSource = new ReloadableResourceBundleMessageSource();
        messageSource.setBasenames(ErrorConfig.COMMON_MESSAGE_SOURCE, "classpath:messages");
        messageSource.setDefaultEncoding(StandardCharsets.UTF_8.name());
        return  new I18nServiceImpl(messageSource);
    }
}
