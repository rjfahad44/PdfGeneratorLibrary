plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    id("maven-publish")
}

android {
    namespace = "com.bitbyte.pdfgenerator"
    compileSdk = 35

    defaultConfig {
        minSdk = 21
        version = "1.0.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {

    implementation("androidx.core:core-ktx:1.15.0")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.12.0")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
}


afterEvaluate {
    publishing {
        publications {
            create<MavenPublication>("release") {
                // Maven Coordinates
                groupId = "https://github.com/rjfahad44/rjfahad44"
                artifactId = "PdfGeneratorLibrary"
                version = "1.0.0-alpha"

                // Specify the artifact to be published
                from(components["release"])

                // Metadata for the POM file
                pom {
                    name.set("PdfGeneratorLibrary") // Library Name
                    description.set("An SDK for integrating Shadhin Music services with GP applications.") // Library Description
                    url.set("https://github.com/rjfahad44/PdfGeneratorLibrary.git") // Repository URL

//                    licenses {
//                        license {
//                            name.set("The Apache License, Version 2.0")
//                            url.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
//                            distribution.set("repo")
//                        }
//                    }

                    developers {
                        developer {
                            id.set("rjfahad44")
                            name.set("Fahad Alam")
                            email.set("rjfahad44@gmail.com")
                        }
                    }

                    scm {
                        connection.set("scm:git:git://github.com/rjfahad44/PdfGeneratorLibrary.git")
                        developerConnection.set("scm:git:ssh://github.com/rjfahad44/PdfGeneratorLibrary.git")
                        url.set("https://github.com/rjfahad44/PdfGeneratorLibrary.git")
                    }
                }
            }
        }

        repositories {
            maven {
                // Specify where to publish the library (JitPack will build automatically)
                name = "GitHubPackages"
                url = uri("https://github.com/rjfahad44/PdfGeneratorLibrary.git")
//                credentials {
//                    username = project.findProperty("gpr.user") as String? ?: System.getenv("GITHUB_USERNAME")
//                    password = project.findProperty("gpr.key") as String? ?: System.getenv("GITHUB_TOKEN")
//                }
            }
        }
    }
}
