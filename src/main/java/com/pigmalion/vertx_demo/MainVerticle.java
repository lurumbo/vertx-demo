package com.pigmalion.vertx_demo;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Promise;
import io.vertx.core.json.Json;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;


public class MainVerticle extends AbstractVerticle {

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
          if (res.succeeded()) System.out.println("Database Verticle was deployed");
          else System.err.println("Error: Database Verticle wasn't deployed: " + res.cause().getMessage());
        });
        startPromise.complete();
        System.out.println("HTTP server started on port 8080");
      } else {
        System.out.println("HTTP server started error");
        startPromise.fail(http.cause());
      }
    });

  }

  private void getUsers(RoutingContext routingContext) {

    vertx.eventBus().request("GET_USERS", "", reply -> {
      if (reply.succeeded()) {
        String users = reply.result().body().toString();
        routingContext
          .response()
          .putHeader("content-type", "text/json")
          .end(users);
      } else {
        System.err.println("Error: GET_USERS address not handled: " + reply.cause().getMessage());
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
