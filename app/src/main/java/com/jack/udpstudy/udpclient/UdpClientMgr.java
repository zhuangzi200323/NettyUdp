package com.jack.udpstudy.udpclient;

import android.util.Log;

import com.jack.udpstudy.listener.UdpMsgListener;

import java.net.InetSocketAddress;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.socket.DatagramPacket;
import io.netty.util.CharsetUtil;


public class UdpClientMgr {
    private static final String TAG = UdpClientMgr.class.getSimpleName();
    private static final int UDP_SERVER_PORT = 6666;
    private static final int UDP_CLIENT_PORT = 7777;
    private int serverPort = UDP_SERVER_PORT;
    private int clientPort = UDP_CLIENT_PORT;
    private static UdpClientMgr instance;

    private UdpClient server;
    private Thread workThread;

    public static UdpClientMgr getInstance() {
        if (instance == null) {
            synchronized (UdpClientMgr.class) {
                if (instance == null) {
                    instance = new UdpClientMgr();
                }
            }
        }
        return instance;
    }

    UdpMsgListener innerUdpMsgListener = new UdpMsgListener() {
        @Override
        public void onReceivedMsg(ChannelHandlerContext ctx, DatagramPacket msg) {
            String strMsg = msg.content().toString(CharsetUtil.UTF_8);
            Log.e(TAG, strMsg);
            if (uiUdpMsgListener != null){
                uiUdpMsgListener.onReceivedMsg(ctx, msg);
            }
        }
    };

    UdpMsgListener uiUdpMsgListener;

    public void setUiUdpMsgListener(UdpMsgListener uiUdpMsgListener) {
        this.uiUdpMsgListener = uiUdpMsgListener;
    }

    public synchronized boolean isRun() {
        return workThread != null && workThread.isAlive();
    }

    public void start() {
        if(isRun()) {
            return;
        }

        synchronized (this) {
            workThread = new Thread() {
                @Override
                public void run() {
                    doWork();
                }
            };
            workThread.start();
        }
    }

    private void doWork() {
        try {
            server = new UdpClient();
            server.start(innerUdpMsgListener, clientPort);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    public void stop() {
        if(server != null) {
            server.stop();
        }

        synchronized (this) {
            if (workThread != null) {
                workThread.interrupt();
                workThread = null;
            }
        }
    }

    public boolean sendBoradcastMsg(String dstAddr, String msg) {
        if (server != null && server.getChannel() != null) {
            if(dstAddr == null || dstAddr.isEmpty()) {
                dstAddr = "255.255.255.255";
            }

            System.out.println("#### " + msg);
            server.ch.writeAndFlush(new DatagramPacket(
                    Unpooled.copiedBuffer(msg, CharsetUtil.UTF_8),
                    new InetSocketAddress(dstAddr, serverPort)));
            return true;
        }

        return false;
    }
}
