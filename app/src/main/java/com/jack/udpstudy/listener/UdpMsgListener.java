package com.jack.udpstudy.listener;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.socket.DatagramPacket;

public interface UdpMsgListener {

    void onReceivedMsg(ChannelHandlerContext ctx, DatagramPacket msg);
}
