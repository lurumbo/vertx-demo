package com.pigmalion.vertx_demo;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Promise;
import io.vertx.core.json.Json;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;


public class MainVerticle extends AbstractVerticle {

  private Logger logger = LoggerFactory.getLogger(MainVerticle.class);

  @Override
  public void start(Promise<Void> startPromise) throws Exception {

    Router router = Router.router(vertx);

    router.route("/").handler(this::sayHi);
    router.route("/users").handler(this::getUsers);

    // Creates a HTTP server
    vertx
      .createHttpServer()
      .requestHandler(router)
      .listen(8080, http -> {
      if (http.succeeded()) {
        vertx.deployVerticle(DBVerticle.class.getName(), res -> {
          if (res.succeeded()) logger.info("Database Verticle was deployed");
          else logger.error("Error: Database Verticle wasn't deployed: " + res.cause().getMessage());
        });
        startPromise.complete();
        logger.info("HTTP server started on port 8080");
      } else {
        logger.error("HTTP server started error");
        startPromise.fail(http.cause());
      }
    });

  }

  private void getUsers(RoutingContext routingContext) {

    vertx.eventBus().request("GET_USERS", "", reply -> {
      //logger.info("GET /users");
      if (reply.succeeded()) {
        String users = reply.result().body().toString();
        routingContext
          .response()
          .putHeader("content-type", "text/json")
          .end(users);
      } else {
        logger.error("Error: GET_USERS address not handled: " + reply.cause().getMessage());
      }
    });

  }

  private void sayHi(RoutingContext routingContext) {
    routingContext
      .response()
      .putHeader("content-type", "text/plain")
      .end("Hello from Vert.x!");
  }

}
