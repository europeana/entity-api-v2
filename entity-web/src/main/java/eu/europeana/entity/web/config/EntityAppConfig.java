package eu.europeana.entity.web.config;

import javax.annotation.Resource;

import eu.europeana.api.commons.auth.AuthenticationBuilder;
import eu.europeana.api.commons.auth.AuthenticationConfig;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;

import eu.europeana.api.commons.config.i18n.I18nService;
import eu.europeana.api.commons.config.i18n.I18nServiceImpl;
import eu.europeana.api.commons.oauth2.service.impl.EuropeanaClientDetailsService;
import eu.europeana.entity.config.AppConfigConstants;

import java.util.Properties;

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

    @Bean(name = BEAN_I18N_SERVICE)
    public I18nService getI18nService() {
        I18nServiceImpl i18Service = new I18nServiceImpl();
        MessageSource source = getMessageSource();
        i18Service.setMessageSource(source);
        return i18Service;
    }

    @Bean(name = BEAN_I18N_MESAGE_SOURCE)
    public MessageSource getMessageSource() {
        ReloadableResourceBundleMessageSource source = new ReloadableResourceBundleMessageSource();
        source.setBasename("classpath:messages");
        source.setDefaultEncoding("utf-8");
        return source;
    }
    
}
