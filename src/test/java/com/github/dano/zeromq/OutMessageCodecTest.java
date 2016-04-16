/**
 * Created by dan on 4/17/16.
 */
package com.github.dano.zeromq;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import io.vertx.core.Vertx;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;

/**
 * Test the OutMessageCodec class
 */
@RunWith(VertxUnitRunner.class)
public class OutMessageCodecTest {
  private Vertx vertx;

  @Before
  public void before() {
    vertx = Vertx.vertx();
    OutMessageCodec codec = new OutMessageCodec();
    vertx.eventBus().registerDefaultCodec(OutMessage.class, codec);
  }

  @Test
  public void testCodec(TestContext context) {
    final Async async = context.async(2);
    OutMessage outMessage = new OutMessage("id".getBytes(), "mymsgmsg".getBytes());
    vertx.eventBus().<OutMessage>consumer("chan", msg -> {
      context.assertEquals(outMessage, msg.body());
      async.countDown();
    });
    vertx.eventBus().send("chan", outMessage);

    OutMessage outMessage2 = new OutMessage("id".getBytes(),
        "mymsgmsgsdfsdfsafa  asdf asdfsd sf".getBytes(),
        "addresssdfsfsdfs".getBytes());
    vertx.eventBus().<OutMessage>consumer("chan2", msg -> {
      context.assertEquals(outMessage2, msg.body());
      async.countDown();
    });
    vertx.eventBus().send("chan2", outMessage2);
  }
}
