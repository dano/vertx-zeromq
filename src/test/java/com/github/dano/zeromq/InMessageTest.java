package com.github.dano.zeromq;

import com.github.dano.zeromq.impl.InMessageImpl;
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
 * Test InMessageImpl.
 */
public class InMessageTest {
  private static final byte[] ID = "id".getBytes();
  private static final PayloadImpl MSG = new PayloadImpl("test msg".getBytes());
  private static final byte[] ADDRESS = "testAddress".getBytes();
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
    InMessageImpl msg = new InMessageImpl(ID, MSG);
    assertTrue(msg.isControl());
    assertEquals(ID, msg.getId());
    assertEquals(MSG, msg.getPayload());
    assertNull(msg.getAddress());
  }

  @Test
  public void testNormalInMessage() {
    InMessageImpl msg = new InMessageImpl(ID, ADDRESS, MSG);
    assertFalse(msg.isControl());
    assertEquals(ID, msg.getId());
    assertEquals(MSG, msg.getPayload());
    assertEquals(ADDRESS, msg.getAddress());
  }

}
