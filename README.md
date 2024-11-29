# Purchase Manager

Simple Purchase Management in Java Swing to demonstrate TDD and CI/CD techniques.

[![Coverage Status](https://coveralls.io/repos/github/cMancio00/order-managment/badge.svg)](https://coveralls.io/github/cMancio00/order-managment)
[![Coverage](https://sonarcloud.io/api/project_badges/measure?project=cMancio00_order-managment&metric=coverage)](https://sonarcloud.io/summary/new_code?id=cMancio00_order-managment)

[![Maintainability Rating](https://sonarcloud.io/api/project_badges/measure?project=cMancio00_order-managment&metric=sqale_rating)](https://sonarcloud.io/summary/new_code?id=cMancio00_order-managment)
[![Bugs](https://sonarcloud.io/api/project_badges/measure?project=cMancio00_order-managment&metric=bugs)](https://sonarcloud.io/summary/new_code?id=cMancio00_order-managment)
[![Technical Debt](https://sonarcloud.io/api/project_badges/measure?project=cMancio00_order-managment&metric=sqale_index)](https://sonarcloud.io/summary/new_code?id=cMancio00_order-managment)

[![Code Smells](https://sonarcloud.io/api/project_badges/measure?project=cMancio00_order-managment&metric=code_smells)](https://sonarcloud.io/summary/new_code?id=cMancio00_order-managment)
[![Duplicated Lines (%)](https://sonarcloud.io/api/project_badges/measure?project=cMancio00_order-managment&metric=duplicated_lines_density)](https://sonarcloud.io/summary/new_code?id=cMancio00_order-managment)
[![Vulnerabilities](https://sonarcloud.io/api/project_badges/measure?project=cMancio00_order-managment&metric=vulnerabilities)](https://sonarcloud.io/summary/new_code?id=cMancio00_order-managment)

Mutation report can be found [here](https://cmancio00.github.io/order-managment/).

![PurchaseManagerView](/pictures/PurchaseManagerView.png "Purchase Manager View")

This Java application is designed to manage orders using a `MySQL` database. It can be run in two main ways: via **JAR** and the **Docker Compose** or with **Maven**.

## Requiremets

- **Java 11** or later.
- A running `MySQL` database.

> [!NOTE]
> **Maven** is not required, as the project includes a Maven Wrapper (`mvnw`) version `3.9.9`.

## Running The Application

### 1. Using Docker Compose and the Jar

You can set up and run a `MySQL` instance using the provided `docker-compose.yml` file,
available [here](/managment/docker-compose.yml):

```yml
services:
  db:
    image: mysql:9.1.0
    container_name: orderManagment
    ports:
      - "3306:3306"
    environment:
      MYSQL_ROOT_PASSWORD: root
      MYSQL_DATABASE: managment
      MYSQL_USER: order-manager
      MYSQL_PASSWORD: mysecret
    volumes:
      - ./mysql:/var/lib/mysql
```
Useful commands

- Start the container
```bash
docker compose up -d
```
- Stop the container
```bash
docker compose down
```
- Remove the container:

```bash
docker compose down -v
```

> [!NOTE]
> Ensure you run the commands in the same directory as the `docker-compose.yml` file.

Data will be stored in a folder named `mysql`, which serves as the container's volume.

The JAR file with all dependencies included is available in the [Releases](https://github.com/cMancio00/order-managment/releases) section.

You can execute it with:

```bash
java -jar managment-1.1.0-jar-with-dependencies.jar
```

The application uses `Hibernate` to connect to the `MySQL` database.

### 2.Running via Maven

**Build the Application**

Clone the repository and, in the directory containing the pom.xml file, run:

```bash
./mvnw clean package
```
This command will run unit tests and generate the application JAR.

**Starting the Database with Maven**

You can start the `MySQL` container using Maven with:

```bash
./mvnw docker:start
```
The data will be stored in a folder called `mysql` which is the volume of the container.

**Running the Application**

Start the application with:

```bash
./mvnw exec:java
```
**Stopping the Database**

When you're done, stop the container with:

```bash
./mvnw docker:stop
```
## Testing and Reports

### Running Tests with Coverage and Mutation Testing

To run all tests, including code coverage and mutation testing, execute:

```bash
./mvnw clean verify -Pjacoco,mutation-testing
```
Reports will be generated in the following locations:

- **Code coverage**: `target/site/jacoco/index.html`.

- **Mutation report**: `target/pit-reports/index.html`.

### Generating Test Reports

To generate `Surefire` (unit tests) and `Failsafe` (IT and E2E tests) reports:

```bash
./mvnw surefire-report:report-only surefire-report:failsafe-report-only site:site -DgenerateReports=false
```

The reports will be available at:

- **Surefire**: `target/reports/surefire.html`
- **Failsafe**: `target/reports/failsafe.html`