package com.github.dano.zeromq;

import com.github.dano.zeromq.impl.OutMessageImpl;
import com.github.dano.zeromq.impl.PayloadImpl;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.zeromq.ZMQ;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * Test OutMessageImpl.
 */
public class OutMessageTest {
  private static final byte [] ID = "id".getBytes();
  private static final PayloadImpl MSG = new PayloadImpl("test msg".getBytes());
  private static final byte [] REPLY_ADDRESS = "testAddress".getBytes();
  public static final String ADDR = "tcp://localhost:5558";

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
  public void testOutMessageWithoutReply() {
    OutMessage msg = new OutMessageImpl(ID, MSG);
    assertFalse(msg.isRepliable());
    assertEquals(ID, msg.getId());
    assertEquals(MSG, msg.getMsg());
    assertNull(msg.getReplyAddress());
  }

  @Test
  public void testOutMessageWithReply() {
    OutMessage msg = new OutMessageImpl(ID, MSG, REPLY_ADDRESS);
    assertTrue(msg.isRepliable());
    assertEquals(ID, msg.getId());
    assertEquals(MSG, msg.getMsg());
    assertEquals(REPLY_ADDRESS, msg.getReplyAddress());
  }

  @Test(timeout = 4000L)
  public void testSendMessageNoReply() throws InterruptedException {
    OutMessage msg = new OutMessageImpl(ID, MSG);
    Thread.sleep(500);
    msg.sendMessage(push);
    assertEquals(new String(ID), new String(pull.recv()));
    assertEquals(new String(MSG.getMsg()), new String(pull.recv()));
    assertFalse(pull.hasReceiveMore());

  }

  @Test(timeout = 4000L)
  public void testSendMessageWithReply() throws InterruptedException {
    OutMessage msg = new OutMessageImpl(ID, MSG, REPLY_ADDRESS);
    Thread.sleep(500);
    msg.sendMessage(push);
    assertEquals(new String(ID), new String(pull.recv()));
    assertEquals(new String(MSG.getMsg()), new String(pull.recv()));
    assertEquals(new String(REPLY_ADDRESS), new String(pull.recv()));
    assertFalse(pull.hasReceiveMore());

  }
}
