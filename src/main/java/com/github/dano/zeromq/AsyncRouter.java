package com.github.dano.zeromq;

import org.zeromq.ZMQ;

import java.util.UUID;

import io.vertx.core.Vertx;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

/**
 * TODO comment me.
 */
public class AsyncRouter {

  private static final String INPROC_ZMQ_ASYNC_BACKEND = "inproc://zmq-async-backend";
  private Logger LOG = LoggerFactory.getLogger(AsyncRouter.class);

  private QueueListeningPublishSocket back;
  private AsyncRouterSocket front;
  private ZMQ.Context ctx;
  private final String address;
  private RequestHandler handler;
  protected final Vertx vertx;

  public AsyncRouter(String address, Vertx vertx) {
    this.address = address;
    this.vertx = vertx;
  }

  protected AsyncRouter setRequestHandler(RequestHandler handler) {
    this.handler = handler;
    return this;
  }

  public AsyncRouter start() {
    ctx = ZMQ.context(2);
    String queueChannel = "myInternalChannel" + UUID.randomUUID().toString();
    front = new AsyncRouterSocket(ctx, address, INPROC_ZMQ_ASYNC_BACKEND,
        vertx, queueChannel, (msg, messageResponder) -> {
            if (handler != null) {
              handler.handleRequest(msg, messageResponder);
            }
        });
    back = new QueueListeningPublishSocket(ctx, INPROC_ZMQ_ASYNC_BACKEND,
        vertx, queueChannel);
    run(front);
    return this;
  }

  public AsyncRouter stop() {
    if (back != null) {
      back.stop();
    }
    if (front != null) {
      front.stop();
    }
    if (ctx != null) {
      ctx.term();
    }
    return this;
  }

  private void run(Runnable runnable) {
    new Thread(runnable).start();
  }
}