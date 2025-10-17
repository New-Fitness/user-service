import org.liquibase.gradle.LiquibaseTask

plugins {
    java
    checkstyle
    id("org.springframework.boot") version "3.5.6"
    id("io.spring.dependency-management") version "1.1.7"
    id("nu.studer.jooq") version "8.2"
    id("org.liquibase.gradle") version "2.2.0"
}

group = "com.new-fitness"
version = "0.0.1-SNAPSHOT"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

repositories {
    mavenCentral()
}

val postgresVersion = "42.6.0"
extra["springAiVersion"] = "1.0.3"

dependencies {
    // Spring Boot
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-graphql")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-webflux")
    implementation("org.springframework.boot:spring-boot-starter-jooq")
    implementation("org.springframework.ai:spring-ai-starter-model-ollama")

    // DB & Liquibase
    implementation("org.liquibase:liquibase-core")
    implementation("org.postgresql:postgresql:$postgresVersion")

    // jOOQ
    implementation("org.jooq:jooq:3.19.17")
    jooqGenerator("org.jooq:jooq-meta:3.19.17")
    jooqGenerator("org.jooq:jooq-codegen:3.19.17")
    jooqGenerator("org.postgresql:postgresql:$postgresVersion")

    // Liquibase runtime
    liquibaseRuntime("org.liquibase:liquibase-core:4.28.0")
    liquibaseRuntime("org.postgresql:postgresql:$postgresVersion")
    liquibaseRuntime("info.picocli:picocli:4.7.6")
    liquibaseRuntime(sourceSets.named("main").get().output)

    // Lombok / devtools
    compileOnly("org.projectlombok:lombok")
    developmentOnly("org.springframework.boot:spring-boot-devtools")
    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
    annotationProcessor("org.projectlombok:lombok")

    // Test
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("io.projectreactor:reactor-test")
    testImplementation("org.springframework.graphql:spring-graphql-test")
    testImplementation("org.springframework.security:spring-security-test")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

dependencyManagement {
    imports {
        mavenBom("org.springframework.ai:spring-ai-bom:${property("springAiVersion")}")
    }
}

checkstyle {
    toolVersion = "10.20.0"
    configFile = file(findProperty("checkstyleConfig") ?: "config/checkstyle/google_checks.xml")
}

// ----------------------------
// Profiles
// ----------------------------
val activeProfile: String = System.getenv("SPRING_PROFILES_ACTIVE") ?: "local"

fun dbUrl(profile: String) = when(profile) {
    "local" -> "jdbc:postgresql://localhost:5433/user_service_local"
    "prod" -> System.getenv("SPRING_DATASOURCE_URL")
    else -> throw IllegalArgumentException("DB URL not configured for profile: $profile")
}
fun dbUser(profile: String) = when(profile){
    "local" -> "local_user"
    "prod" -> System.getenv("SPRING_DATASOURCE_USERNAME")
    else -> throw IllegalArgumentException("DB user not configured for profile: $profile")
}
fun dbPassword(profile: String) = when(profile){
    "local" -> "local_password"
    "prod" -> System.getenv("SPRING_DATASOURCE_PASSWORD")
    else -> throw IllegalArgumentException("DB password not configured for profile: $profile")
}

// ----------------------------
// Liquibase config
// ----------------------------
liquibase {
    activities.create("main") {
        arguments = mapOf(
            "changelogFile" to "src/main/resources/db/changelog/db.changelog-master.yaml",
            "url" to dbUrl(activeProfile),
            "username" to dbUser(activeProfile),
            "password" to dbPassword(activeProfile),
            "driver" to "org.postgresql.Driver"
        )
    }
    runList = "main"
}

// ----------------------------
// jOOQ config
// ----------------------------
jooq {
    configurations {
        create("main") {
            jooqConfiguration.apply {
                jdbc.apply {
                    driver = "org.postgresql.Driver"
                    url = dbUrl(activeProfile)
                    user = dbUser(activeProfile)
                    password = dbPassword(activeProfile)
                }
                generator.apply {
                    name = "org.jooq.codegen.JavaGenerator"
                    database.apply {
                        name = "org.jooq.meta.postgres.PostgresDatabase"
                        inputSchema = "public"
                        excludes = "databasechangelog|databasechangeloglock"
                    }
                    generate.apply {
                        isDaos = true
                        isPojos = true
                        isFluentSetters = true
                    }
                    target.apply {
                        packageName = "com.newfitness.jooq"
                        directory = "build/generated-src/jooq/main"
                    }
                }
            }
        }
    }
}

tasks.named("generateJooq") {
    dependsOn("update")
}

// ----------------------------
// Test config
// ----------------------------
tasks.withType<Test> { useJUnitPlatform() }

// ----------------------------
// Local Docker tasks
// ----------------------------
if (activeProfile == "local") {
    tasks.register<Exec>("dockerUp") {
        group = "docker"
        description = "Start PostgreSQL via docker-compose"
        commandLine("docker-compose", "up", "-d")
    }

    tasks.register<Exec>("dockerDown") {
        group = "docker"
        description = "Stop PostgreSQL container"
        commandLine("docker-compose", "down")
    }

    tasks.register<Exec>("dockerReset") {
        group = "docker"
        description = "Reset PostgreSQL (removes volumes)"
        commandLine("docker-compose", "down", "-v")
    }
}

// ----------------------------
// Setup / Dev / Migrate
// ----------------------------
tasks.register("setup") {
    group = "application"
    description = "Complete setup: Docker + Liquibase + jOOQ"
    dependsOn(if (activeProfile == "local") listOf("dockerUp", "update") else listOf("updateSql", "update"))
    doLast { println("✅ Setup completed! Profile: $activeProfile") }
}

tasks.register("migrate") {
    group = "application"
    description = "Run migrations"
    dependsOn("updateSql", "update")
}

tasks.register("dev") {
    group = "application"
    description = "Run app with migrations + jOOQ"
    dependsOn("setup", "bootRun")
}

// ----------------------------
// Git hooks
// ----------------------------
tasks.register<Copy>("installGitHooks") {
    group = "git"
    description = "Install Git hooks"
    from("gradle/git-hooks")
    into(".git/hooks")
    doLast {
        file(".git/hooks").listFiles()?.forEach { it.setExecutable(true) }
        println("✅ Git hooks installed")
    }
}
