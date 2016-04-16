package com.github.dano.zeromq;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.zeromq.ZMQ;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * Test InMessage.
 */
public class InMessageTest {
  private static final byte [] ID = "id".getBytes();
  private static final byte [] MSG = "test msg".getBytes();
  private static final byte [] ADDRESS = "testAddress".getBytes();
  private static final String ADDR = "tcp://localhost:5558";
  ZMQ.Context ctx;
  ZMQ.Socket push;
  ZMQ.Socket pull;

  @Before
  public void before() {
    ctx = ZMQ.context(2);
    push = ctx.socket(ZMQ.PUSH);
    pull = ctx.socket(ZMQ.PULL);
    push.bind(ADDR);
    pull.connect(ADDR);
  }

  @After
  public void after() {
    push.close();
    pull.close();
    ctx.term();
  }

  @Test
  public void testControlInMessage() {
    InMessage msg = new InMessage(ID, MSG);
    assertTrue(msg.isControl());
    assertEquals(ID, msg.getId());
    assertEquals(MSG, msg.getMsg());
    assertNull(msg.getAddress());
  }

  @Test
  public void testNormalInMessage() {
    InMessage msg = new InMessage(ID, ADDRESS, MSG);
    assertFalse(msg.isControl());
    assertEquals(ID, msg.getId());
    assertEquals(MSG, msg.getMsg());
    assertEquals(ADDRESS, msg.getAddress());
  }

  @Test(timeout = 4000L)
  public void testSendMessageNoReply() throws InterruptedException {
    InMessage msg = new InMessage(ID, MSG);
    Thread.sleep(500);
    msg.sendMessage(push);
    assertEquals(new String(ID), new String(pull.recv()));
    assertEquals(new String(MSG), new String(pull.recv()));
    assertFalse(pull.hasReceiveMore());

  }

  @Test(timeout = 4000L)
  public void testSendMessageWithReply() throws InterruptedException {
    InMessage msg = new InMessage(ID, ADDRESS, MSG);
    Thread.sleep(500);
    msg.sendMessage(push);
    assertEquals(new String(ID), new String(pull.recv()));
    assertEquals(new String(ADDRESS), new String(pull.recv()));
    assertEquals(new String(MSG), new String(pull.recv()));
    assertFalse(pull.hasReceiveMore());
  }
}
