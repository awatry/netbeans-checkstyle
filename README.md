#Building netbeans-checkstyle

In order to build Checkstyle Beans from sources you have to do
the following steps. As a prerequisite you also need JDK 8 or
newer and Maven 3.

1. Build the netbeans-checkstyle-[plugin|library|extra]

```
cd /path/to/checkout/
mvn clean -pl netbeans-checkstyle-plugin,netbeans-checkstyle-library,netbeans-checkstyle-extra install
```

2. Package Checkstyle Beans.
    - `mvn install assembly:attached nbm:autoupdate`

3. NBM files are available in target directories
