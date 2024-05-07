package com.skysec.netty;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.DatagramPacket;

import java.nio.charset.StandardCharsets;


public class UdpServerHandler extends SimpleChannelInboundHandler<DatagramPacket> {

    @Override
    protected void channelRead0(ChannelHandlerContext ctx,
                                DatagramPacket msg) throws Exception {
        ByteBuf buf = msg.content();
        int len = buf.readableBytes();
        byte[] data = new byte[len];
        buf.readBytes(data);
        String receive = new String(data, StandardCharsets.UTF_8);

        String host = msg.sender().getAddress().getHostAddress();
        int port = msg.sender().getPort();
        System.out.printf("UdpServer 接收到来自 %s:%d 发送信息[长度=%d] %s%n", host, port, len, receive);
    }
}
