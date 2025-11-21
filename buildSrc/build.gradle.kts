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
