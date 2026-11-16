package tanalista.arquitetura;

import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static org.junit.jupiter.api.Assertions.*;

class ArquiteturaTest {
    private static final ArchRule INDEPENDENCIA =
            noClasses().that().resideInAnyPackage("tanalista.dominio..", "tanalista.aplicacao..")
                    .should().dependOnClassesThat().resideInAnyPackage(
                            "jakarta..", "javax.persistence..", "javax.sql..", "java.sql..", "java.net..",
                            "io.quarkus..", "org.hibernate..", "io.grpc..", "com.google.protobuf..",
                            "tanalista.adaptadores..", "tanalista.config..", "tanalista.contrato..");

    @Test
    void productionDomainAndUseCasesDoNotDependOnInfrastructure() {
        var classes = new ClassFileImporter()
                .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
                .importPackages("tanalista.dominio", "tanalista.aplicacao");
        assertFalse(classes.isEmpty(), "Architecture check must not be vacuous");
        INDEPENDENCIA.check(classes);
    }

}
