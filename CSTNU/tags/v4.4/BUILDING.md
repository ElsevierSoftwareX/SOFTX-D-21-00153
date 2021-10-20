# Building CSTNU Tool library
version 1.0


The CSTNU Tool project is a Maven project.
File `pom.xml` in the main directory contains all directives for compiling, testing, building the fat jar, and buinding the main site of the project.

Summary of the main maven commands:

* `mvn clean`
    Cleans the enviroment removing all dangling or temporary files

* `mvn compile`
    Compiles all the sources

* `mvn test`
    Executes all JUNIT4 tests

* `mvn package`
    Builds the jar package `CSTNU-Tool-*.*.jar` and the binary-distribution archive `CstnuTool-*.*.tgz`.
    The archive  `CstnuTool-*.*.tgz` contains the JAR, some temporal networks instances and some scripts to ease the use of some classes (see `README.md`)

* `mvn package -Ddebug`
    As `mvn package` but with all debug messages enabled.
    To customize the level of debugging, it is necessary to give a `java.util.logging configuration` file as parameter to the JRE.
    In the `CstnuTool` directory there are two configuration files for logging: `logging.properties` and `nologging.properties` (`nologging.properties` is useful for using the JAR with debug activated but no debug message are required for an execution).
    For using the `logging.properties` file, it is sufficient to specify it as JRE parameter (it is assumed that both `logging.properties` and `CSTNU-Tool-4.3.jar` are in the current directory):

     ```bash
     java -Djava.util.logging.config.file=logging.properties -cp CSTNU-Tool-4.3.jar ...
     ```

* ` mvn spotbugs:check `
    It makes a static analysys of Java sources.
    Behind some warning about the tool (ignore them!), the output must be equal to
    ```bash
    [INFO] --- spotbugs-maven-plugin:4.2.3:check (default-cli) @ CSTNU-Tool ---
    [INFO] BugInstance size is 0
    [INFO] Error size is 0
    [INFO] No errors/warnings found
    [INFO] ------------------------------------------------------------------------
    [INFO] BUILD SUCCESS
    [INFO] ------------------------------------------------------------------------
    ```

## Before a SVN commit
Before committing a new version of a class/method, it is necessary:
- to add/update relative Junit tests for testing the behavior of the new/modified class/method.
  The scope of Junit test is to *certificate* that the class/method works as expected in some scenarios.

  Add as many tests as it is possible :-)
  The `src/test` directory contains all Junit classes.
  Each class correponds to the class in the package. It has the same name with `Test` suffix.
  If a new class has been added to the package, a new Junit corresponding class must be added and the name of Junit class must be added in the `AllTests.java` class present in the same directory where the new Junut test class is added.
- to execute `mvn test`
  In case that some Junit test fails, it is necessary to fix the errors before proceeding.

- to execute mvn spotbugs:check
  If any error or **warning** is reported, fix it before continuing.

* to execute `mvn package`
  This step is just execute the previous ones in sequence... it is just a further *check* that everything is ok

At this point, it is possible to commit the new code.
