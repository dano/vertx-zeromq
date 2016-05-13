/*
 * This is the confidential unpublished intellectual property of EMC Corporation,
 * and includes without limitation exclusive copyright and trade secret rights
 * of EMC throughout the world.
 */
package com.github.dano.zeromq.impl;

import com.github.dano.zeromq.BaseZeroMQBridge;
import com.github.dano.zeromq.InMessage;
import com.github.dano.zeromq.InMessageFactory;
import com.github.dano.zeromq.MessageResponder;
import com.github.dano.zeromq.OutMessageFactory;
import com.github.dano.zeromq.Payload;
import com.github.dano.zeromq.PayloadFactory;

import io.vertx.core.Vertx;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.Message;
import io.vertx.core.eventbus.MessageCodec;
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
  protected final PayloadFactory<?> payloadFactory;

  /**
   * Create a ZeroMQBridge.
   *
   * @param address The address to listen on.
   * @param vertx The vertx instance.
   */
  public ZeroMQBridgeImpl(String address, Vertx vertx) {
    this(address, vertx, 10000);
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
    this(address, vertx, responseTimeout, new PayloadFactoryImpl(), new PayloadImplMessageCodec(),
        new InMessageFactoryImpl(), new OutMessageFactoryImpl());
  }

  public <T> ZeroMQBridgeImpl(String address, Vertx vertx, final long responseTimeout,
                          PayloadFactory<T> payloadFactory, MessageCodec<T, T> codec,
                          InMessageFactory inMessageFactory, OutMessageFactory outMessageFactory) {
    super(vertx, address, inMessageFactory, outMessageFactory);
    this.payloadFactory = payloadFactory;
    this.responseTimeout = responseTimeout;
    vertx.eventBus().registerDefaultCodec(payloadFactory.getPayloadType(), codec);
  }

  @Override
  protected void handleUserMessage(InMessage inMessage, MessageResponder responder) {
    if (handlerSocketIds.contains(responder.getSocketId())) {
      LOG.info("Sending message to " + inMessage.getAddressAsString());
      vertx.eventBus().send(inMessage.getAddressAsString(), inMessage.getPayload());
    } else {
      DeliveryOptions options = new DeliveryOptions().setSendTimeout(responseTimeout);
      LOG.info("Sending message to " + inMessage.getAddressAsString());
      vertx.eventBus().<Payload>send(inMessage.getAddressAsString(),
          inMessage.getPayload(), options,
          event -> {
            if (event.succeeded()) {
              sendSuccessResponse(event.result(), responder);
            } else {
              sendFailureResponse(event.cause(), responder);
            }
          });
    }
  }

  /**
   * Send a successful response to a ZeroMQ client.
   *
   * @param msg The EventBus message containing the response payload.
   * @param responder The message responder.
   */
  private void sendSuccessResponse(Message<Payload> msg, MessageResponder responder) {
    String replyAddress = msg.replyAddress();
    if (replyAddress == null) {
      responder.respond(msg.body());
    } else {
      responder.respond(msg.body(), replyAddress.getBytes());
    }
  }

  /**
   * Send a failure response to a ZeroMQ socket.
   *
   * @param throwable The throwable containing the failure.
   * @param responder The message responder.
   */
  private void sendFailureResponse(Throwable throwable, MessageResponder responder) {
    LOG.error("Send failed", throwable);
    if (throwable instanceof ReplyException) {
      ReplyException ex = (ReplyException) throwable;
      responder.respond(payloadFactory.fromBytes(ex.failureType().name().getBytes()));
    } else {
      responder.respond(payloadFactory.fromBytes(
          ("Unknown error: " + throwable.getMessage()).getBytes()));
    }
  }
}
