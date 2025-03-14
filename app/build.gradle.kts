import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    id(libs.plugins.jvm.module.convention.get().pluginId)
    alias(libs.plugins.kotlinx.serialization)
    alias(libs.plugins.shadow.jar)
    application
}

dependencies {
    implementation(projects.data)
    implementation(projects.core)
    implementation(projects.common.smtpMailer)
    implementation(projects.common.cliArguments)
    implementation(projects.common.hashing)

    implementation(projects.infrastructure.grpcApi)
    implementation(projects.infrastructure.rsocketApi)

    implementation(libs.ktor.client.core)

    implementation(libs.kotlinx.serialization.json)
    implementation(libs.kotlinx.coroutines)

    implementation(libs.exposed.core)
    implementation(libs.exposed.jdbc)

    implementation(libs.postgresql.driver)
    implementation(libs.h2.database)

    implementation(libs.y9vad9.rsocket.router)
    implementation(libs.grpc.kotlin.stub)

    implementation(libs.grpc.netty)
    implementation(libs.grpc.services)

    implementation(libs.koin.core)

    implementation(libs.logback.classic)
}

application {
    mainClass.set("io.timemates.backend.application.ApplicationKt")
}


tasks.withType<ShadowJar> {
    archiveBaseName.set("application")
    archiveClassifier.set("")
}
