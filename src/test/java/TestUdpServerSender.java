import com.skysec.netty.UdpServerHandler;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.DatagramPacket;
import io.netty.channel.socket.nio.NioDatagramChannel;

import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class TestUdpServerSender {

    public static void main(String[] args) {
        Bootstrap bootstrap = new Bootstrap();
        EventLoopGroup workGroup = new NioEventLoopGroup();
        bootstrap.group(workGroup).channel(NioDatagramChannel.class)
                .option(ChannelOption.SO_BROADCAST, true)
                .option(ChannelOption.SO_SNDBUF, 65535)
                .option(ChannelOption.SO_RCVBUF, 65535)
                .option(ChannelOption.RCVBUF_ALLOCATOR, new FixedRecvByteBufAllocator(65535))
                .handler(new ChannelInitializer<NioDatagramChannel>() {

                    @Override
                    protected void initChannel(NioDatagramChannel ch) throws Exception {
                        // TODO Auto-generated method stub
                        ch.pipeline().addLast(new UdpServerHandler());
                    }
                });
        try {
            Channel channel = bootstrap.bind(10001).sync().channel();
            InetSocketAddress address = new InetSocketAddress("192.168.2.244", 5141);
            // byte[] bytes = new byte[61440];
            // Arrays.fill(bytes, (byte) 1);
            ByteBuf byteBuf = Unpooled.copiedBuffer("hello world udp".getBytes(StandardCharsets.UTF_8));
            // ByteBuf byteBuf = Unpooled.copiedBuffer(bytes);
            ChannelFuture sync = null;
            try {
                sync = channel.writeAndFlush(new DatagramPacket(byteBuf, address)).sync();
                if (sync.isSuccess()) {
                    System.out.println("send data success");
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            channel.closeFuture().sync().await();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
