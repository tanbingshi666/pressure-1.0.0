package com.skysec.netty;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioDatagramChannel;
import lombok.Getter;

import java.net.InetSocketAddress;

public class UdpServer {

    private final InetSocketAddress bind;

    @Getter
    private Bootstrap bootstrap;
    private EventLoopGroup workerGroup;

    @Getter
    private Channel channel;

    public UdpServer(String hostname, int port) {
        if (hostname == null) {
            bind = new InetSocketAddress(port);
        } else {
            bind = new InetSocketAddress(hostname, port);
        }

        this.bootstrap = new Bootstrap();
        this.workerGroup = new NioEventLoopGroup();

        this.bootstrap
                .group(this.workerGroup)
                .channel(NioDatagramChannel.class)
                // 接受和发送缓存区大小
                .option(ChannelOption.SO_SNDBUF, 65535)
                .option(ChannelOption.SO_RCVBUF, 65535)
                // netty udp 默认接受最大包大小为 2048
                .option(ChannelOption.RCVBUF_ALLOCATOR, new FixedRecvByteBufAllocator(65535));

        getBootstrap().handler(new ChannelInitializer<NioDatagramChannel>() {
            @Override
            public void initChannel(NioDatagramChannel ch) {
                ch.pipeline().addLast(new UdpServerHandler());
            }
        });
    }

    public void startup() {

        ChannelFuture future;
        try {
            future = bootstrap.bind(bind).sync();
            if (future.isSuccess()) {
                this.channel = future.channel();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void shutdown() {
        try {
            this.workerGroup.shutdownGracefully();
            this.workerGroup = null;
            bootstrap = null;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
