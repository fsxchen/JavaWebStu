package nio.eventLoop;

import java.net.InetSocketAddress;
import java.util.Scanner;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringEncoder;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ReadAndWriteClient {
    public static void main(String[] args) throws InterruptedException {
        NioEventLoopGroup group = new NioEventLoopGroup();
        // connect 返回的是channelFuture，Java中Future、Promise都是异步的
        ChannelFuture channelFuture = new Bootstrap()
                .group(group)
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<NioSocketChannel>() {
                    @Override
                    protected void initChannel(NioSocketChannel channel) throws Exception {
                        channel.pipeline().addLast(new StringEncoder());
                    }
                })
                // connect 是一个异步非阻塞的
                // mian线程发出调用，真正执行的是NioEvent启动的线程
                .connect(new InetSocketAddress("localhost", 9898));
        // 同步版本
        channelFuture.sync();
        Channel channel = channelFuture.channel();

        new Thread(()-> {
            Scanner scanner = new Scanner(System.in);
            while (true) {
                String line = scanner.nextLine();
                if ("q".equals(line)) {
                    channel.close();
                    break;
                }
                channel.writeAndFlush(line);
            }
        }).start();

        // 获取closeFuture对象，可以同步，也可以异步操作
        ChannelFuture closeFuture = channel.closeFuture();
        // 1. 同步处理
//        closeFuture.sync();
//        log.debug("close");
        //2. 异步处理
        closeFuture.addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture channelFuture) throws Exception {
                log.debug("complete");
                group.shutdownGracefully();
            }
        });

    }
}
