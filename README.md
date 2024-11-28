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

To run the application needs a `mySQL` database connection. It is advised to use the following `docker-compose.yml` that you can find [here](/managment/docker-compose.yml)
```yml
services:
  db:
    image: mysql:9.1.0
    container_name: orderManagment
    restart: unless-stopped
    ports:
      - "3306:3306"
    environment:
      MYSQL_ROOT_PASSWORD: root
      MYSQL_DATABASE: managment
      MYSQL_USER: order-manager
      MYSQL_PASSWORD: mysecret
```
The command:
```bash
docker compose up -d
```
will start the container and
```bash
docker compose down
```
will stop the container.
Using
```bash
docker compose down -d
```
will also remove the created container.

> [!NOTE]
> You must run the `docker compose` command in the same directory of the file `docker-compose.yml`.

The jar with all the dependencies can be found in the release and can now run with the following command:

```bash
java -cp managment-1.0.0-jar-with-dependencies.jar managment.app.ManagmentSwingApp
```

The app is using `Hibernate` and will try to connect to the `mySQL` previously started.

If you don't want to use the jar you can clone the repository and in the `pom.xml` directory run the following command:

```bash
mvn clean package
```

The unit tests will start and it will be created the jar, that can be run with:

```bash
mvn exec:java -Dexec.mainClass="managment.app.ManagmentSwingApp"
```

Assuming you have `Maven` and at least `Java 11` installed.

> [!WARNING]
> The container must be starded even when using maven

