package com.github.dano.zeromq;

/**
 * TODO comment me
 */
public interface RequestHandler {

  void handleRequest(InMessage message, MessageResponder responder);

}
