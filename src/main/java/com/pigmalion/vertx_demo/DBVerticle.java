package com.pigmalion.vertx_demo;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.eventbus.Message;
import io.vertx.pgclient.PgConnectOptions;
import io.vertx.pgclient.PgPool;
import io.vertx.sqlclient.*;

import java.util.ArrayList;

public class DBVerticle extends AbstractVerticle {

  private PgPool dbClient;

  @Override
  public void start () throws Exception {

    PgConnectOptions pgConnectOptions = new PgConnectOptions()
        .setPort(5432)
        .setHost("localhost")
        .setDatabase("usersdb")
        .setUser("postgres")
        .setPassword("1111");

    PoolOptions poolOptions = new PoolOptions().setMaxSize(5);

    dbClient = PgPool.pool(vertx, pgConnectOptions, poolOptions);

    dbClient.getConnection(res -> {

      if (res.succeeded()) {
          System.out.println("Database Connection Succeeded");
          vertx.eventBus().consumer("GET_USERS", this::getUsers);
      } else {
        System.err.println("Database Connection failed: " + res.cause().getMessage());
        return;
      }

      // dbClient.close();

    });

  }

  private void getUsers(Message message) {
    dbClient
      .query("SELECT * FROM users")
      .execute(res -> {
        if (res.succeeded()) {
          ArrayList<User> usersArrayList = new ArrayList<>();
          RowSet<Row> rows = res.result();
          for (Row row : rows) {
            Long id = row.getLong("id");
            String name = row.getString("name");
            usersArrayList.add(new User(id, name));
          }
          message.reply(usersArrayList.toString());
        } else {
          System.err.println("Database Execution Query Failed: " + res.cause().getMessage());
        }
      });
  }


}
