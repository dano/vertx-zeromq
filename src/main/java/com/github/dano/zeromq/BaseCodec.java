package com.github.dano.zeromq;

import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.MessageCodec;

/**
 * A base codec for Payloads and Responses.
 */
public abstract class BaseCodec<T> implements MessageCodec<T, T> {

  @Override
  public T transform(T obj) {
    return obj;
  }


  protected void addBytes(Buffer buffer, byte[] item) {
    buffer.appendInt(item.length);
    buffer.appendBytes(item);
  }

  @Override
  public String name() {
    return getClass().getSimpleName();
  }

  @Override
  public byte systemCodecID() {
    return -1;
  }
}
