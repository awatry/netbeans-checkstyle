In order to build Checkstyle Beans from sources you have to do
the following steps. As a prerequisite you also need JDK 7 or
newer and Maven 3.

1) Build Checkstyle Beans.
cd /path/to/checkstyle/beans/
mvn clean install package assembly:attached nbm:autoupdate

2) NBM files are available in target directory.
