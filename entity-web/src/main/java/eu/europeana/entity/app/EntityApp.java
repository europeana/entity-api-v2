package eu.europeana.entity.app;

import java.util.Arrays;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.actuate.autoconfigure.metrics.mongo.MongoMetricsAutoConfiguration;
import org.springframework.boot.actuate.autoconfigure.security.servlet.ManagementWebSecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.mongo.MongoDataAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.boot.autoconfigure.mongo.embedded.EmbeddedMongoAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.web.embedded.EmbeddedWebServerFactoryCustomizerAutoConfiguration;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.ApplicationContext;

/**
 * Main application. Allows deploying as a war and logs instance data when deployed in Cloud Foundry
 */
@SpringBootApplication(scanBasePackages = {"eu.europeana.entity"}, exclude = {
    // Remove these exclusions to re-enable security
    SecurityAutoConfiguration.class, ManagementWebSecurityAutoConfiguration.class,
    // DataSources are manually configured (for EM and batch DBs),
    EmbeddedMongoAutoConfiguration.class, EmbeddedWebServerFactoryCustomizerAutoConfiguration.class,
    MongoAutoConfiguration.class, MongoDataAutoConfiguration.class,
    MongoMetricsAutoConfiguration.class, DataSourceAutoConfiguration.class})
public class EntityApp extends SpringBootServletInitializer {

  private static final Logger logger = LogManager.getLogger(EntityApp.class);

  /**
   * Main entry point of this application
   * 
   * @param args command-line arguments
   */
  public static void main(String[] args) {
    LogManager.getLogger(EntityApp.class).info("Configure Spring Application!");
    ApplicationContext ctx = SpringApplication.run(EntityApp.class, args);
    if (logger.isDebugEnabled()) {
      printRegisteredBeans(ctx);
    }
  }
  

  private static void printRegisteredBeans(ApplicationContext ctx) {
    String[] beanNames = ctx.getBeanDefinitionNames();

    Arrays.sort(beanNames);
    logger.debug("Instantiated beans:");
    logger.debug(StringUtils.join(beanNames, "\n"));
  }    
}
