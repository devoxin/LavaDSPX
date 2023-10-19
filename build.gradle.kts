plugins {
    id("java-library")
}

group = "me.devoxin"
version = "1.0.0"

repositories {
    mavenCentral()
    maven {
        setUrl("https://jitpack.io")
    }
}

dependencies {
    api("com.github.devoxin.lavaplayer:lavaplayer:1.4.4")
}
