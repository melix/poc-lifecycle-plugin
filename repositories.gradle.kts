repositories {
    maven {
        setUrl(file("repo"))

        // this is just because this test repo doesn't provide maven-metadata.xml
        // so the following block is not necessary with real repositories
        metadataSources {
            gradleMetadata()
            artifact()
        }
    }
}