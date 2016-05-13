package com.github.dano.zeromq.verticle;

import com.github.dano.zeromq.ZeroMQBridge;
import com.github.dano.zeromq.impl.ZeroMQBridgeImpl;

/**
 * The verticle that starts up the ZeroMQ bridge.
 */
public class ZeroMQBridgeVerticle extends AbstractZeroMQBridgeVerticle {

  @Override
  protected ZeroMQBridge getBridgeImpl(String address, long timeout) {
    return new ZeroMQBridgeImpl(address, vertx, timeout);
  }
}
