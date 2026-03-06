plugins {
    java
    id("org.springframework.boot") apply false
    id("io.spring.dependency-management")
}

allprojects {
    group = "com.example"
    version = "0.0.1-SNAPSHOT"

    repositories {
        mavenCentral()
    }
}

subprojects {
    apply(plugin = "org.springframework.boot")
    apply(plugin = "io.spring.dependency-management")
    apply(plugin = "java-library")

    tasks.withType<JavaCompile> {
        options.encoding = "UTF-8"
    }

    tasks {
        val javaVersion: String by project
        compileJava {
            targetCompatibility = javaVersion
            sourceCompatibility = javaVersion
        }

        withType<JavaCompile> {
            options.encoding = "UTF-8"
        }
    }
    dependencyManagement {
        val springBootVersion: String by project
        val web3jVersion: String by project
        val lombokVersion: String by project
        val junitVersion: String by project
        val mockitoVersion: String by project

        imports {
            mavenBom("org.springframework.boot:spring-boot-dependencies:$springBootVersion")
        }

        dependencies {
            dependency("org.web3j:core:$web3jVersion")
            dependency("org.projectlombok:lombok:$lombokVersion")
            dependency("org.junit.jupiter:junit-jupiter:$junitVersion")
            dependency("org.mockito:mockito-core:$mockitoVersion")
            dependency("org.mockito:mockito-junit-jupiter:$mockitoVersion")
        }
    }

    tasks.withType<JavaCompile> {
        options.encoding = "UTF-8"
    }

    tasks.withType<Test> {
        useJUnitPlatform()
    }
}