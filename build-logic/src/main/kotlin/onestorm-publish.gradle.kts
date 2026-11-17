// Publishes to the OneStorm Maven repository, next to the upstream PaperMC repository.
//
// Credentials are read from the Gradle properties onestormUsername and onestormPassword,
// e.g. via the environment variables ORG_GRADLE_PROJECT_onestormUsername and
// ORG_GRADLE_PROJECT_onestormPassword.
//
// Publish with: ./gradlew :velocity-api:publishMavenPublicationToOnestormRepository
plugins {
    id("velocity-publish")
}

extensions.configure<PublishingExtension> {
    repositories {
        maven {
            name = "onestorm"
            url = uri("https://repo.onestorm.net/maven-public/")
            credentials(PasswordCredentials::class.java)
        }
    }
}
