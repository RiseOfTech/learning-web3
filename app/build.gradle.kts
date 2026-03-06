plugins {
    id("application")
    id("org.springframework.boot")
}

val mainClassName = "org.example.App"
application {
    mainClass.set(mainClassName)
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-webflux")
    implementation("org.springframework.boot:spring-boot-starter-validation")

    implementation("org.web3j:core")

    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation("org.mockito:mockito-core")
    testImplementation("org.mockito:mockito-junit-jupiter")
}

tasks.bootJar {
    archiveFileName.set("learning-web3-app.jar")
}