# Publishing Guide

## Publish to Local Maven Repository

For local development and testing:

```bash
./gradlew publishToMavenLocal
```

This installs to `~/.m2/repository/uk/co/ryanharrison/math-engine/1.0.0/`

### Using in Another Project

Add to your project's `build.gradle.kts`:

```kotlin
repositories {
    mavenLocal()  // For local testing
}

dependencies {
    implementation("uk.co.ryanharrison:math-engine:1.0.0")
}
```

## Publishing to Maven Central

### Prerequisites

1. **Sonatype OSSRH Account**
    - Create account at https://issues.sonatype.org/
    - Request namespace for `uk.co.ryanharrison`

2. **GPG Key**
   ```bash
   # Generate key
   gpg --gen-key

   # List keys
   gpg --list-keys

   # Publish to key server
   gpg --keyserver keys.openpgp.org --send-keys YOUR_KEY_ID
   ```

3. **Credentials**
   Add to `~/.gradle/gradle.properties`:
   ```properties
   ossrhUsername=your-jira-username
   ossrhPassword=your-jira-password
   ```

   Or use environment variables:
   ```bash
   export OSSRH_USERNAME=your-jira-username
   export OSSRH_PASSWORD=your-jira-password
   ```

### Enable Maven Central Publishing

1. **Uncomment in `build.gradle.kts`:**
   ```kotlin
   // Uncomment the OSSRH maven repository
   // Uncomment the signing configuration
   ```

2. **Update POM metadata:**
    - Replace GitHub URLs with actual repository
    - Update developer email
    - Verify license

3. **Publish:**
   ```bash
   ./gradlew publish
   ```

## Version Management

### Current Version

Set in `build.gradle.kts`:

```kotlin
version = "1.0.0"
```

### Versioning Strategy

- **Releases:** `1.0.0`, `1.1.0`, `2.0.0`
- **Snapshots:** `1.0.0-SNAPSHOT` (for development)
- **Semantic Versioning:**
    - MAJOR: Breaking API changes
    - MINOR: New features, backwards compatible
    - PATCH: Bug fixes

### Snapshot Workflow

```kotlin
version = "1.1.0-SNAPSHOT"
```

```bash
./gradlew publish  # Publishes to snapshot repository
```

### Inspect Published Files

```bash
# Local Maven
ls ~/.m2/repository/uk/co/ryanharrison/math-engine/1.0.0/

# Build directory
ls build/repo/uk/co/ryanharrison/math-engine/1.0.0/
```

### Verify POM

```bash
cat ~/.m2/repository/uk/co/ryanharrison/math-engine/1.0.0/math-engine-1.0.0.pom
```
