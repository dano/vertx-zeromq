/*
 * This is the confidential unpublished intellectual property of EMC Corporation,
 * and includes without limitation exclusive copyright and trade secret rights
 * of EMC throughout the world.
 */
package com.github.dano.zeromq;

import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.Message;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * An abstract class that provides event bus registration/unregistration
 * mechanisms for ZeroMQ sockets.
 */
public abstract class BaseZeroMQBridge extends AsyncRouter {

  private final static Logger LOG = LoggerFactory.getLogger(BaseZeroMQBridge.class);

  public final static String REGISTER = "register:";
  private final static int REG_LENGTH = REGISTER.length();
  public final static String UNREGISTER = "unregister:";
  private final static int UNREG_LENGTH = UNREGISTER.length();

  protected final Map<String, MessageConsumer<Payload>> zmqHandlers = new HashMap<>();
  protected final Set<byte[]> handlerSocketIds = new HashSet<>();
  protected final Vertx vertx;

  public BaseZeroMQBridge(Vertx vertx, String address,
      InMessageFactory inMessageFactory, OutMessageFactory outMessageFactory) {
    super(address, inMessageFactory, outMessageFactory);
    this.vertx = vertx;
  }

  /**
   * Handle a control message.
   *
   * @param message The control message.
   * @param responder The MessageResponder associated with the message.
   */
  protected void handleControlMessage(InMessage message, final MessageResponder responder) {
    String command = new String(message.getControlMessage());
    if (command.startsWith(REGISTER)) {  // Register.
      final String address = command.substring(REG_LENGTH);
      LOG.debug("Registering handler at address {0}", address);
      unregister(address, responder.getSocketId());
      register(address, responder.getSocketId(), msg -> {
        LOG.info("Listener on " + address + " got msg " + msg.body());
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
  private void register(String address, byte[] socketId, Handler<Message<Payload>> handler) {
    handlerSocketIds.add(socketId);
    MessageConsumer<Payload> consumer = vertx.eventBus().consumer(address, handler);
    zmqHandlers.put(address, consumer);
  }

  /**
   * Unregister a handler.
   *
   * @param address The handler to unregister
   * @param socketId The socketID of the handler to unregister.
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
