plugins {
    id(libs.plugins.jvm.module.convention.get().pluginId)
    alias(libs.plugins.kotlinx.serialization)
}

dependencies {
    implementation(projects.core)
    implementation(projects.infrastructure.rsocketApi.serializableTypes)
}