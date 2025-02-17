import org.jetbrains.kotlin.gradle.dsl.JvmTarget

val kotlinVersion: String by project
val ktorVersion: String by project
val koinVersion: String by project
val mockkVersion: String by project
val logbackVersion: String by project

plugins {
    application
    kotlin("jvm") version "2.0.20"
    id("com.ncorti.ktfmt.gradle") version "0.20.1"
    id("io.ktor.plugin") version "2.3.12"
    id("org.jetbrains.kotlin.plugin.serialization") version "2.0.20"
    id("com.adarshr.test-logger") version "4.0.0"
    jacoco
}

group = "software.shonk"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven { url = uri("https://maven.pkg.jetbrains.space/public/p/ktor/eap") }
}

// todo check if syntax can be adjusted to better match the other snippets
configurations {
    val integrationTestImplementation by creating {
        extendsFrom(configurations.implementation.get(), configurations.testImplementation.get())
    }
    val integrationTestRuntimeOnly by creating {
        extendsFrom(configurations.runtimeOnly.get(), configurations.testRuntimeOnly.get())
    }
}

sourceSets.create("integrationTest") {
    kotlin.srcDir("src/integrationTest/kotlin")
    resources.srcDir("src/integrationTest/resources")
    compileClasspath += sourceSets["main"].output + sourceSets["test"].output
    runtimeClasspath += output + compileClasspath
}

dependencies {
    // Project-specific modules
    implementation(project(":shork"))

    // Ktor dependencies
    implementation("io.ktor:ktor-server-cors-jvm:$ktorVersion")
    implementation("io.ktor:ktor-server-content-negotiation:$ktorVersion")
    implementation("io.ktor:ktor-client-content-negotiation:$ktorVersion")
    implementation("io.ktor:ktor-serialization-kotlinx-json:$ktorVersion")
    implementation("io.ktor:ktor-server-core:$ktorVersion")
    implementation("io.ktor:ktor-server-netty:$ktorVersion")
    testImplementation("io.ktor:ktor-server-test-host-jvm:$ktorVersion")

    // Koin dependencies
    implementation("io.insert-koin:koin-ktor:$koinVersion")
    implementation("io.insert-koin:koin-core:$koinVersion")
    testImplementation("io.insert-koin:koin-test-junit5:$koinVersion")

    // Kotlin dependencies
    implementation("org.jetbrains.kotlin:kotlin-stdlib:$kotlinVersion")

    // Logging
    implementation("ch.qos.logback:logback-classic:$logbackVersion")

    // Testing
    testImplementation(kotlin("test"))
    testImplementation("io.mockk:mockk:$mockkVersion")
}

application {
    mainClass.set("software.shonk.ApplicationKt")
}

ktfmt {
    kotlinLangStyle()
}

tasks {
    // Kotlin compiler options
    withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_21)
        }
    }

    // Java compiler options
    withType<JavaCompile> {
        sourceCompatibility = "21"
        targetCompatibility = "21"
    }

    // Test configuration
    test {
        useJUnitPlatform()
        finalizedBy(jacocoTestReport) // Generate JaCoCo report after tests
        testlogger {
            showPassed = false
        }
    }

    // todo check if syntax can be adjusted to better match the other snippets
    val integrationTest by creating(Test::class) {
        testClassesDirs = sourceSets["integrationTest"].output.classesDirs
        classpath = sourceSets["integrationTest"].runtimeClasspath
        useJUnitPlatform()
        description = "Runs the integration tests."
        group = "verification"
        shouldRunAfter(test) // Ensure unit tests run first
    }

    check {
        dependsOn(integrationTest) // Include integration tests in the "check" lifecycle
    }

    // JaCoCo test report configuration
    jacocoTestReport {
        dependsOn(test) // Ensure tests run before generating report
    }
}
