# Spring Profiler Application

This is a Kotlin Multiplatform project targeting Desktop (JVM).

* [/composeApp](./composeApp/src) is for code that will be shared across your Compose Multiplatform applications.
  It contains several subfolders:
    - [commonMain](./composeApp/src/commonMain/kotlin) is for code that’s common for all targets.
    - Other folders are for Kotlin code that will be compiled for only the platform indicated in the folder name.
      For example, if you want to use Apple’s CoreCrypto for the iOS part of your Kotlin app,
      the [iosMain](./composeApp/src/iosMain/kotlin) folder would be the right place for such calls.
      Similarly, if you want to edit the Desktop (JVM) specific part, the [jvmMain](./composeApp/src/jvmMain/kotlin)
      folder is the appropriate location.

### Build and Run Desktop (JVM) Application

To build and run the development version of the desktop app, use the run configuration from the run widget
in your IDE’s toolbar or run it directly from the terminal:

- on macOS/Linux
  ```shell
  ./gradlew :composeApp:run
  ```
- on Windows
  ```shell
  .\gradlew.bat :composeApp:run
  ```

### Screens

#### Beans screen

Beans screen targets _/actuator/beans_ endpoint and renders Spring beans in all active contexts. Bean's dependencies are
clickable and navigate to the relevant
element.

#### Health screen

Health screen targets _/actuator/health_ endpoint. Data is polled every 5 seconds. Polling is done only if the screen is
rendered into the view.

#### Configuration properties screen

Configuration properties screen targets _/actuator/configprops_ endpoint and displays available configuration properties
in the server application.

#### Metrics screen

Metrics screen targets _/actuator/metrics_ and _/actuator/metrics/{requiredMetricName}_ endpoints. Metrics data is
polled every 3 seconds (1 + N calls, where N calls run concurrently) to provide near real-time performance insight.
Polling
is done only if the screen is rendered into the view.

### Running Tests

To run all tests:

```shell
./gradlew :composeApp:jvmTest
```

### Code Coverage

This project uses [Kover](https://github.com/Kotlin/kotlinx-kover) for code coverage reporting.

#### Generate HTML Coverage Report

```shell
./gradlew :composeApp:koverHtmlReport
```

The HTML report will be generated at: `composeApp/build/reports/kover/html/index.html`

#### Generate XML Coverage Report

```shell
./gradlew :composeApp:koverXmlReport
```

The XML report will be generated at: `composeApp/build/reports/kover/report.xml`

#### Verify Coverage Thresholds

```shell
./gradlew :composeApp:koverVerify
```

This will verify that the code coverage meets the minimum threshold (currently set to 70%).

#### Generate All Reports

```shell
./gradlew clean :composeApp:jvmTest :composeApp:koverHtmlReport :composeApp:koverXmlReport
```
