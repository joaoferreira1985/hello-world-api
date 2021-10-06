package com.revolut.helloworld;

import com.revolut.helloworld.model.Person;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.MongoClient;
import io.vertx.ext.mongo.UpdateOptions;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.logging.Logger;


public class HelloWorldVerticle extends AbstractVerticle {

  private final static Logger LOGGER = Logger.getLogger(HelloWorldVerticle.class.getName());

  public static final String COLLECTION = "Person";
  private MongoClient mongo;


  @Override
  public void start(Future<Void> fut)  {

    int serverPort = config().getInteger("http.port", 8080);
    LOGGER.info("Backend will listen on port "+ serverPort);

    String mongoHost = config().getString("connection_string","mongodb://localhost:27017");
    LOGGER.info("Backend will connect to "+ mongoHost);

    // Create a Mongo client
    mongo = MongoClient.createShared(vertx, config());


    createSomeData(
            (nothing) -> startApi(
                    (http) -> completeStartup(http, fut)
            ), fut);


  }

  private void startApi(Handler<AsyncResult<HttpServer>> next) {
    // Create a router object.
    Router router = Router.router(vertx);

    router.route("/hello/*").handler(BodyHandler.create());
    router.put("/hello/:username").handler(this::createOrUpdate);
    router.get("/hello/:username").handler(this::getOne);


    router.route("/status").handler(routingContext -> {
      HttpServerResponse response = routingContext.response();
      response.setStatusCode(200).end("OK");
    });

    // Create the HTTP server and pass the "accept" method to the request handler.
    vertx
        .createHttpServer()
        .requestHandler(router::accept)
        .listen(
            config().getInteger("http.port", 8080),
                next::handle
        );
  }

  private void completeStartup(AsyncResult<HttpServer> http, Future<Void> fut) {
    if (http.succeeded()) {
      fut.complete();
    } else {
      fut.fail(http.cause());
    }
  }


  @Override
  public void stop()  {
    mongo.close();
  }


  private void createOrUpdate(RoutingContext rc) {

    DateHelpers.isDateBeforeTheToday(rc);

    UpdateOptions options = new UpdateOptions().setUpsert(true);

    String username = rc.pathParam("username");
    String dateOfBirth = rc.getBodyAsJson().getString("dateOfBirth");

    final Person whisky = new Person(username,dateOfBirth);

    if (username == null || dateOfBirth == null  || DateHelpers.isDateBeforeTheToday(rc) || !username.chars().allMatch(Character::isLetter)) {
      rc.response().setStatusCode(412).end();
    } else {
      mongo.updateWithOptions(COLLECTION,
              new JsonObject().put("_id", username),
              // The update syntax: {$set, the json object containing the fields to update}
              new JsonObject()
                      .put("$set", whisky.toJson()),
              options,
              v -> {
                if (v.failed()) {
                  rc.response().setStatusCode(500).end();
                } else {
                  rc.response().setStatusCode(204).end("");
                }
              });
    }
  }

  private void getOne(RoutingContext rc) {
    final String username = rc.pathParam("username");
    if (username == null) {
      rc.response().setStatusCode(400).end();
    } else {
      mongo.findOne(COLLECTION, new JsonObject().put("_id", username), null, ar -> {
        if (ar.succeeded()) {
          if (ar.result() == null) {
            rc.response().setStatusCode(404).end();
            return;
          }
          Person whisky = new Person(ar.result());
          LocalDate dateOfBirth = LocalDate.parse(whisky.getDateOfBirth(), DateTimeFormatter.ofPattern("yyyy-MM-dd"));

          JsonObject httpResponse = whisky.jsonHttpResponse(whisky.getUsername(), DateHelpers.calculateDaysToBirthday(dateOfBirth));
          rc.response()
              .setStatusCode(200)
              .putHeader("content-type", "application/json; charset=utf-8")
                   .end(Json.encodePrettily(httpResponse));
        } else {
          rc.response().setStatusCode(404).end();
        }
      });
    }
  }
  private void createSomeData(Handler<AsyncResult<Void>> next, Future<Void> fut) {

    DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    Person Joao = new Person("test", dateFormat.format(new Date()));



    // Do we have data in the collection ?
    mongo.count(COLLECTION, new JsonObject(), count -> {
      if (count.succeeded()) {
        if (count.result() == 0) {
          // no Person, insert data
          mongo.insert(COLLECTION,  new JsonObject().put("_id", Joao.getUsername()).put("dateOfBirth",Joao.getDateOfBirth()), ar -> {
            if (ar.failed()) {
              fut.fail(ar.cause());
            } else {
                  next.handle(Future.<Void>succeededFuture());
            }
          });
        } else {
          next.handle(Future.<Void>succeededFuture());
        }
      } else {
        // report the error
        fut.fail(count.cause());
      }
    });
  }
}