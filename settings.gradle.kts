rootProject.name = "learning-web3"

val modules = listOf("app")

fun initModulesFullName(moduleName: String) {
    findProject(":$moduleName")?.name = "${rootProject.name}-$moduleName"
}

modules.forEach { module ->
    include(module)
    initModulesFullName(module)
}

pluginManagement {
    val springBootVersion: String by settings
    val springDependencyManagementVersion: String by settings

    plugins {
        id("org.springframework.boot") version springBootVersion
        id("io.spring.dependency-management") version springDependencyManagementVersion
    }
    repositories {
        mavenCentral()
        gradlePluginPortal()
    }
}
