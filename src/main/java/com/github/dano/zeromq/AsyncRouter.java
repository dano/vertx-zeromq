package com.github.dano.zeromq;

import org.zeromq.ZMQ;

import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

/**
 * A ZeroMQ Router.
 *
 * Creates a ZeroMQ Context and an AsyncRouterSocket, which builds
 * ZMQ Sockets using that Context. The AsyncRouterSocket listens for
 * ZMQ connections to a provided address, and forwards them on to
 * a request handler provided to the router.
 *
 * The AsyncRouterSocket also listens for outgoing messages on an
 * internal address, which it then sends back out to reply to
 * external clients.
 */
public abstract class AsyncRouter {

  private static final String INPROC_ZMQ_ASYNC_BACKEND = "inproc://zmq-async-backend";
  private Logger LOG = LoggerFactory.getLogger(AsyncRouter.class);

  private AsyncRouterSocket front;
  private ZMQ.Context ctx;
  private final String address;

  public AsyncRouter(String address) {
    this.address = address;
  }

  /**
   * Handle an incoming request over the external 0MQ socket.
   *
   * @param message The incoming message.
   * @param responder The responder, used to send a response to the request.
   */
  protected abstract void handleRequest(InMessage message, MessageResponder responder);

  public AsyncRouter start() {
    ctx = ZMQ.context(2);
    front = new AsyncRouterSocket(ctx, address, INPROC_ZMQ_ASYNC_BACKEND, this::handleRequest);
    new Thread(front).start();
    return this;
  }

  public AsyncRouter stop() {
    if (front != null) {
      front.stop();
    }
    if (ctx != null) {
      ctx.term();
    }
    return this;
  }
}