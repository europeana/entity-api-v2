package eu.europeana.entity.web.service;

import javax.annotation.Resource;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.security.oauth2.provider.ClientDetailsService;
import org.springframework.stereotype.Component;

import eu.europeana.api.commons.definitions.vocabulary.Role;
import eu.europeana.api.commons.service.authorization.BaseAuthorizationService;
import eu.europeana.entity.config.AppConfigConstants;
import eu.europeana.entity.web.config.EntityWebConfig;
import eu.europeana.entity.web.model.vocabulary.UserRoles;

@Component(AppConfigConstants.BEAN_AUTHORIZATION_SERVICE)
public class EntityAuthorizationService extends BaseAuthorizationService implements eu.europeana.api.commons.service.authorization.AuthorizationService {

    protected final Logger logger = LogManager.getLogger(getClass());

    @Resource(name = AppConfigConstants.BEAN_WEB_CONFIG)
    private EntityWebConfig entityWebConfig;
   
    @Resource(name = AppConfigConstants.BEAN_CLIENT_DETAILS_SERVICE)
    private ClientDetailsService clientDetailsService; 

    @Override
    protected ClientDetailsService getClientDetailsService() {
	return clientDetailsService;
    }

    @Override
    protected String getSignatureKey() {
	return entityWebConfig.getJwtSignatureKey();
    }


    @Override
    protected String getApiName() {
	return entityWebConfig.getAuthorizationApiName();
    }

	@Override
	protected Role getRoleByName(String name) {
            return UserRoles.getRoleByName(name);
	}
    
}
