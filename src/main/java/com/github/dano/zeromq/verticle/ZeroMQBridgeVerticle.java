package com.github.dano.zeromq.verticle;

import com.github.dano.zeromq.impl.ZeroMQBridgeImpl;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

/**
 * The verticle that starts up the ZeroMQ bridge.
 */
public class ZeroMQBridgeVerticle extends AbstractVerticle {

  public static final String DEFAULT_ADDRESS = "tcp://localhost:5558";
  public static final long DEFAULT_TIMEOUT = 5000L;
  private static final Logger LOG = LoggerFactory.getLogger(ZeroMQBridgeVerticle.class);
  private ZeroMQBridgeImpl bridge;

  @Override
  public void start() {

    String address = config().getString("address", DEFAULT_ADDRESS);
    long timeout = config().getLong("timeout", DEFAULT_TIMEOUT);

    LOG.info("Starting ZeroMQBridge on {0}", address);
    bridge = new ZeroMQBridgeImpl(address, vertx, timeout);
    try {
      bridge.start();
      LOG.info("ZeroMQBridge started on {0}", address);
    } catch (Exception e) {
      LOG.error("Failed to start ZeroMQBridge", e);
    }
  }

  @Override
  public void stop() {
    if (bridge != null)
      bridge.stop();
  }
}
