package nio.eventLoop;

import java.nio.charset.Charset;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.DefaultEventLoop;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class EventLoopServer {
//    public static void main(String[] args) {
//        new ServerBootstrap()
//                // 细化EventLoop，划分成boss和work,直接加上一个
//                .group(new NioEventLoopGroup(), new NioEventLoopGroup())
//                // 此时前面的负责连接，后面的负责处理事务
//                .channel(NioServerSocketChannel.class)
//                .childHandler(new ChannelInitializer<NioSocketChannel>() {
//                    @Override
//                    protected void initChannel(NioSocketChannel channel) throws Exception {
//                        channel.pipeline().addLast(new ChannelInboundHandlerAdapter() {
//                            @Override
//                            public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
//                                ByteBuf buf = (ByteBuf) msg;
//                                log.debug(buf.toString(Charset.defaultCharset()));
//                            }
//                        });
//                    }
//                })
//                .bind(9898);
//    }
    // 再次细分，创建一个独立的EventLoop，处理连接较长的事件
    public static void main(String[] args) {
        DefaultEventLoop group = new DefaultEventLoop();
        new ServerBootstrap()
                // 细化EventLoop，划分成boss和work,直接加上一个
                .group(new NioEventLoopGroup(), new NioEventLoopGroup())
                // 此时前面的负责连接，后面的负责处理事务
                .channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer<NioSocketChannel>() {
                    @Override
                    protected void initChannel(NioSocketChannel channel) throws Exception {
                        channel.pipeline().addLast(group, "group", new ChannelInboundHandlerAdapter() {
                            @Override
                            public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                                ByteBuf buf = (ByteBuf) msg;
                                log.debug(buf.toString(Charset.defaultCharset()));
                            }
                        });
                    }
                })
                .bind(9898);
    }
}
