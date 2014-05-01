HILMA (http://www.hankintailmoitukset.fi/) scraper, API, and better reader.

### Develop

Create a development H2 database:

    mvn -Dspring.datasource.url=jdbc:h2:./devdb -Dspring.jpa.hibernate.ddl-auto=create clean test

Run:

    mvn spring-boot:run

Run with XRebel:

    mvn clean package
    java -javaagent:${XREBEL_HOME}/xrebel.jar -jar target/*.war

### Requirements

Java 8, Maven 3.
