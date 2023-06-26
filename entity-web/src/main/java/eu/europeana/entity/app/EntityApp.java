package eu.europeana.entity.app;

import org.apache.logging.log4j.LogManager;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.actuate.autoconfigure.security.servlet.ManagementWebSecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

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
        SpringApplication.run(EntityApp.class, args);
    }
}
