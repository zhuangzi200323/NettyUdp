package com.jack.udpstudy.udpserver;

import com.jack.udpstudy.listener.UdpMsgListener;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.DatagramPacket;
import io.netty.util.CharsetUtil;

class UdpServerHandler extends SimpleChannelInboundHandler<DatagramPacket> {
    UdpMsgListener udpMsgListener;

    public UdpServerHandler(UdpMsgListener udpMsgListener) {
        this.udpMsgListener = udpMsgListener;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, DatagramPacket msg) throws Exception {
        if(udpMsgListener != null) {
            udpMsgListener.onReceivedMsg(ctx, msg);
        }
    }
}
