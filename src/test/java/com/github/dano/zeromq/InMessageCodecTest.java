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
 * Test the InMessageCodec class
 */
@RunWith(VertxUnitRunner.class)
public class InMessageCodecTest {
  private Vertx vertx;

  @Before
  public void before() {
    vertx = Vertx.vertx();
    InMessageCodec codec = new InMessageCodec();
    vertx.eventBus().registerDefaultCodec(InMessage.class, codec);
  }

  @Test
  public void testCodec(TestContext context) {
    final Async async = context.async(2);
    InMessage inMessage = new InMessage("id".getBytes(), "mymsgmsg".getBytes());
    vertx.eventBus().<InMessage>consumer("chan", msg -> {
      context.assertEquals(inMessage, msg.body());
      async.countDown();
    });
    vertx.eventBus().send("chan", inMessage);

    InMessage inMessage2 = new InMessage("id".getBytes(), "addresssdfsfsdfs".getBytes(),
        "mymsgmsgsdfsdfsafa  asdf asdfsd sf".getBytes());
    vertx.eventBus().<InMessage>consumer("chan2", msg -> {
      context.assertEquals(inMessage2, msg.body());
      async.countDown();
    });
    vertx.eventBus().send("chan2", inMessage2);
  }
}
