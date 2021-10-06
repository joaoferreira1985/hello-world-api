#  API example

Requirements for build:
Openjdk15 and Maven



## Building

You build the project using:
```
mvn package
```

## Testing
```
mvn test
```

The application is tested using [vertx-unit](http://vertx.io/docs/vertx-unit/java/).

## Packaging

The application is packaged as a _fat jar_, using the 
[Maven Shade Plugin](https://maven.apache.org/plugins/maven-shade-plugin/).

## Running

Once packaged, just launch the _fat jar_ as follows:

```
java -jar target/hello-wold-api-1.0-SNAPSHOT.jar
```
docker-compose :
```
docker compose up --build
```

Then, open a browser to http://localhost:11011




java -jar    target/hello-wold-api-1.0-SNAPSHOT.jar  -conf=src/main/conf/my-application-conf-[local,environment].json 
