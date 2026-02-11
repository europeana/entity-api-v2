package eu.europeana.entity.web.service;

import eu.europeana.api.commons_sb3.definitions.oauth.Role;
import eu.europeana.api.commons_sb3.oauth2.service.authorization.AuthorizationService;
import eu.europeana.api.commons_sb3.oauth2.service.authorization.BaseAuthorizationService;
import jakarta.annotation.Resource;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.security.oauth2.provider.ClientDetailsService;
import org.springframework.stereotype.Component;
import eu.europeana.entity.config.AppConfigConstants;
import eu.europeana.entity.web.config.EntityWebConfig;
import eu.europeana.entity.web.model.vocabulary.UserRoles;

/**
 * Entity api v2 authorization service
 */
@Component(AppConfigConstants.BEAN_AUTHORIZATION_SERVICE)
public class EntityAuthorizationService extends BaseAuthorizationService implements AuthorizationService {

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
