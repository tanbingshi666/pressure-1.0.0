package com.skysec.sender;

import com.skysec.io.ReadFile;
import com.skysec.netty.UdpServer;
import com.skysec.queue.MessageQueue;
import com.skysec.queue.Termination;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.socket.DatagramPacket;

import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;

public class Sender {

    public static long count = 0L;

    private final String path;
    private final String remoteHost;
    private final int remotePort;
    private final ReadFile readFile;
    private final UdpServer udpServer;

    public Sender(String path, String remoteHost, int remotePort, UdpServer udpServer) {
        this.path = path;
        this.readFile = new ReadFile(path);
        this.remoteHost = remoteHost;
        this.remotePort = remotePort;
        this.udpServer = udpServer;
    }

    public void send() {
        // 异步读取文件到 MessageQueue
        CompletableFuture.runAsync(readFile::read);

        // 拉取 MessageQueue 输出到第三方 udp server
        Object obj;
        while (true) {
            try {
                obj = MessageQueue.take();
                if (obj instanceof Termination) {
                    udpServer.shutdown();
                    break;
                } else if (obj instanceof String) {
                    InetSocketAddress address = new InetSocketAddress(remoteHost, remotePort);
                    ByteBuf byteBuf = Unpooled.copiedBuffer(((String) obj).getBytes(StandardCharsets.UTF_8));
                    ChannelFuture sync = udpServer.getChannel().writeAndFlush(new DatagramPacket(byteBuf, address)).sync();
                    if (sync.isSuccess()) {
                        count++;
                    }
                }
            } catch (InterruptedException e) {
                // nothing to do
            }
        }

        System.out.println("成功发送数据量大小 " + count);
    }

}
