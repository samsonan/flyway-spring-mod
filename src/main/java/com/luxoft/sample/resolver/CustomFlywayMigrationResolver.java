package com.luxoft.sample.resolver;

import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.api.MigrationType;
import org.flywaydb.core.api.MigrationVersion;
import org.flywaydb.core.api.configuration.FlywayConfiguration;
import org.flywaydb.core.api.migration.MigrationChecksumProvider;
import org.flywaydb.core.api.migration.MigrationInfoProvider;
import org.flywaydb.core.api.migration.spring.SpringJdbcMigration;
import org.flywaydb.core.api.resolver.ResolvedMigration;
import org.flywaydb.core.internal.resolver.MigrationInfoHelper;
import org.flywaydb.core.internal.resolver.ResolvedMigrationComparator;
import org.flywaydb.core.internal.resolver.ResolvedMigrationImpl;
import org.flywaydb.core.internal.resolver.spring.SpringJdbcMigrationExecutor;
import org.flywaydb.core.internal.resolver.spring.SpringJdbcMigrationResolver;
import org.flywaydb.core.internal.util.ClassUtils;
import org.flywaydb.core.internal.util.Location;
import org.flywaydb.core.internal.util.Pair;
import org.flywaydb.core.internal.util.StringUtils;
import org.flywaydb.core.internal.util.scanner.Scanner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

/**
 * Migration resolver for {@link SpringJdbcMigration}s which are registered in the given {@link ApplicationContext}.
 * This resolver provides the ability to use other beans registered in the {@link ApplicationContext} and reference
 * them via Spring's dependency injection facility inside the {@link SpringJdbcMigration}s.
 */
public class CustomFlywayMigrationResolver extends SpringJdbcMigrationResolver {

	private static Logger log = LoggerFactory.getLogger(CustomFlywayMigrationResolver.class);
	
	private final ApplicationContext applicationContext;

    public CustomFlywayMigrationResolver(Scanner scanner, Location location, FlywayConfiguration configuration, ApplicationContext applicationContext) {
        super(scanner, location, configuration);
        this.applicationContext = applicationContext;
    }

    @Override
    public Collection<ResolvedMigration> resolveMigrations() {
    	
    	 log.debug("resolvedMigration started");
    	
        // get all beans of type SpringJdbcMigration from the application context
        Map<String, SpringJdbcMigration> springJdbcMigrationBeans =
                (Map<String, SpringJdbcMigration>) this.applicationContext.getBeansOfType(SpringJdbcMigration.class);

        ArrayList<ResolvedMigration> resolvedMigrations = new ArrayList<ResolvedMigration>();

        // resolve the migration and populate it with the migration info
        for (SpringJdbcMigration springJdbcMigrationBean : springJdbcMigrationBeans.values()) {
            ResolvedMigrationImpl resolvedMigration = extractMigrationInfo(springJdbcMigrationBean);
            log.debug("resolvedMigration found. V: {}", resolvedMigration.getVersion());
            resolvedMigration.setPhysicalLocation(ClassUtils.getLocationOnDisk(springJdbcMigrationBean.getClass()));
            resolvedMigration.setExecutor(new SpringJdbcMigrationExecutor(springJdbcMigrationBean));

            resolvedMigrations.add(resolvedMigration);
        }

        Collections.sort(resolvedMigrations, new ResolvedMigrationComparator());
        return resolvedMigrations;
    }

    /**
     * It is a copy of private Flyway's {@link SpringJdbcMigrationResolver.extractMigrationInfo}
     * @param springJdbcMigration
     * @return
     */
    private ResolvedMigrationImpl extractMigrationInfo(SpringJdbcMigration springJdbcMigration) {
        Integer checksum = null;
        if (springJdbcMigration instanceof MigrationChecksumProvider) {
            MigrationChecksumProvider checksumProvider = (MigrationChecksumProvider) springJdbcMigration;
            checksum = checksumProvider.getChecksum();
        }

        MigrationVersion version;
        String description;
        if (springJdbcMigration instanceof MigrationInfoProvider) {
            MigrationInfoProvider infoProvider = (MigrationInfoProvider) springJdbcMigration;
            version = infoProvider.getVersion();
            description = infoProvider.getDescription();
            if (!StringUtils.hasText(description)) {
                throw new FlywayException("Missing description for migration " + version);
            }
        } else {
            String shortName = ClassUtils.getShortName(springJdbcMigration.getClass());
            String prefix;
            if (shortName.startsWith("V") || shortName.startsWith("R")) {
                prefix = shortName.substring(0, 1);
            } else {
                throw new FlywayException("Invalid Jdbc migration class name: " + springJdbcMigration.getClass().getName()
                        + " => ensure it starts with V or R," +
                        " or implement org.flywaydb.core.api.migration.MigrationInfoProvider for non-default naming");
            }
            Pair<MigrationVersion, String> info = MigrationInfoHelper.extractVersionAndDescription(shortName, prefix, "__", "");
            version = info.getLeft();
            description = info.getRight();
        }

        ResolvedMigrationImpl resolvedMigration = new ResolvedMigrationImpl();
        resolvedMigration.setVersion(version);
        resolvedMigration.setDescription(description);
        resolvedMigration.setScript(springJdbcMigration.getClass().getName());
        resolvedMigration.setChecksum(checksum);
        resolvedMigration.setType(MigrationType.SPRING_JDBC);
        return resolvedMigration;
    }	
}
