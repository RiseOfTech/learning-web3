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
    plugins {
        val springBootVersion: String by settings
        val springDependencyManagementVersion: String by settings
        id("org.springframework.boot") version springBootVersion
        id("io.spring.dependency-management") version springDependencyManagementVersion
    }
    repositories {
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositories {
        mavenCentral()
    }
}
