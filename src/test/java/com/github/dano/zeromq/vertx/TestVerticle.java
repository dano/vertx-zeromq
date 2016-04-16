package com.github.dano.zeromq.vertx;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.json.JsonObject;

/**
 * A TestVerticle that registers an EventBus consumer.
 */
public class TestVerticle extends AbstractVerticle {
  public static final String CHANNEL = "testChannel";
  public static final String REPLY = "you win";
  @Override
  public void start() {
    vertx.eventBus().<byte[]>consumer(CHANNEL, msg -> {
      JsonObject json = new JsonObject(new String(msg.body()));
      vertx.eventBus().send(json.getString("replyChannel"), REPLY.getBytes());
    });
  }
}
