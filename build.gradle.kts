plugins {
    kotlin("jvm") version "1.9.25"
    kotlin("plugin.spring") version "1.9.25"
    id("org.springframework.boot") version "3.2.0"
    id("io.spring.dependency-management") version "1.1.7"
}

group = "com.example"
version = "0.0.1-SNAPSHOT"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    runtimeOnly("com.h2database:h2")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation(kotlin("test"))
}

kotlin {
    compilerOptions {
        freeCompilerArgs.addAll("-Xjsr305=strict")
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}
tasks.test {
    useJUnitPlatform()

    testLogging {
        events("passed", "skipped", "failed")
        showStandardStreams = false
    }

    finalizedBy("testSummary")
}

tasks.register("testSummary") {
    mustRunAfter("test")

    doLast {
        val testResultsDir = file("$buildDir/test-results/test")
        if (testResultsDir.exists()) {
            var total = 0
            var failed = 0
            var skipped = 0

            testResultsDir.listFiles()?.filter { it.name.endsWith(".xml") }?.forEach { file ->
                val text = file.readText()

                Regex("""tests="(\d+)"""").find(text)?.groupValues?.get(1)?.toIntOrNull()?.let { total += it }
                Regex("""failures="(\d+)"""").find(text)?.groupValues?.get(1)?.toIntOrNull()?.let { failed += it }
                Regex("""errors="(\d+)"""").find(text)?.groupValues?.get(1)?.toIntOrNull()?.let { failed += it }
                Regex("""skipped="(\d+)"""").find(text)?.groupValues?.get(1)?.toIntOrNull()?.let { skipped += it }
            }

            val passed = total - failed - skipped

            println("\n🧪 TEST RESULTS:")
            println("━".repeat(30))
            println("📊 Total: $total")
            println("✅ Passed: $passed")
            println("❌ Failed: $failed")
            println("⏭️  Skipped: $skipped")
            println("📈 Success Rate: ${if (total > 0) (passed * 100 / total) else 0}%")
            println("━".repeat(30))

            if (failed > 0) {
                println("🚨 Some tests failed!")
            } else {
                println("🎉 All tests passed!")
            }
        }
    }
}