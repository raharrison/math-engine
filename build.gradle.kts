plugins {
    java
    jacoco
}

group = "uk.co.ryanharrison"
version = "1.0.0"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(25)
    }
    withSourcesJar()
//    withJavadocJar()
}

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.11.4"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation("org.assertj:assertj-core:3.27.6")
    testImplementation("com.fasterxml.jackson.core:jackson-databind:2.20.1")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.test {
    useJUnitPlatform()
    finalizedBy(tasks.jacocoTestReport)

    // Improved test output configuration for better feedback
    testLogging {
        events("skipped", "failed")

        // Show exceptions and their causes
        exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
        showExceptions = true
        showCauses = true
        showStackTraces = true
    }

    // Generate detailed HTML reports for better analysis
    reports {
        html.required = true
        junitXml.required = true
    }
}

// Add task to print test results summary from XML
tasks.register("testSummary") {
    group = "verification"
    description = "Prints a summary of test results from XML reports"
    doLast {
        val testResultsDir = file("build/test-results/test")
        if (!testResultsDir.exists()) {
            println("No test results found. Run './gradlew test' first.")
            return@doLast
        }

        var totalTests = 0
        var failures = 0
        var errors = 0
        var skipped = 0
        val failedTests = mutableListOf<String>()

        fileTree(testResultsDir).matching {
            include("**/*.xml")
        }.forEach { xmlFile ->
            val doc = javax.xml.parsers.DocumentBuilderFactory.newInstance()
                .newDocumentBuilder()
                .parse(xmlFile)

            val testSuites = doc.documentElement
            totalTests += testSuites.getAttribute("tests").toIntOrNull() ?: 0
            failures += testSuites.getAttribute("failures").toIntOrNull() ?: 0
            errors += testSuites.getAttribute("errors").toIntOrNull() ?: 0
            skipped += testSuites.getAttribute("skipped").toIntOrNull() ?: 0

            // Collect failed test names
            val failedElements = doc.getElementsByTagName("failure")
            for (i in 0 until failedElements.length) {
                val testcase = failedElements.item(i).parentNode
                val className = testcase.attributes.getNamedItem("classname").nodeValue
                val methodName = testcase.attributes.getNamedItem("name").nodeValue
                failedTests.add("  - $className.$methodName")
            }
        }

        println("\n" + "=".repeat(80))
        println("TEST SUMMARY")
        println("=".repeat(80))
        println("Total tests: $totalTests")
        println("Passed: ${totalTests - failures - errors - skipped}")
        println("Failed: $failures")
        println("Errors: $errors")
        println("Skipped: $skipped")

        if (failedTests.isNotEmpty()) {
            println("\nFailed tests:")
            failedTests.forEach { println(it) }
        }
        println("=".repeat(80) + "\n")

        println("View detailed HTML report: build/reports/tests/test/index.html")
    }
}

tasks.jacocoTestReport {
    dependsOn(tasks.test)

    reports {
        xml.required = true
        html.required = true
        csv.required = false
    }
}

tasks.jacocoTestCoverageVerification {
    violationRules {
        rule {
            limit {
                minimum = "0.0".toBigDecimal() // Set to 0 initially, increase as tests are added
            }
        }
    }
}
