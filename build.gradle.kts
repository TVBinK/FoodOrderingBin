buildscript{
    repositories {
        mavenCentral()
        google()
    }
}
plugins {
    id("com.android.application") version "8.7.2" apply false
    id("org.jetbrains.kotlin.android") version "1.8.0" apply false
    id("com.google.gms.google-services") version "4.4.1" apply false
}