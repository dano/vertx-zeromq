package com.github.dano.zeromq.vertx;

import com.github.dano.zeromq.PayloadImpl;
import com.github.dano.zeromq.PayloadImplMessageCodec;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.json.JsonObject;

/**
 * A TestVerticle that registers an EventBus consumer.
 */
public class TestVerticle extends AbstractVerticle {
  public static final String CHANNEL = "testChannel";
  public static final String REPLY = "you win";
  @Override
  public void start() {
    vertx.eventBus().<PayloadImpl>consumer(CHANNEL, msg -> {
      JsonObject json = new JsonObject(new String(msg.body().getMsg()));
      DeliveryOptions options = new DeliveryOptions().setCodecName("payloadImpl");
      vertx.eventBus().send(json.getString("replyChannel"), new PayloadImpl(REPLY.getBytes()),
          options);
    });
  }
}
