package nio.eventLoop;

import java.net.InetSocketAddress;

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
public class EventLoopClient {
//    public static void main(String[] args) throws InterruptedException {
//        Channel channel = new Bootstrap()
//                .group(new NioEventLoopGroup())
//                .channel(NioSocketChannel.class)
//                .handler(new ChannelInitializer<NioSocketChannel>() {
//                    @Override
//                    protected void initChannel(NioSocketChannel channel) throws Exception {
//                        channel.pipeline().addLast(new StringEncoder());
//                    }
//                })
//                // connect 是一个异步非阻塞的
//                // mian线程发出调用，真正执行的是NioEvent启动的线程
//                .connect(new InetSocketAddress("localhost", 9898))
//
//                .sync()
//                // 如果没有sync，会直接向下执行。但是此时的channel是空的
//                .channel();
//        System.out.println(channel);
//        System.out.println("...");
//    }

    public static void main(String[] args) throws InterruptedException {
        // connect 返回的是channelFuture，Java中Future、Promise都是异步的
        ChannelFuture channelFuture = new Bootstrap()
                .group(new NioEventLoopGroup())
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

//        channelFuture.sync();
        // 方法1. 使用sync同步处理结果
//        channelFuture.sync()      // 阻塞当前线程，知道Nio线程连接建立完毕，此时就会阻塞
                                        // 和同步类似
//        Channel channel = channelFuture.channel();
//                // 如果没有sync，会直接向下执行。但是此时的channel是空的
//        System.out.println(channel);
//        System.out.println("...");
        // 方法2. 异步处理结果,使用addListener 添加一个回调对象。
        channelFuture.addListener(new ChannelFutureListener() {
            // 连接建立好了以后会调用的
            @Override
            public void operationComplete(ChannelFuture channelFuture) throws Exception {
                Channel channel = channelFuture.channel();
                log.debug("{}", channel);
            }
        });

    }
}
