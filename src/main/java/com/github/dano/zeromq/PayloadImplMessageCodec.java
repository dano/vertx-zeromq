/*
 * This is the confidential unpublished intellectual property of EMC Corporation,
 * and includes without limitation exclusive copyright and trade secret rights
 * of EMC throughout the world.
 */
package com.github.dano.zeromq;

import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.MessageCodec;

/**
 * TODO comment me
 */
public class PayloadImplMessageCodec implements MessageCodec<PayloadImpl, PayloadImpl> {
  @Override
  public void encodeToWire(Buffer buffer, PayloadImpl payload) {
    buffer.appendInt(payload.getMsg().length);
    buffer.appendBytes(payload.getMsg());

  }

  @Override
  public PayloadImpl decodeFromWire(int i, Buffer buffer) {
    int len = buffer.getInt(i);
    i+= 4;
    return new PayloadImpl(buffer.getBytes(i, i+len));
  }

  @Override
  public PayloadImpl transform(PayloadImpl payload) {
    return payload;
  }

  @Override
  public String name() {
    return "payloadImpl";
  }

  @Override
  public byte systemCodecID() {
    return -1;
  }
}
