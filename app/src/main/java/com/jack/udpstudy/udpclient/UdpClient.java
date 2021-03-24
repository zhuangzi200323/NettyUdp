package com.jack.udpstudy.udpclient;

import android.util.Log;

import com.jack.udpstudy.listener.UdpMsgListener;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.DatagramPacket;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.util.CharsetUtil;

public class UdpClient {
    private static final String TAG = UdpClient.class.getSimpleName();
    Channel ch;
    EventLoopGroup group;

    public void start(UdpMsgListener udpMsgListener, int port) {
        try {
            group = new NioEventLoopGroup();
        } catch (Throwable e){
            e.printStackTrace();
        }
        try {
            Bootstrap b = new Bootstrap();
            b.group(group)
                    .channel(NioDatagramChannel.class)
                    .option(ChannelOption.SO_BROADCAST, true)
                    .option(ChannelOption.SO_REUSEADDR, true)
                    .handler(new ClientHandler(udpMsgListener));

            ch = b.bind(port).sync().channel();
            ch.closeFuture().await();
        } catch (Exception e) {
            Log.e(TAG, "", e);
        } finally {
            group.shutdownGracefully();
        }
    }

    public Channel getChannel() {
        return ch;
    }

    public void stop() {
        if (group != null) {
            group.shutdownGracefully();
        }
    }

    private static class ClientHandler extends SimpleChannelInboundHandler<DatagramPacket> {
        UdpMsgListener udpMsgListener;
        public ClientHandler(UdpMsgListener l) {
            udpMsgListener = l;
        }

        @Override
        protected void channelRead0(ChannelHandlerContext ctx, DatagramPacket msg) throws Exception {
            if (udpMsgListener != null){
                udpMsgListener.onReceivedMsg(ctx, msg);
            }
        }
    }
}
