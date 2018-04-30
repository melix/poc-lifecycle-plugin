plugins {
    `kotlin-dsl`
    `java-gradle-plugin`
}

gradlePlugin {
    (plugins) {
        "lifecycle" {
            id = "org.gradle.plugins.lifecycle"
            implementationClass = "org.gradle.plugins.lifecycle.LifecyclePlugin"
        }
    }
}

