# containerd-proto
![Maven releases](https://repo.kcra.me/api/badge/latest/releases/me/kcra/containerd-proto)  

Compiled protobuf definitions for containerd gRPC API for Java.

## Usage
### Gradle
```kotlin
repositories {
    maven("https://repo.kcra.me/releases")
}

dependencies {
    implementation("me.kcra:containerd-proto:YOUR_CONTAINERD_VERSION")
}
```

### Maven
```xml
<repositories>
    <repository>
        <id>kcra-repo</id>
        <url>https://repo.kcra.me/releases</url>
    </repository>
</repositories>

<dependencies>
    <dependency>
        <groupId>me.kcra</groupId>
        <artifactId>containerd-proto</artifactId>
        <version>YOUR_CONTAINERD_VERSION</version>
        <scope>compile</scope>
    </dependency>
</dependencies>
```