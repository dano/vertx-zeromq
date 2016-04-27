/*
 * This is the confidential unpublished intellectual property of EMC Corporation,
 * and includes without limitation exclusive copyright and trade secret rights
 * of EMC throughout the world.
 */
package com.github.dano.zeromq;

/**
 * TODO comment me
 */
public class PayloadImpl implements Payload {

  private final byte[] msg;

  public PayloadImpl(byte[] msg)  {
    this.msg = msg;
  }

  public byte[] getMsg() {
    return msg;
  }
}
