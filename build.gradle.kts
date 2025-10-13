import org.liquibase.gradle.LiquibaseTask

plugins {
    java
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
    // =============================================
    // Spring Boot Starters (основные фреймворки)
    // =============================================
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")        // JPA + Hibernate
    implementation("org.springframework.boot:spring-boot-starter-graphql")         // GraphQL API
    implementation("org.springframework.boot:spring-boot-starter-security")        // Spring Security
    implementation("org.springframework.boot:spring-boot-starter-webflux")         // Reactive Web
    implementation("org.springframework.boot:spring-boot-starter-jooq")            // jOOQ SQL Builder
    implementation("org.springframework.ai:spring-ai-starter-model-ollama")        // AI интеграция с Ollama

    // =============================================
    // Database & Migration (БД и миграции)
    // =============================================
    implementation("org.liquibase:liquibase-core")                                 // Миграции БД
    implementation("org.postgresql:postgresql:$postgresVersion")                         // PostgreSQL драйвер

    // =============================================
    // jOOQ Explicit Dependencies (ФИКС для ошибки key_seq)
    // =============================================
    implementation("org.jooq:jooq:3.19.17")                                        // Явно указываем версию jOOQ
    jooqGenerator("org.jooq:jooq-meta:3.19.17")                                    // Метаданные для генерации
    jooqGenerator("org.jooq:jooq-codegen:3.19.17")                                 // Генератор кода jOOQ
    jooqGenerator("org.postgresql:postgresql:$postgresVersion")                             // PostgreSQL для генерации jOOQ

    // =============================================
    // Liquibase Runtime (для Gradle тасок liquibase)
    // =============================================
    liquibaseRuntime("org.liquibase:liquibase-core:4.28.0")                        // Ядро Liquibase
    liquibaseRuntime("org.postgresql:postgresql:$postgresVersion")                         // PostgreSQL для Liquibase
    liquibaseRuntime("info.picocli:picocli:4.7.6")                                 // CLI парсер для Liquibase
    liquibaseRuntime(sourceSets.named("main").get().output)


    // =============================================
    // Development & Annotation Processing
    // =============================================
    compileOnly("org.projectlombok:lombok")                                        // Генерация кода Lombok
    developmentOnly("org.springframework.boot:spring-boot-devtools")               // Hot reload для разработки
    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor") // Обработка @ConfigurationProperties
    annotationProcessor("org.projectlombok:lombok")                                // Обработка аннотаций Lombok

    // =============================================
    // Test Dependencies (тестирование)
    // =============================================
    testImplementation("org.springframework.boot:spring-boot-starter-test")        // Spring Boot тесты
    testImplementation("io.projectreactor:reactor-test")                           // Reactive streams тестирование
    testImplementation("org.springframework.graphql:spring-graphql-test")          // GraphQL тестирование
    testImplementation("org.springframework.security:spring-security-test")        // Security тестирование
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")                  // Запуск JUnit тестов
}

dependencyManagement {
    imports {
        mavenBom("org.springframework.ai:spring-ai-bom:${property("springAiVersion")}")
    }
}

// ================================
// 1. DB Creds по профилям (Spring Profiles)
// ================================


val activeProfile: String = System.getenv("SPRING_PROFILES_ACTIVE") ?: "local"


fun dbUrl(profile: String) = when(profile) {
    "local" -> "jdbc:postgresql://localhost:5433/user_service_local"
    "prod" -> "jdbc:postgresql://localhost:5432/user_service"
    else -> throw IllegalArgumentException("Unknown profile: $profile")
}
fun dbUser(profile: String) = if(profile=="local") "local_user" else "postgres"
fun dbPassword(profile: String) = if(profile=="local") "local_password" else "password"


// ================================
// 2. Liquibase конфигурация
// ================================
liquibase {
    activities.create("main") {
        this.arguments = mapOf(
            "changelogFile" to "src/main/resources/db/changelog/db.changelog-master.yaml",
            "url" to dbUrl(activeProfile),  // ← РЕАЛЬНАЯ БД!
            "username" to dbUser(activeProfile),
            "password" to dbPassword(activeProfile),
            "driver" to "org.postgresql.Driver"
        )
    }
    runList = "main"
}

// ================================
// 3. jOOQ генерация
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
                        // ИСКЛЮЧАЕМ системные таблицы Liquibase (опционально)
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

tasks.withType<Test> { useJUnitPlatform() }

// ================================
// 4. Docker (только локально)
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

    tasks.register<Exec>("dockerReset") {
        group = "docker"
        description = "Reset PostgreSQL (removes volumes)"
        commandLine("docker-compose", "down", "-v")
    }
}

// ================================
// 6. Полная сборка и setup
// ================================
tasks.register("setup") {
    group = "application"
    description = "Complete project setup: Docker + SQL + jOOQ + Migrations"
    dependsOn(if (activeProfile == "local") listOf("dockerUp", "update")
    else listOf("updateSql", "update"))
    doLast { println("✅ Setup completed! Profile: $activeProfile") }
}

tasks.register("migrate") {
    group = "application"
    description = "Update migrations"
    dependsOn("updateSql", "update")
}

tasks.register("dev") {
    group = "application"
    description = "Run app smartly (Docker + Jooq + Liquibase)"
    dependsOn("setup", "bootRun")
}

// ================================
// 7. Git hooks
// ================================
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
