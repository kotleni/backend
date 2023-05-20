plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlinx.serialization)
    application
}

dependencies {
    implementation(projects.data)
    implementation(projects.domain)
    implementation(projects.infrastructure.services)
    implementation(projects.features.smtpMailer)
    implementation(projects.features.cliArguments)

    implementation(libs.kotlinx.serialization)
    implementation(libs.kotlinx.coroutines)

    implementation(libs.exposed.core)
    implementation(libs.exposed.jdbc)

    implementation(libs.grpc.netty)

    implementation(libs.koin.core)
}

application {
    mainClass.set("io.timemates.backend.ApplicationKt")
}
