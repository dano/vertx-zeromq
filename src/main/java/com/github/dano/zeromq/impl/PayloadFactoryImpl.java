package com.github.dano.zeromq.impl;

import com.github.dano.zeromq.Payload;
import com.github.dano.zeromq.PayloadFactory;

/**
 * Default PayloadFactory implementation.
 */
public class PayloadFactoryImpl implements PayloadFactory<PayloadImpl> {

  @Override
  public Payload fromBytes(byte[] msg) {
    return new PayloadImpl(msg);
  }

  @Override
  public Class<PayloadImpl> getPayloadType() {
    return PayloadImpl.class;
  }
}
