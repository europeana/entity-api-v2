package eu.europeana.entity.web.config;

import javax.annotation.Resource;

import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;

import eu.europeana.api.commons.config.i18n.I18nService;
import eu.europeana.api.commons.config.i18n.I18nServiceImpl;
import eu.europeana.api.commons.oauth2.service.impl.EuropeanaClientDetailsService;
import eu.europeana.entity.config.AppConfigConstants;

@Configuration
public class EntityAppConfig extends AppConfigConstants {

    @Resource(name = AppConfigConstants.BEAN_WEB_CONFIG)
    private EntityWebConfig entityWebConfig;

    @Bean(name = BEAN_CLIENT_DETAILS_SERVICE)
    public EuropeanaClientDetailsService getClientDetailsService() {
        EuropeanaClientDetailsService clientDetailsService = new EuropeanaClientDetailsService();
        clientDetailsService.setApiKeyServiceUrl(entityWebConfig.getApiKeyServiceUrl());
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
