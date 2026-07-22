package io.quarkusdroneshop.homeoffice;

import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices;

/**
 * ArchUnit によるアーキテクチャ適合性テスト。
 * パッケージ構造:
 *   io.quarkusdroneshop.homeoffice.domain.*         - ドメイン層 (エンティティ / ビュー)
 *   io.quarkusdroneshop.homeoffice.infrastructure.* - インフラ層 (Kafka / REST / シリアライズ)
 *   io.quarkusdroneshop.homeoffice.viewmodels.*     - ビューモデル層
 *   io.quarkusdroneshop.homeoffice.bootstrap.*      - 初期化層
 */
@AnalyzeClasses(
        packages = "io.quarkusdroneshop.homeoffice",
        importOptions = ImportOption.DoNotIncludeTests.class)
public class ArchitectureTest {

    // =========================================================================
    // 1. 命名規則
    // =========================================================================

    @ArchTest
    static final ArchRule Deserializer命名規則 =
        classes()
            .that().implement("org.apache.kafka.common.serialization.Deserializer")
            .or().areAssignableTo(
                io.quarkus.kafka.client.serialization.ObjectMapperDeserializer.class)
            .should().haveSimpleNameEndingWith("Deserializer");

    @ArchTest
    static final ArchRule 例外クラスの命名規則 =
        classes()
            .that().areAssignableTo(Exception.class)
            .and().resideInAPackage("io.quarkusdroneshop.homeoffice..")
            .should().haveSimpleNameEndingWith("Exception");

    @ArchTest
    static final ArchRule RESTリソースの命名規則 =
        classes()
            .that().areAnnotatedWith(jakarta.ws.rs.Path.class)
            .and().resideInAPackage("io.quarkusdroneshop.homeoffice..")
            .should().haveSimpleNameEndingWith("Resource");

    @ArchTest
    static final ArchRule RESTリソースはPublic =
        classes()
            .that().haveSimpleNameEndingWith("Resource")
            .and().resideInAPackage("..infrastructure..")
            .should().bePublic();

    @ArchTest
    static final ArchRule RESTリソースは_Pathアノテーションを持つ =
        classes()
            .that().haveSimpleNameEndingWith("Resource")
            .and().resideInAPackage("..infrastructure..")
            .should().beAnnotatedWith(jakarta.ws.rs.Path.class);

    // =========================================================================
    // 2. パッケージ配置ルール
    // =========================================================================

    @ArchTest
    static final ArchRule Deserializerはinfrastructureに配置 =
        classes()
            .that().haveSimpleNameEndingWith("Deserializer")
            .should().resideInAPackage("..infrastructure..");

    // =========================================================================
    // 3. レイヤー間依存ルール
    // =========================================================================

    @ArchTest
    static final ArchRule ドメイン層はJAX_RSを使用しない =
        noClasses()
            .that().resideInAPackage("io.quarkusdroneshop.homeoffice.domain..")
            .should().dependOnClassesThat()
            .resideInAPackage("jakarta.ws.rs..");

    @ArchTest
    static final ArchRule ドメイン層はInfrastructureに依存しない =
        noClasses()
            .that().resideInAPackage("io.quarkusdroneshop.homeoffice.domain..")
            .should().dependOnClassesThat()
            .resideInAPackage("io.quarkusdroneshop.homeoffice.infrastructure..");

    @ArchTest
    static final ArchRule ドメインクラスはPublic =
        classes()
            .that().resideInAPackage("io.quarkusdroneshop.homeoffice.domain")
            .and().areNotInterfaces()
            .should().bePublic();

    @ArchTest
    static final ArchRule Infrastructureの依存範囲チェック =
        classes()
            .that().resideInAPackage("io.quarkusdroneshop.homeoffice.infrastructure..")
            .should().onlyDependOnClassesThat()
            .resideInAnyPackage(
                "io.quarkusdroneshop.homeoffice.infrastructure..",
                "io.quarkusdroneshop.homeoffice.domain..",
                "io.quarkusdroneshop.homeoffice.viewmodels..",
                "java..",
                "javax..",
                "jakarta..",
                "io.quarkus..",
                "io.smallrye..",
                "org.eclipse.microprofile..",
                "org.apache.kafka..",
                "com.fasterxml..",
                "org.slf4j..",
                "org.jboss..",
                // dataproduct-order-events (Avro) を読む OrderPlacedLineItemDeserializer 用。
                "io.apicurio..",
                "org.apache.avro..");

    // =========================================================================
    // 4. 循環依存
    // =========================================================================

    @ArchTest
    static final ArchRule パッケージ間循環依存なし =
        slices()
            .matching("io.quarkusdroneshop.homeoffice.(*)..")
            .should().beFreeOfCycles();
}
