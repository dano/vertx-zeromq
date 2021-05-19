package com.github.dano.zeromq.verticle;

import org.zeromq.SocketType;
import org.zeromq.ZMQ;

/**
 * Created by Dean Pehrsson-Chapman Date: 05/11/2013
 */
public class EchoSocket implements Runnable {
  final String address, handlerName;

  public EchoSocket(String address, String handlerName) {
    this.address = address;
    this.handlerName = handlerName;
  }

  @Override
  public void run() {
    final ZMQ.Context ctx = ZMQ.context(1);
    final ZMQ.Socket registered = ctx.socket(SocketType.DEALER);
    registered.connect(address);
    registered.send(("register:" + handlerName).getBytes());
    System.out.println("registered");

    ZMQ.Poller poller = ctx.poller(1);
    poller.register(registered);
    int count = 0;
    while (listening) {
      poller.poll(1000);
      if (poller.pollin(0)) {
        byte[] response = registered.recv();
        byte[] replyaddress = registered.recv();
        registered.send(replyaddress, ZMQ.SNDMORE);
        registered.send(response, 0);
        count++;
      }
    }
    registered.close();
    ctx.term();
    System.out.print("Echo handler echoed " + count);
  }

  private volatile boolean listening = true;


  public void setListening(boolean listening) {
    this.listening = listening;
  }
}
