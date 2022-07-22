package nio.client;

import java.net.InetSocketAddress;

import org.junit.Test;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringEncoder;

public class HelloClient {
    @Test
    public static void main(String[] args) throws InterruptedException {
        // 启动类
        new Bootstrap()
                // 添加EventGroup
                .group(new NioEventLoopGroup())
                // 选择channel的实现
                .channel(NioSocketChannel.class)
                // 添加处理器
                .handler(new ChannelInitializer<NioSocketChannel>() {
                    @Override
                    protected void initChannel(NioSocketChannel channel) throws Exception {
                        // 发送的时候，进行编码
                        channel.pipeline().addLast(new StringEncoder());
                    }
                })
                // 5.连接到服务器
                .connect(new InetSocketAddress("localhost", 9090))
                .sync()
                .channel()
                .writeAndFlush("Hello World");

    }
}
