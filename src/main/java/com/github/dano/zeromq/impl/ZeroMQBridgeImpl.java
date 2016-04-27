/*
 * This is the confidential unpublished intellectual property of EMC Corporation,
 * and includes without limitation exclusive copyright and trade secret rights
 * of EMC throughout the world.
 */
package com.github.dano.zeromq.impl;

import com.github.dano.zeromq.InMessage;
import com.github.dano.zeromq.MessageResponder;
import com.github.dano.zeromq.Payload;
import com.github.dano.zeromq.BaseZeroMQBridge;
import io.vertx.core.AsyncResult;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.Message;
import io.vertx.core.eventbus.ReplyException;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

/**
 * The ZeroMQ bridge.
 *
 * Once started, the bridge forwards ZeroMQ requests to the Vert.x
 * Event Bus, and sends responses back to the requester. It also
 * allows ZMQ Sockets to act as Vert.x Event Bus listeners.
 */
public class ZeroMQBridgeImpl extends BaseZeroMQBridge {

  private final static Logger LOG = LoggerFactory.getLogger(ZeroMQBridgeImpl.class);

  private final long responseTimeout;

  /**
   * Create a ZeroMQBridge.
   *
   * @param address The address to listen on.
   * @param vertx The vertx instance.
   */
  public ZeroMQBridgeImpl(String address, Vertx vertx) {
    this(address, vertx, 5000);
  }

  /**
   * Create a ZeroMQBridge
   *
   * @param address The address to listen for requests on.
   * @param vertx The vertx instance.
   * @param responseTimeout The response timeout. This is how long to
   *                        wait for a reply from the event bus listener
   *                        for a given request.
   */
  public ZeroMQBridgeImpl(String address, Vertx vertx, final long responseTimeout) {
    super(vertx, address, new InMessageFactoryImpl(), new OutMessageFactoryImpl());
    this.responseTimeout = responseTimeout;
    vertx.eventBus().registerCodec(new PayloadImplMessageCodec());
  }

  @Override
  protected void handleRequest(InMessage inMessage, MessageResponder responder) {
    LOG.debug("Got msg {0}", inMessage);
    if (inMessage.isControl()) {
      handleControlMessage(inMessage, responder);
    } else {
      DeliveryOptions options = new DeliveryOptions().setCodecName("payloadImpl");
      if (handlerSocketIds.contains(responder.getSocketId())) {
        LOG.info("Sending message to " + inMessage.getAddressAsString());
        vertx.eventBus().send(inMessage.getAddressAsString(), inMessage.getPayload(),
            options);
      } else {
        options.setSendTimeout(responseTimeout);
        LOG.info("Sending message to " + inMessage.getAddressAsString());
        vertx.eventBus().<Payload>send(inMessage.getAddressAsString(),
            inMessage.getPayload(), options,
            event -> {
              if (event.succeeded()) {
                sendSuccessResponse(event.result(), responder);
              } else {
                sendFailureResponse(responder, event);
              }
            });
      }
    }
  }

  private void sendSuccessResponse(Message<Payload> msg, MessageResponder responder) {
    String replyAddress = msg.replyAddress();
    if (replyAddress == null) {
      responder.respond(msg.body());
    } else {
      responder.respond(msg.body(), replyAddress.getBytes());
    }
  }

  private void sendFailureResponse(MessageResponder responder,
      AsyncResult<Message<Payload>> event) {
    LOG.error("Send failed", event.cause());
    if (event.cause() instanceof ReplyException) {
      ReplyException ex = (ReplyException) event.cause();
      responder.respond(new PayloadImpl(ex.failureType().name().getBytes()));
    } else {
      responder.respond(new PayloadImpl(
          ("Unknown error: " + event.cause().getMessage()).getBytes()));
    }
  }
}
