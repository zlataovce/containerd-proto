import com.google.protobuf.gradle.*
import java.net.URL
import java.io.ByteArrayOutputStream

plugins {
    id("com.google.protobuf") version "0.8.18"
    `maven-publish`
    java
}

group = "me.kcra"
version = "0.0.0" // set when pullProtoSources task runs

object DependencyVersions {
    const val PROTOC = "3.19.1"
    const val GRPC = "1.45.0"
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("io.grpc:grpc-protobuf:${DependencyVersions.GRPC}")
    implementation("io.grpc:grpc-stub:${DependencyVersions.GRPC}")
}

sourceSets.forEach {
    it.java.srcDirs(
        "${project.buildDir}/generatedProto/${it.name}/java",
        "${project.buildDir}/generatedProto/${it.name}/grpc"
    )
}

protobuf {
    protoc {
        artifact = "com.google.protobuf:protoc:${DependencyVersions.PROTOC}"
    }

    plugins {
        id("grpc") {
            artifact = "io.grpc:protoc-gen-grpc-java:${DependencyVersions.GRPC}"
        }
    }

    generateProtoTasks {
        all().forEach {
            it.dependsOn("pullProtoSources")

            it.plugins {
                id("grpc")
            }
        }
    }

    generatedFilesBaseDir = "${project.buildDir}/generatedProto"
}

tasks.register("pullProtoSources") {
    onlyIf { !file("src/main/proto").isDirectory }

    doFirst {
        delete("src/main/proto")
        file("src/main/proto").mkdirs()
    }
    doLast {
        exec {
            workingDir = file("src/main/proto")

            commandLine = listOf("git", "clone", "--branch", "main", "--no-checkout", "https://github.com/containerd/containerd.git", ".")
        }
        val output = ByteArrayOutputStream()
        exec {
            workingDir = file("src/main/proto")

            commandLine = listOf("git", "describe", "--tags", "--abbrev=0")
            standardOutput = output
        }
        version = output.toString().trim().substring(1).replace("api/", "")
        exec {
            workingDir = file("src/main/proto")

            commandLine = listOf("git", "checkout", version as String, "*.proto")
        }
    }
    doLast {
        downloadFile("https://raw.githubusercontent.com/gogo/protobuf/master/gogoproto/gogo.proto", "src/main/proto/gogoproto/gogo.proto")
    }
    doLast {
        delete("src/main/proto/.git")
    }
    doLast {
        file("src/main/proto").walkTopDown().forEach { file ->
            if (file.isFile) {
                file.writeText(
                    // remove GitHub url imports
                    file.readText()
                        .replace("github.com/containerd/containerd/", "")
                        .replace("github.com/gogo/protobuf/", "")
                )
            }
        }
    }
    doLast {
        // remove containerd vendor stuff
        delete("src/main/proto/vendor")
    }
}

fun Project.downloadFile(url: String, path: String) {
    val loc: File = file(path)
    loc.parentFile.mkdirs()
    if (!loc.exists()) {
        URL(url).openStream().use { it.copyTo(loc.outputStream()) }
    }
}

configure<PublishingExtension> {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
        }
    }

    repositories {
        maven {
            url = if ((project.version as String).endsWith("-SNAPSHOT")) uri("https://repo.kcra.me/snapshots")
                else uri("https://repo.kcra.me/releases")
            credentials {
                username = System.getenv("REPO_USERNAME")
                password = System.getenv("REPO_PASSWORD")
            }
        }
    }
}