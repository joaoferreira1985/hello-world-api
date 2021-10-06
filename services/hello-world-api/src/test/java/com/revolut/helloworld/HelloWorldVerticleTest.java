package com.revolut.helloworld;

import com.revolut.helloworld.model.Person;
import de.flapdoodle.embed.mongo.MongodExecutable;
import de.flapdoodle.embed.mongo.MongodProcess;
import de.flapdoodle.embed.mongo.MongodStarter;
import de.flapdoodle.embed.mongo.config.IMongodConfig;
import de.flapdoodle.embed.mongo.config.MongodConfigBuilder;
import de.flapdoodle.embed.mongo.config.Net;
import de.flapdoodle.embed.mongo.distribution.Version;
import de.flapdoodle.embed.process.runtime.Network;
import io.vertx.core.*;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.*;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.net.ServerSocket;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@RunWith(VertxUnitRunner.class)
public class HelloWorldVerticleTest {

  private Vertx vertx;
  private Integer port;
  private static MongodProcess MONGO;
  private static int MONGO_PORT = 12345;


  @BeforeClass
  public static void initialize() throws IOException {
    MongodStarter starter = MongodStarter.getDefaultInstance();

    IMongodConfig mongodConfig = new MongodConfigBuilder()
            .version(Version.Main.PRODUCTION)
            .net(new Net(MONGO_PORT, Network.localhostIsIPv6()))
            .build();

    MongodExecutable mongodExecutable = starter.prepare(mongodConfig);
    MONGO = mongodExecutable.start();

  }

  @AfterClass
  public static void shutdown() {
    MONGO.stop();
  }

  @Before
  public void setUp(TestContext context) throws IOException {
    vertx = Vertx.vertx();

    ServerSocket socket = new ServerSocket(0);
    port = socket.getLocalPort();
    socket.close();

    DeploymentOptions options = new DeploymentOptions()
            .setConfig(new JsonObject()
                    .put("http.port", port)
                    .put("db_name", "whiskies")
                    .put("connection_string", "mongodb://localhost:" + MONGO_PORT)
            );

    vertx.deployVerticle(HelloWorldVerticle.class.getName(), options, context.asyncAssertSuccess());
  }

  @After
  public void tearDown(TestContext context) {
    vertx.close(context.asyncAssertSuccess());
  }

  @Test
  public void checkStatusEndpoint(TestContext context) {

    final Async async = context.async();

    vertx.createHttpClient().getNow(port, "localhost", "/status", response -> {
      response.handler(body -> {
        context.assertTrue(response.statusCode()==200);
        async.complete();
      });
    });
  }


  @Test
  public void checkThatWeCanPut(TestContext context) {
    Async async = context.async();
    final String jsonBodyPutDateOfBirth = Json.encodePrettily(new Person("dateOfBirth", "1985-10-22"));
    vertx.createHttpClient().put(port, "localhost", "/hello/joao")
            .putHeader("content-type", "application/jsonBodyPutDateOfBirth")
            .putHeader("content-length", Integer.toString(jsonBodyPutDateOfBirth.length()))
            .handler(response -> {
              context.assertEquals(response.statusCode(), 204);
            async.complete();
            })
            .write(jsonBodyPutDateOfBirth)
            .end();
  }

  @Test
  public void checkThatWeCanPutValidateTheDateBeforeToday(TestContext context) {
    Async async = context.async();
    final String jsonBodyPutDateOfBirth = Json.encodePrettily(new Person("dateOfBirth", "2021-10-22"));
    vertx.createHttpClient().put(port, "localhost", "/hello/joao")
            .putHeader("content-type", "application/jsonBodyPutDateOfBirth")
            .putHeader("content-length", Integer.toString(jsonBodyPutDateOfBirth.length()))
            .handler(response -> {
              context.assertEquals(response.statusCode(), 412);
              async.complete();

            })
            .write(jsonBodyPutDateOfBirth)
            .end();
  }
  @Test
  public void checkThatWeCanPutOnlyWithLetters(TestContext context) {
    Async async = context.async();
    final String jsonBodyPutDateOfBirth = Json.encodePrettily(new Person("dateOfBirth", "1985-10-22"));
    vertx.createHttpClient().put(port, "localhost", "/hello/123123")
            .putHeader("content-type", "application/jsonBodyPutDateOfBirth")
            .putHeader("content-length", Integer.toString(jsonBodyPutDateOfBirth.length()))
            .handler(response -> {
              context.assertEquals(response.statusCode(), 412);
              async.complete();

            })
            .write(jsonBodyPutDateOfBirth)
            .end();
  }
  @Test
  public void checkThatWeCanGetBirthdayOfUser(TestContext context) {
    Async async = context.async();
    vertx.createHttpClient().get(port, "localhost", "/hello/test") //Create some dummy data for have this test
            .handler(response -> {
              response.bodyHandler(body -> {
                context.assertEquals(response.statusCode(), 200);
                context.assertTrue(body.toString().contains("Happy birthday!"));
                async.complete();
              });
            })
            .end();
  }


}
