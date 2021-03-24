package com.jack.udpstudy;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;

import com.jack.udpstudy.databinding.ActivityMainBinding;
import com.jack.udpstudy.listener.UdpMsgListener;
import com.jack.udpstudy.udpclient.UdpClientMgr;
import com.jack.udpstudy.udpserver.UdpServerMgr;
import com.jack.udpstudy.utils.NetUtils;

import java.lang.ref.WeakReference;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.socket.DatagramPacket;
import io.netty.util.CharsetUtil;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();
    private static ActivityMainBinding uiBinding;
    private static String broadCastIp = "";
    private ChannelHandlerContext channelHandlerContext;
    private DatagramPacket datagramPacket;

    private static MyHandler handler;
    public static final int SEND_UDP_CLIENT_DATA = 1;
    public static final int RECV_UDP_CLIENT_DATA = 2;
    public static final int RECV_UDP_SEND_DATA = 3;

    static class MyHandler extends Handler {
        @Override
        public void handleMessage(@NonNull Message msg){
            switch (msg.what){
                case SEND_UDP_CLIENT_DATA:
                    String broadMsg = broadCastIp + "." + System.currentTimeMillis();
                    uiBinding.clientSendTv.setText("send data: " + broadMsg);
                    UdpClientMgr.getInstance().sendBoradcastMsg(broadCastIp, broadMsg);
                    break;
                case RECV_UDP_CLIENT_DATA:
                    uiBinding.clientReceiveTv.setText("recv data: " + msg.obj);
                    break;
                case RECV_UDP_SEND_DATA:
                    uiBinding.serverReceiveTv.setText("recv data: " + msg.obj);
                    break;
                default:
                    break;
            }
        }
    }

    UdpMsgListener clientUdpMsgListener = new UdpMsgListener() {
        @Override
        public void onReceivedMsg(ChannelHandlerContext ctx, DatagramPacket msg) {
            channelHandlerContext = ctx;
            datagramPacket = msg;
            String strMsg = msg.content().toString(CharsetUtil.UTF_8);
            Log.e(TAG, strMsg);
            Message message = Message.obtain();
            message.what = RECV_UDP_CLIENT_DATA;
            message.obj = msg.content().toString(CharsetUtil.UTF_8);
            handler.sendMessage(message);
        }
    };

    UdpMsgListener serverUdpMsgListener = new UdpMsgListener() {
        @Override
        public void onReceivedMsg(ChannelHandlerContext ctx, DatagramPacket msg) {
            String strMsg = msg.content().toString(CharsetUtil.UTF_8);
            Log.e(TAG, strMsg);
            Message message = Message.obtain();
            message.what = RECV_UDP_SEND_DATA;
            message.obj = msg.content().toString(CharsetUtil.UTF_8);
            handler.sendMessage(message);
            String sendData = "Hello, I'm from server, my time: " + System.currentTimeMillis();
            UdpServerMgr.getInstance().sendData(ctx, msg, sendData);
            runOnUiThread(()->{
                uiBinding.serverSendTv.setText("send data: " + sendData);
            });
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        handler = new MyHandler();
        uiBinding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        broadCastIp = NetUtils.getBroadcastIp(this);
    }

    public void startUdpClient(View view) {
        UdpClientMgr.getInstance().setUiUdpMsgListener(clientUdpMsgListener);
        UdpClientMgr.getInstance().start();
    }

    public void sendUdpClientBroadcast(View view) {
        handler.removeMessages(SEND_UDP_CLIENT_DATA);
        handler.sendEmptyMessageDelayed(SEND_UDP_CLIENT_DATA, 1000);
    }

    public void stopUdpClient(View view) {
        handler.removeMessages(SEND_UDP_CLIENT_DATA);
        UdpClientMgr.getInstance().stop();
    }

    public void startUdpServer(View view) {
        UdpServerMgr.getInstance().setUiUdpMsgListener(serverUdpMsgListener);
        UdpServerMgr.getInstance().start();
    }

    public void stopUdpServer(View view) {
        UdpServerMgr.getInstance().stop();
    }
}