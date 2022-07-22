package nio.eventLoop;

import java.util.concurrent.TimeUnit;

import io.netty.channel.DefaultEventLoop;
import io.netty.channel.nio.NioEventLoopGroup;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TestEventLoop {
    public static void main(String[] args) {
        // 1. 创建事件循环对象
        NioEventLoopGroup group = new NioEventLoopGroup(2);// io事件、普通任务、定时任务
        // 如果没指定，默认是cpu数量*2
        //        new DefaultEventLoop();     // 普通任务，定时任务

        System.out.println(group.next());       // 获取第一个事件循环对象，这里有2个
        System.out.println(group.next());      // 获取第二个事件循环对象，这里有2个
        System.out.println(group.next());       // 获取第一个事件循环对象，这里有2个
        System.out.println(group.next());      // 获取第二个事件循环对象，这里有2个

        // 执行一个普通任务，
        group.next().submit(()-> {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            log.debug("Ok");
        });

        // 执行定时任务
        group.next().scheduleAtFixedRate(()-> {
            System.out.println("hello");
        }, 0, 1, TimeUnit.SECONDS);

    }
}
