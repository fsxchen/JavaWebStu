package nio.server;

import org.junit.Test;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringDecoder;

public class NettyServer {
    @Test
    public static void main(String[] args) {
        // 启动器
        new ServerBootstrap()
                // group，其实就是boss selector + worker selector
                .group(new NioEventLoopGroup())

                // 选择服务端socketIO的channel
                .channel(NioServerSocketChannel.class)

                // boss复杂处理连接，worker负责读写，worker也就是这里的child
                // 决定了worker【child】可以执行哪些handler
                .childHandler(new ChannelInitializer<NioSocketChannel>() {

                    // channel代表和客户端进行读写的通信channel，Initial代表初始化，
                    // 负责添加别的handler
                    @Override
                    protected void initChannel(NioSocketChannel ch) throws Exception {
                        // 两个具体的handler
                        // 1 转成字符串，将Bytebuf转成字符串
                        ch.pipeline().addLast(new StringDecoder());
                        ch.pipeline().addLast(new ChannelInboundHandlerAdapter() {
                            // 处理读事件
                            @Override
                            public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                                System.out.println(msg);
                            }
                        });
                    }
                })
                .bind(9090);

    }
}
