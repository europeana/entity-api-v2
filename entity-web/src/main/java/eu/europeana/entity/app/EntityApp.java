package eu.europeana.entity.app;

import org.apache.logging.log4j.LogManager;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.actuate.autoconfigure.security.servlet.ManagementWebSecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

import eu.europeana.entity.web.config.EntitySocksProxyActivator;

/**
 * Main application. Allows deploying as a war and logs instance data when
 * deployed in Cloud Foundry
 */
@SpringBootApplication(scanBasePackages = { "eu.europeana.entity" }, exclude = {
        // Remove these exclusions to re-enable security
        SecurityAutoConfiguration.class, ManagementWebSecurityAutoConfiguration.class,
        // DataSources are manually configured (for EM and batch DBs)
        DataSourceAutoConfiguration.class })
public class EntityApp extends SpringBootServletInitializer {

    /**
     * Main entry point of this application
     * 
     * @param args command-line arguments
     */
    public static void main(String[] args) {
        LogManager.getLogger(EntityApp.class).info("Configure Spring Application!");
        // When deploying to Cloud Foundry, this will log the instance index number, IP
        // and GUID
        LogManager.getLogger(EntityApp.class).info(
                "CF_INSTANCE_INDEX  = {}, CF_INSTANCE_GUID = {}, CF_INSTANCE_IP  = {}",
                System.getenv("CF_INSTANCE_INDEX"), System.getenv("CF_INSTANCE_GUID"), System.getenv("CF_INSTANCE_IP"));

        registerSocksProxy();
        SpringApplication.run(EntityApp.class, args);
    }

    protected static void registerSocksProxy() {
        // Activate socks proxy (if your application requires it)
        LogManager.getLogger(EntityApp.class).info("Configure SocksProxy...");
        EntitySocksProxyActivator.activate(new SocksProxyConfig("entity.properties", "entity.user.properties"));
    }

}
