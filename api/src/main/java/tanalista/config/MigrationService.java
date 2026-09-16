package tanalista.config;

import org.flywaydb.core.Flyway;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped 
public class MigrationService {

    @Inject
    Flyway flyway;

    public void checkMigration() {
        System.out.println(flyway.info().current().getVersion().toString());
    }
    
}
