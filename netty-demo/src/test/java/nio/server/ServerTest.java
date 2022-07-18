package nio.server;

import static utils.ByteBufferUtil.debugRead;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import lombok.extern.slf4j.Slf4j;


@Slf4j
public class ServerTest {
    @Test
    public void Server01() throws IOException {
        // 0. 使用bytebuffer接收数据
        ByteBuffer buffer = ByteBuffer.allocate(16);

        // 1. 创建服务器
        ServerSocketChannel ssc = ServerSocketChannel.open();

        // 2. 绑定监听端口
        ssc.bind(new InetSocketAddress(8999));

        // 3. 连接的集合
        List<SocketChannel> channelList = new ArrayList<>();
        while (true) {
            // 4. accept 建立与客户端连接， Socketchannel用来与客户之间进行通信
            SocketChannel sc = ssc.accept();        // accept是一个阻塞方法，此时线程阻塞了。

            log.debug("connected...{}", sc);
            channelList.add(sc);

            for (SocketChannel socketChannel : channelList) {
                // 5. 接收客户端端发送的数据
                log.debug("before read {}", socketChannel);
                socketChannel.read(buffer);     // 阻塞方法
                buffer.flip();
                debugRead(buffer);
                buffer.clear();
                log.debug("after read...{}", socketChannel);
            }

        }
    }

    @Test
    public void Server02() throws IOException {
        // 这个版本也不好，因为for循环一致在运行
        // 这个线程以及在跑 ，在 开发的时候，也不会这么用
        // 0. 使用bytebuffer接收数据
        ByteBuffer buffer = ByteBuffer.allocate(16);

        // 1. 创建服务器
        ServerSocketChannel ssc = ServerSocketChannel.open();

        ssc.configureBlocking(false);   // 设置成非阻塞模式

        // 2. 绑定监听端口
        ssc.bind(new InetSocketAddress(8999));

        // 3. 连接的集合
        List<SocketChannel> channelList = new ArrayList<>();
        while (true) {
            // 4. accept 建立与客户端连接， Socketchannel用来与客户之间进行通信
            SocketChannel sc = ssc.accept();        // accept是一个阻塞方法，此时线程阻塞了。
            // 在非阻塞模式下，上面的代码，在没有返回的时候，返回null

            if (sc != null) {
                log.debug("connected...{}", sc);
                sc.configureBlocking(false);    // 设置成非阻塞模式
                channelList.add(sc);
            }

            for (SocketChannel socketChannel : channelList) {
                // 5. 接收客户端端发送的数据
//                log.debug("before read {}", socketChannel);
                int read = socketChannel.read(buffer);// 非阻塞模式下，如果没有读到数据，返回0
                if (read > 0) {
                    buffer.flip();
                    debugRead(buffer);
                    buffer.clear();
//                    log.debug("after read...{}", socketChannel);
                }
            }

        }
    }
}
