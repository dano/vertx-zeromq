vertx-zeromq
===========

[![Build Status](https://travis-ci.org/dano/vertx-zeromq.svg?branch=master)](https://travis-ci.org/dano/vertx-zeromq)

A ZeroMQ Event Bus bridge for Vert.x. This project started as a Vert.x 3.x port of Dean Pehrsson-Chapman's Vert.x 2.x-only [vert-zeromq project](https://github.com/p14n/vert-zeromq), though the code has since been refactored significantly.

The ZeroMQ bridge enables you to remotely call a handler on the Vert.x Event Bus using a ZeroMQ socket, receive replies, and reply back. It also allows you to register a ZeroMQ socket as a Vert.x Event Bus handler, receive calls to that Event Bus address on the ZeroMQ socket, and reply back.

If a reply handler was supplied by the sender of the message received at the socket, that handler's address is included as the second frame of the message.

*NOTE: you cannot currently reply to a ZeroMQ socket that has registered as a handler (although it can reply to you).*

The Bridge is started by deploying the `ZeroMQBridgeVerticle`. The verticle requires the address the module should listen on as part of its `DeploymentOptions` config:

```json
{
 "address":"tcp://*:5558"
}
```

* Send a message to an event bus handler by sending the handler address as the first frame, the message as the second.
* Reply to a handler by using its address (provided in the second frame of the message).
* Register a ZeroMQ handler by sending a single message 'register:myHandlerName'
* Unregister a ZeroMQ handler by sending a single message 'unregister:myHandlerName'.

### Installing via Maven
```xml
<dependency>
  <groupId>com.github.dano</groupId>
  <artifactId>vertx-zeromq</artifactId>
  <version>1.0.1</version>
</dependency>
```

### Calling an event bus handler

```java
ZMQ.Context ctx = ZMQ.context(1);

ZMQ.Socket client = ctx.socket(ZMQ.DEALER);
client.connect("tcp://localhost:5558");
client.send("echoHandler".getBytes(), ZMQ.SNDMORE); //Send the handler address
client.send("hello".getBytes(), 0); //Send the message

byte[] response = client.recv(); //Get the response
Assert.assertEquals("hello", new String(response));
```
### Registering a socket as a handler, sending to it and receiving a reply

```java
String address = "tcp://localhost:5558";
ZMQ.Context ctx = ZMQ.context(1);
final String msg = "message 1";
final String msg2 = "message 2";
final String channel = "testHandler";

// Register a ZMQ socket as a handler for an Event Bus address
ZMQ.Socket registered = ctx.socket(ZMQ.DEALER); // This will be our handler
registered.connect(address);
registered.send(("register:" + channel).getBytes()); // Register this socket as EB channel 'testHandler'

Thread.sleep(100); // Messages sent immediately could be dropped in the bus

// Use another Socker to send a message to the ZeroMQ Bridge, 
// which will forward it to the handler we registered above.
ZMQ.Socket client = ctx.socket(ZMQ.DEALER);
client.connect(address);
client.send(channel.getBytes(), ZMQ.SNDMORE); // Send the handler address
client.send(msg.getBytes(), 0); //Send the message

// Get the message sent by the client, which was forwared to us
// by the Bridge. The second frame contains an address we can use
// to reply to the client.
byte[] response = registered.recv();
byte[] replyaddress = registered.recv();
assertEquals(msg, new String(response));

// Send Reply to the client, via the Bridge.
registered.send(replyaddress, ZMQ.SNDMORE); // Send the address of the client socket
registered.send(msg2.getBytes(), 0); // Send a reply message

// Get the reply sent to the client socket.
byte[] response3 = client.recv(); // Get the response sent to the client by the handler socket
assertEquals(msg2, new String(response3));

```

### Receiving a message sent from a ZeroMQ client

Messages sent into the bridge from ZeroMQ clients will be turned into instances of the `Payload` interface. `vertx-zeromq` includes a default implementation, which simply stores the raw message as a `byte[]`, accessible via `PayloadImpl.getMsg()`.

So, if a ZeroMQ client wants to send a message, and then expect to receive a response:
```java
client.send("testChannel".getBytes(), ZMQ.SNDMORE);
client.send("a message".getBytes(), 0);
byte[] reply = client.recv(0); // This will contain "some reply" as bytes.
```

The `EventBus` consumer to receive this messaage and send the response would look like this:
```java
public class ReceivingVerticle extends AbstractVerticle {

  @Override
  public void start() {
    vertx.eventBus().<PayloadImpl>consumer("testChannel", msg -> {
      PayloadImpl payload = msg.body();
      String msg = new String(payload.getMsg()); // msg == "a message"
      msg.reply(new PayloadImpl("some reply".getBytes()));
    });
  }
}
```

### Customizing the Message format
Note that the format of the ZeroMQ message the EventBus bridge expects to receive, and the format of the response it sends to ZeroMQ clients, can be customized (though the process is admittedly a bit messy). To do it, write custom implementations of the following interfaces: `InMessage`, `InMessageFactory`, `OutMessage`, `OutMessageFactory`, `Payload`, and `PayloadFactory`. Additionally, implement a Vert.x `MessageCodec` that can serialize the custom `Payload` class. You'll then need to implement your own concrete implementation of `AbstractZeroMQBridgeVerticle`. To do this, you just need to implement `getBridgeImpl`, and use it to create a `ZeroMQBridgeImpl` that takes all your custom implementations:

```java

public class MyZeroMQBridgeVerticle extends AbstractZeroMQBridgeVerticle {

  @Override
  protected ZeroMQBridge getBridgeImpl(String address, long timeout) {
    return new ZeroMQBridgeImpl(address, vertx, timeout, new MyPayloadFactory(),
        new MyPayloadMessageCodec(), new MyInMessageFactory(), new MyOutMessageFactory());
  }
}
```

---
For more on ZeroMQ, see the excellent guide at <http://zguide.zeromq.org/page:all>

