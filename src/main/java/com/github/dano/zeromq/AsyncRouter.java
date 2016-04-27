package com.github.dano.zeromq;

import com.github.dano.zeromq.impl.AsyncRouterSocketImpl;
import org.zeromq.ZMQ;

import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

import java.util.function.BiConsumer;

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
  private final InMessageFactory inMessageFactory;
  private final OutMessageFactory outMessageFactory;

  /**
   * Create an AsyncRouter.
   *
   * @param address The address to listen for external ZMQ connections on.
   */
  public AsyncRouter(String address, InMessageFactory inMessageFactory,
                     OutMessageFactory outMessageFactory) {
    this.address = address;
    this.inMessageFactory = inMessageFactory;
    this.outMessageFactory = outMessageFactory;
  }

  /**
   * Handle an incoming request over the external 0MQ socket.
   *
   * @param message The incoming message.
   * @param responder The responder, used to send a response to the request.
   */
  protected abstract void handleRequest(InMessage message, MessageResponder responder);

  /**
   * Start up the ZMQ sockets.
   *
   * @return An instance of this, so the API can be used fluently.
   */
  public AsyncRouter start() {
    ctx = ZMQ.context(2);
    front = createZmqSocket(ctx, address, INPROC_ZMQ_ASYNC_BACKEND, inMessageFactory,
        outMessageFactory, this::handleRequest);
    new Thread(front).start();
    return this;
  }

  /**
   * Create the AsyncRouterSocket.
   *
   * @param ctx The ZMQ socket to use.
   * @param frontendAddress The address to use for the frontend.
   * @param backendAddress The address to use for the backend.
   * @param inMessageFactory A factory for creating InMessages.
   * @param outMessageFactory A factory for creating OutMessages.
   * @param handleBlockingRequest The function to use to handle received requests.
   * @return The AsyncRouterSocket.
   */
  protected AsyncRouterSocket createZmqSocket(ZMQ.Context ctx, String frontendAddress,
      String backendAddress, InMessageFactory inMessageFactory,
      OutMessageFactory outMessageFactory,
      BiConsumer<InMessage, MessageResponder> handleBlockingRequest) {
    return new AsyncRouterSocketImpl(ctx, frontendAddress, backendAddress,
        inMessageFactory, outMessageFactory, handleBlockingRequest);
  }

  /**
   * Stop the ZMQ sockets and terminate the ZMQ context.
   *
   * @return An instance of this, so the API can be used fluently.
   */
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