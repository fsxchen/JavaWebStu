package nio.server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.junit.Test;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MutiThreadServer {
    @Test
    public void mutiThreadServer01() throws IOException {

        Thread.currentThread().setName("boss");

        ServerSocketChannel ssc = ServerSocketChannel.open();
        ssc.configureBlocking(false);
        Selector boss = Selector.open();

        SelectionKey bossKey = ssc.register(boss, SelectionKey.OP_ACCEPT, 0);
        ssc.bind(new InetSocketAddress(9999));

        Worker worker = new Worker("worker");

        while (true) {
            boss.select();      //阻塞
            Iterator<SelectionKey> iterator = boss.selectedKeys().iterator();
            while (iterator.hasNext()) {
                SelectionKey key = iterator.next();
                iterator.remove();

                if (key.isAcceptable()) {
                    SocketChannel sc = ssc.accept();
                    sc.configureBlocking(false);
                    log.debug("sc is connect {}", sc);
                    worker.register(sc);

                    // 注册到work中的selector
                    //                    sc.register(worker.selector, SelectionKey.OP_READ, null);
                }
            }
        }
    }

    class Worker implements Runnable {
        private Thread thread;
        private Selector selector;
        private String name;

        private volatile Boolean start = false;

        ConcurrentLinkedQueue<Runnable> queue = new ConcurrentLinkedQueue<>();

        public Worker(String name) {
            this.name = name;
        }

        // 初始化线程
        public void register(SocketChannel sc) throws IOException {
            if (!start) {
                thread = new Thread(this);  // 去执行run
                thread.start();
                selector = Selector.open();
                start = true;
            }
            //            sc.register(selector, SelectionKey.OP_READ, null);
            // 向队列添加了任务，但是任务并没有运行
            queue.add(() -> {
                try {
                    sc.register(selector, SelectionKey.OP_READ, null);
                } catch (ClosedChannelException e) {
                    throw new RuntimeException(e);
                }
            });
            selector.wakeup();  // 唤醒selector
        }

        @Override
        public void run() {
            while (true) {
                try {
                    selector.select();
                    Runnable task = queue.poll();
                    if (task != null) {
                        task.run();     // 执行了注册
                    }
                    Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();

                    while (iterator.hasNext()) {
                        SelectionKey key = iterator.next();
                        iterator.remove();

                        if (key.isReadable()) {
                            ByteBuffer buffer = ByteBuffer.allocate(16);
                            SocketChannel channel = (SocketChannel) key.channel();
                            channel.read(buffer);

                            buffer.flip();
                            System.out.println(Charset.defaultCharset().decode(buffer));
                        }
                    }

                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }
}
