package com.luxoft.sample;

import org.flywaydb.core.Flyway;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.flyway.FlywayMigrationInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import com.luxoft.sample.resolver.CustomFlywayMigrationResolver;

import org.flywaydb.core.internal.util.Location;
import org.flywaydb.core.internal.util.scanner.Scanner;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.ApplicationContext;

@Configuration
@ComponentScan({"com.luxoft.db.migration"})
public class FlywayConfig {

	private static Logger log = LoggerFactory.getLogger(FlywayConfig.class);
    
    @Bean
    public FlywayMigrationInitializer flywayInitializer(Flyway flyway) {
//        flyway.setCallbacks(flywayCallback());
        return new FlywayMigrationInitializer(flyway);
    }

    @Bean
    public BeanPostProcessor postProcessFlyway(ApplicationContext context) {
        return new BeanPostProcessor() {

            @Override
            public Object postProcessBeforeInitialization(Object o, String s) throws BeansException {
                return o;
            }

            @Override
            public Object postProcessAfterInitialization(Object o, String s) throws BeansException {
                if (o instanceof Flyway) {
                    Flyway flyway = (Flyway) o;
                    flyway.setSkipDefaultResolvers(true);
                    
                    log.debug("postProcessAfterInitialization. creating custom resolver");
                    
                    CustomFlywayMigrationResolver resolver = new CustomFlywayMigrationResolver(
                            new Scanner(Thread.currentThread().getContextClassLoader()),
                            new Location("com/luxoft/db/migration"),
                            context.getBean(org.flywaydb.core.api.configuration.FlywayConfiguration.class),
                            context);
                    
                    //TODO: re-create flyway SQL resolver to resolve SQL migrations
                    
                    flyway.setResolvers(resolver);
                }
                return o;
            }
        };
    }    
    
    
}
