package com.github.dano.zeromq.vertx;

import com.github.dano.zeromq.AsyncRouter;
import com.github.dano.zeromq.InMessage;
import com.github.dano.zeromq.MessageResponder;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.Message;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.eventbus.ReplyException;
import io.vertx.core.eventbus.ReplyFailure;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

/**
 * The ZeroMQ bridge.
 * TODO add more.
 */
public class ZeroMQBridge extends AsyncRouter {

  private final static Logger LOG = LoggerFactory.getLogger(ZeroMQBridge.class);
  public final static String REGISTER = "register:";
  private final static int REG_LENGTH = REGISTER.length();
  public final static String UNREGISTER = "unregister:";
  private final static int UNREG_LENGTH = UNREGISTER.length();

  private final Map<String, MessageConsumer<byte[]>> zmqHandlers = new HashMap<>();
  private final Set<byte[]> handlerSocketIds = new HashSet<>();
  private final Vertx vertx;
  private final long responseTimeout;

  /**
   * Create a ZeroMQBridge.
   *
   * @param address The address to listen on.
   * @param vertx The vertx instance.
   */
  public ZeroMQBridge(String address, Vertx vertx) {
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
  public ZeroMQBridge(String address, Vertx vertx, final long responseTimeout) {
    super(address);
    this.vertx = vertx;
    this.responseTimeout = responseTimeout;
  }

  @Override
  protected void handleRequest(InMessage inMessage, MessageResponder responder) {
    LOG.debug("Got msg {0}", inMessage);
    if (inMessage.isControl()) {
      handleCommand(inMessage, responder);
    } else {
      if (handlerSocketIds.contains(responder.getSocketId())) {
        vertx.eventBus().send(inMessage.getAddressAsString(), inMessage.getMsg());
      } else {
        DeliveryOptions options = new DeliveryOptions().setSendTimeout(responseTimeout);
        vertx.eventBus().<byte[]>send(inMessage.getAddressAsString(), inMessage.getMsg(), options,
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

  private void sendSuccessResponse(Message<byte[]> msg, MessageResponder responder) {
    String replyAddress = msg.replyAddress();
    if (replyAddress == null) {
      responder.respond(msg.body());
    } else {
      responder.respond(msg.body(), replyAddress.getBytes());
    }
  }

  private void sendFailureResponse(MessageResponder responder, AsyncResult<Message<byte[]>> event) {
    if (event.cause() instanceof ReplyException) {
      ReplyException ex = (ReplyException) event.cause();
      if (ReplyFailure.NO_HANDLERS.equals(ex.failureType())) {
        LOG.error("Send failed", event.cause());
        responder.respond(ex.failureType().name().getBytes());
      }
    }
  }

  /**
   * Handle a control message.
   *
   * @param message The control message.
   * @param responder The MessageResponder associated with the message.
   */
  private void handleCommand(InMessage message, final MessageResponder responder) {
    String command = new String(message.getMsg());
    if (command.startsWith(REGISTER)) {  // Register.
      final String address = command.substring(REG_LENGTH);
      LOG.debug("Registering handler at address {0}", address);
      unregister(address, responder.getSocketId());
      register(address, responder.getSocketId(), msg -> {
          if (msg.replyAddress() != null) {
            responder.respond(msg.body(), msg.replyAddress().getBytes());
          } else {
            responder.respond(msg.body());
          }
       });
    } else if (command.startsWith(UNREGISTER)) {  // Unregister
      String handler = command.substring(UNREG_LENGTH);
      unregister(handler, responder.getSocketId());
    } else {
      LOG.error("Got an unknown command: {0}", command);
    }
  }

  /**
   * Register a handler for a given channel.
   *
   * @param address The channel to associate with the handler.
   * @param socketId The socketId.
   * @param handler The handler.
   */
  private void register(String address, byte[] socketId, Handler<Message<byte[]>> handler) {
    handlerSocketIds.add(socketId);
    MessageConsumer<byte[]> consumer = vertx.eventBus().consumer(address, handler);
    zmqHandlers.put(address, consumer);
  }

  /**
   * Unregister a handler.
   *
   * @param address The handler to unregister
   * @param socketId THe socketID of the handler to unregister.
   */
  private void unregister(String address, byte[] socketId) {
    if (address != null && zmqHandlers.containsKey(address)) {
      zmqHandlers.get(address).unregister(result -> {
        zmqHandlers.remove(address);
        handlerSocketIds.remove(socketId);
        LOG.debug("Unregistered 0mq handler {0}", address);
      });
    }
  }
}
