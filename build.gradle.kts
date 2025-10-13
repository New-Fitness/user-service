import org.jooq.codegen.GenerationTool

plugins {
    java
    id("org.springframework.boot") version "3.5.6"
    id("io.spring.dependency-management") version "1.1.7"
    id("nu.studer.jooq") version "8.2"
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

dependencies {
    // Spring Boot
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-graphql")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-webflux")
    implementation("org.springframework.boot:spring-boot-starter-jooq")

    // AI
    implementation("org.springframework.ai:spring-ai-starter-model-ollama")

    // Database
    implementation("org.postgresql:postgresql:$postgresVersion")

    // jOOQ
    implementation("org.jooq:jooq:3.19.17")
    jooqGenerator("org.jooq:jooq-meta:3.19.17")
    jooqGenerator("org.jooq:jooq-codegen:3.19.17")
    jooqGenerator("org.postgresql:postgresql:$postgresVersion")

    // Development
    compileOnly("org.projectlombok:lombok")
    developmentOnly("org.springframework.boot:spring-boot-devtools")
    annotationProcessor("org.projectlombok:lombok")
    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")

    // Tests
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("io.projectreactor:reactor-test")
    testImplementation("org.springframework.graphql:spring-graphql-test")
    testImplementation("org.springframework.security:spring-security-test")
}

dependencyManagement {
    imports {
        mavenBom("org.springframework.ai:spring-ai-bom:1.0.3")
    }
}

// ================================
// DB credentials
// ================================
val activeProfile: String = findProperty("spring.profiles.active") as? String ?: "local"

fun dbUrl(profile: String) = when(profile) {
    "local" -> "jdbc:postgresql://localhost:5433/user_service_local"
    "prod" -> "jdbc:postgresql://prod-db-host:5432/user_service"
    else -> throw IllegalArgumentException("Unknown profile: $profile")
}
fun dbUser(profile: String) = if(profile=="local") "local_user" else "prod_user"
fun dbPassword(profile: String) = if(profile=="local") "local_password" else "prod_password"

// ================================
// jOOQ генерация
// ================================
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

// ================================
// Docker локально
// ================================
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
}

// ================================
// dev таска: Docker + Spring Boot + jOOQ
// ================================
tasks.register("dev") {
    group = "application"
    description = "Start local DB, run Spring Boot (Liquibase auto), generate jOOQ"
    if(activeProfile=="local") dependsOn("dockerUp")
    dependsOn("bootRun")
    finalizedBy("generateJooq")
}

tasks.withType<Test> { useJUnitPlatform() }
