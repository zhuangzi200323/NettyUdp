package com.jack.udpstudy.udpserver;

import android.text.TextUtils;
import android.util.Log;

import com.jack.udpstudy.listener.UdpMsgListener;

import java.net.InetSocketAddress;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.socket.DatagramPacket;
import io.netty.util.CharsetUtil;


public class UdpServerMgr {
    private static final String TAG = UdpServerMgr.class.getSimpleName();

    public static final int SERVER_PORT = 6666;
    public static final int CLIENT_PORT = 7777;

    private static volatile UdpServerMgr instance;
    UdpServer server;
    Thread serverThread;

    volatile boolean isCtrlStop;
    private int serverPort = SERVER_PORT;
    private int dstServerPort = CLIENT_PORT;

    private UdpMsgListener innerUdpServerListener = new UdpMsgListener() {
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

    public static UdpServerMgr getInstance() {
        if (instance == null) {
            synchronized (UdpServerMgr.class) {
                if (instance == null) {
                    instance = new UdpServerMgr();
                }
            }
        }
        return instance;
    }

    public synchronized boolean isRun() {
        return serverThread != null && serverThread.isAlive();
    }

    public void start() {
        if(isRun()) {
            return;
        }

        //前次停止之后设置为false，重新start时设置为true
        isCtrlStop = false;

        serverThread = new Thread() {
            @Override
            public void run() {
                doWork();
            }
        };
        serverThread.start();

        Log.d(TAG, "start ok");
    }

    private void doWork() {
        try {
            server = new UdpServer();
            server.startServer(serverPort, innerUdpServerListener);
        } catch (Throwable e) {
            e.printStackTrace();
        }
        Log.d(TAG, "doWork end");
    }

    public void stop() {
        if(server != null) {
            server.stopServer();
        }

        if(serverThread != null) {
            serverThread.interrupt();
            serverThread = null;
        }

    }

    public void sendData(ChannelHandlerContext ctx, DatagramPacket msg, String info) {
        if (!TextUtils.isEmpty(info)) {
            ctx.writeAndFlush(new DatagramPacket(
                    Unpooled.copiedBuffer(info, CharsetUtil.UTF_8),
                    msg.sender()));
        }
    }
}
