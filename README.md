vertx-zeromq
===========

A ZeroMQ Event Bus bridge for Vert.x. This project is a Vert.x 3.x port of Dean Pehrsson-Chapman's [vert-zeromq project](https://github.com/p14n/vert-zeromq), which is Vert.x 2.x only. 

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
---
For more on ZeroMQ, see the excellent guide at <http://zguide.zeromq.org/page:all>

