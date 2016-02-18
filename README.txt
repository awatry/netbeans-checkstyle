In order to build Checkstyle Beans from sources you have to do
the following steps. As a prerequisite you also need JDK 7 or
newer and Maven 3.

0) Build the netbeans-checkstyle-[plugin|library|extra]
cd /path/to/checkout/
mvn clean -pl netbeans-checkstyle-plugin,netbeans-checkstyle-library,netbeans-checkstyle-extra install

1) Package Checkstyle Beans.
mvn install assembly:attached nbm:autoupdate

2) NBM files are available in target directories
