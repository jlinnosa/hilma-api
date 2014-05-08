HILMA (http://www.hankintailmoitukset.fi/) scraper, API, and better reader.

### Develop

Create a development H2 database:

    mvn -Dspring.datasource.url=jdbc:h2:./devdb -Dspring.jpa.hibernate.ddl-auto=create clean test

Run:

    mvn spring-boot:run

Run with a persistent H2 database:

    mvn -Dspring.profiles.active=dev-persistent spring-boot:run

Run with XRebel:

    mvn clean package
    java -javaagent:${XREBEL_HOME}/xrebel.jar -jar target/*.war

### Requirements

Java 8, Maven 3.


### Architecture

Scrapes data with Jsoup + Java 8 streams and Optional.

Saves it either to a H2 dev database, or a PostgreSQL production database.

Provides a REST JSON interface to the data with Spring Data REST.

Provides JPA data structures and DAO with Lombok and Spring Data JPA.

Supports Java 8 ``java.time`` types JSR-310 with Hibernate and Jackson extensions.

Provides a HTML5 WebSocket STOMP interface to new data events with Spring Framework 4.

Consumes new data events on client side with SockJS and Stomp.js.

Handles client-side timestamp data with Moment.js.

Provides a single-page user interface with KnockoutJS and JQuery.

Timeline UI concept from @codrops.

Unit tests with JUnit and spring-test.

### TODO

* As a salesperson, I want to hide uninteresting categories' notices.

* As a salesperson, I want my ignores to be available at my next session.

* As a salesperson, I want to select interesting notices to be moved to SalesForce.

* As a devops admin, I want to deploy the app on Heroku.

* As a devops admin, I want to monitor the application on New Relic and Papertrail.
