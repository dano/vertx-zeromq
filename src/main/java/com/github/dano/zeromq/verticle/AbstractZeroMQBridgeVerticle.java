package com.github.dano.zeromq.verticle;

import com.github.dano.zeromq.ZeroMQBridge;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

/**
 * An Abstract Verticle used to start up a ZeroMQBridge.
 */
public abstract class AbstractZeroMQBridgeVerticle extends AbstractVerticle {
  public static final String DEFAULT_ADDRESS = "tcp://localhost:5558";
  public static final long DEFAULT_TIMEOUT = 5000L;
  private static final Logger LOG = LoggerFactory.getLogger(AbstractZeroMQBridgeVerticle.class);
  private ZeroMQBridge bridge;

  @Override
  public void start() {

    String address = config().getString("address", DEFAULT_ADDRESS);
    long timeout = config().getLong("timeout", DEFAULT_TIMEOUT);

    LOG.info("Starting ZeroMQBridge on {0}", address);
    bridge = getBridgeImpl(address, timeout);
    try {
      bridge.start();
      LOG.info("ZeroMQBridge started on {0}", address);
    } catch (Exception e) {
      LOG.error("Failed to start ZeroMQBridge", e);
    }
  }

  /**
   * Returns an instance of a ZeroMQ bridge.
   *
   * @param address The address to listen on.
   * @param timeout The timeout for replies.
   * @return A ZeroMQBridge instance.
   */
  protected abstract ZeroMQBridge getBridgeImpl(String address, long timeout);

  @Override
  public void stop() {
    if (bridge != null)
      bridge.stop();
  }
}
