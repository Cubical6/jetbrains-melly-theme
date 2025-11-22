#!/bin/bash
# Test only ITermColorSchemeTest by compiling only that test file

echo "Compiling and testing ITermColorScheme only..."
cd buildSrc

# Create a temporary build.gradle.kts that excludes problematic tests
cat > build.gradle.kts.tmp << 'GRADLE'
plugins {
  `kotlin-dsl`
}

repositories {
  mavenCentral()
  mavenLocal()
}

dependencies {
  implementation("org.jsoup:jsoup:1.13.1")
  implementation("com.google.code.gson:gson:2.8.9")
  implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")

  // Testing dependencies for Sprint 1
  testImplementation("org.junit.jupiter:junit-jupiter:5.9.0")
  testImplementation("io.kotest:kotest-assertions-core:5.5.4")
}

tasks.withType<Test> {
  useJUnitPlatform()
}

// Exclude problematic test files during compilation
tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
  exclude("**/integration/**")
  exclude("**/mapping/**")
  exclude("**/tasks/**")
  exclude("**/utils/**")
}
GRADLE

# Backup original build file
cp build.gradle.kts build.gradle.kts.backup

# Use temporary build file
mv build.gradle.kts.tmp build.gradle.kts

# Run the test
../gradlew clean test --tests ITermColorSchemeTest

# Restore original build file
mv build.gradle.kts.backup build.gradle.kts

cd ..
