plugins {
    id("com.android.application") version "8.4.0" apply false
    id("com.android.library") version "8.4.0" apply false
    id("org.jetbrains.kotlin.android") version "1.9.25" apply false
    id("org.jetbrains.kotlin.plugin.serialization") version "1.9.25"
}

tasks.register<Delete>("clean") {
    delete(layout.buildDirectory)

    project.allprojects.forEach { project ->
        delete(project.layout.buildDirectory)
    }
}