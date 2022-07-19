package nio.server;

import static utils.ByteBufferUtil.debugRead;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.junit.Test;

import lombok.extern.slf4j.Slf4j;


@Slf4j
public class ServerTest {
    public static void split(ByteBuffer source) {
        source.flip();
        for(int i=0; i<source.position(); i++) {
            int length = i - source.position() + 1;
            if (source.get(i) == '\n') {
                ByteBuffer target = ByteBuffer.allocate(length);
                for (int j=0; j < length; j++) {
                    target.put(source.get());
                }
                debugRead(target);
            }
        }
        source.compact();
    }
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

    @Test
    public void Server03() throws IOException {
        // select 版本的

        // 1. 创建Selector,管理多个channel

        Selector selector = Selector.open();

        ByteBuffer buffer = ByteBuffer.allocate(16);
        // 1. 创建服务器
        ServerSocketChannel ssc = ServerSocketChannel.open();
        ssc.configureBlocking(false);   // 设置成非阻塞模式

        // 2. 建立selector和channel的联系，也就是注册的过程
        // SelectionKey：事件发生时，通过selectionKey可以得到事件和chanel
        SelectionKey sscKey = ssc.register(selector, 0, null);
        log.debug("register key {}", sscKey);
        // 但是处理的事情有多种类型：accept/connect/read/write
        // accept： 连接请求的时候，触发accept事件
        // connect： 客户端连接建立起来的时候，的事件
        // read： 可读的时候触发
        // write： 可写的时候触发
        sscKey.interestOps(SelectionKey.OP_ACCEPT);     // 关注accept事件

        ssc.bind(new InetSocketAddress(8999));

        while (true) {
            // 3. 阻塞
            selector.select();      // select 是阻塞的，如果没有事情发生，就阻塞
            // 4. 处理事件, selectedKeys是一个集合，包含了所有的发生的事件
            Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
            while (iterator.hasNext()) {
                SelectionKey key = iterator.next();
                log.debug("key {}", key);
                ServerSocketChannel channel = (ServerSocketChannel) key.channel();
                SocketChannel sc = channel.accept();
                log.debug("server socket {}", sc);
            }

        }
    }

    @Test
    public void Server04() throws IOException {
        // select 版本的, 处理读取操作

        // 1. 创建Selector,管理多个channel

        Selector selector = Selector.open();

        ByteBuffer buffer = ByteBuffer.allocate(16);
        // 1. 创建服务器
        ServerSocketChannel ssc = ServerSocketChannel.open();
        ssc.configureBlocking(false);   // 设置成非阻塞模式

        // 2. 建立selector和channel的联系，也就是注册的过程
        // SelectionKey：事件发生时，通过selectionKey可以得到事件和chanel
        SelectionKey sscKey = ssc.register(selector, 0, null);
        log.debug("register key {}", sscKey);
        // 但是处理的事情有多种类型：accept/connect/read/write
        // accept： 连接请求的时候，触发accept事件
        // connect： 客户端连接建立起来的时候，的事件
        // read： 可读的时候触发
        // write： 可写的时候触发
        sscKey.interestOps(SelectionKey.OP_ACCEPT);     // 关注accept事件

        ssc.bind(new InetSocketAddress(8999));

        while (true) {
            // 3. 阻塞
            selector.select();      // select 是阻塞的，如果没有事情发生，就阻塞
            // 4. 处理事件, selectedKeys是一个集合，包含了所有的发生的事件
            Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
            while (iterator.hasNext()) {
                // 此时，事件可能是accept，也可能是readable
                SelectionKey key = iterator.next();
                log.debug("key {}", key);
                // 因为我们的select会给我们维护这个 selectedKey,只会往里面加，不会删，因此我们自己处理
                // 完了只会，需要删除掉
                iterator.remove();
                if (key.isAcceptable()) {
                    ServerSocketChannel channel = (ServerSocketChannel) key.channel();
                    SocketChannel sc = channel.accept();
                    // 这里的是读写的socket
                    sc.configureBlocking(false);    // 这里出现了一个空指针
                    // 通用需要把它放到selector中处理
                    SelectionKey scKey = sc.register(selector, 0, null);
                    //                log.debug("server socket {}", sc);
                    scKey.interestOps(SelectionKey.OP_READ);
                    // 处理完了sscKey
                    log.debug("sc {}", sc);
                } else if (key.isReadable()) {
                    // 读取数据逻辑
                    try {
                        SocketChannel channel = (SocketChannel) key.channel();
                        ByteBuffer bu = ByteBuffer.allocate(16);
                        int read = channel.read(bu);
                        // 正常断开，返回值位-1
                        // 0 正常断开
                        if (read == -1) {
                            key.cancel();
                        } else {
                            bu.flip();
                            debugRead(bu);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                        key.cancel();  // 从 selector集合中删除了
                    }
                }


            }

        }
    }

    @Test
    public void Server05() throws IOException {
        // select 版本的, 处理读取操作

        // 1. 创建Selector,管理多个channel

        Selector selector = Selector.open();

        ByteBuffer buffer = ByteBuffer.allocate(16);
        // 1. 创建服务器
        ServerSocketChannel ssc = ServerSocketChannel.open();
        ssc.configureBlocking(false);   // 设置成非阻塞模式

        // 2. 建立selector和channel的联系，也就是注册的过程
        // SelectionKey：事件发生时，通过selectionKey可以得到事件和chanel
        SelectionKey sscKey = ssc.register(selector, 0, null);
        log.debug("register key {}", sscKey);

        sscKey.interestOps(SelectionKey.OP_ACCEPT);     // 关注accept事件

        ssc.bind(new InetSocketAddress(8999));

        while (true) {
            // 3. 阻塞
            selector.select();      // select 是阻塞的，如果没有事情发生，就阻塞
            // 4. 处理事件, selectedKeys是一个集合，包含了所有的发生的事件
            Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
            while (iterator.hasNext()) {
                // 此时，事件可能是accept，也可能是readable
                SelectionKey key = iterator.next();
                log.debug("key {}", key);
                // 因为我们的select会给我们维护这个 selectedKey,只会往里面加，不会删，因此我们自己处理
                // 完了只会，需要删除掉
                iterator.remove();

                if (key.isAcceptable()) {
                    ServerSocketChannel channel = (ServerSocketChannel) key.channel();
                    SocketChannel sc = channel.accept();
                    // 这里的是读写的socket
                    sc.configureBlocking(false);
                    // attachment
                    ByteBuffer bu = ByteBuffer.allocate(16);    // attachment

                    SelectionKey scKey = sc.register(selector, 0, bu);
                    //                log.debug("server socket {}", sc);
                    scKey.interestOps(SelectionKey.OP_READ);
                    // 处理完了sscKey
                    log.debug("sc {}", sc);
                } else if (key.isReadable()) {
                    // 读取数据逻辑
                    try {
                        SocketChannel channel = (SocketChannel) key.channel();
                        ByteBuffer bu = (ByteBuffer)key.attachment();
                        int read = channel.read(bu);
                        // 正常断开，返回值位-1
                        // 0 正常断开
                        if (read == -1) {
                            key.cancel();
                        } else {
                            split(bu);
                            if (bu.limit() == bu.position()) {
                                // bu是满的，而且split没有切割
                                ByteBuffer newBuffer = ByteBuffer.allocate(bu.capacity() * 2);
                                bu.flip();
                                newBuffer.put(bu);
                                key.attach(newBuffer);
                            }
                            System.out.println(Charset.defaultCharset().decode(bu));
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                        key.cancel();  // 从 selector集合中删除了
                    }
                }
            }
        }
    }
}
