import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

import org.junit.Test;

import com.google.common.annotations.VisibleForTesting;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TestByteBuffer {
    public static void main(String[] args) {
        // FileChannel
        // 1. 输入输出流，2 RandomAccessFile

//        try (FileChannel channel = (new FileInputStream("netty-demo/src/data.txt")).getChannel()) {
//            // 准备缓冲区
//            ByteBuffer  buffer = ByteBuffer.allocate(10);
//            // 从channel中读取数据,写到buffer
//            channel.read(buffer);
//            // 打印buffer的内容
//            buffer.flip();    // 切换至读模式
//            while (buffer.hasRemaining()) {
//                byte b = buffer.get();//一次读一个字节
//                System.out.println((char) b);
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        }

        try (FileChannel channel = new FileInputStream("netty-demo/src/data.txt").getChannel()) {
            // 准备缓冲区
            ByteBuffer  buffer = ByteBuffer.allocate(10);
            // 从channel中读取数据,写到buffer
            while (true) {
                int readLens = channel.read(buffer);
                if (readLens == -1) break;
                // 打印buffer的内容
                buffer.flip();    // 切换至读模式
                while (buffer.hasRemaining()) {
                    byte b = buffer.get();//一次读一个字节
                    System.out.println((char) b);
                }

                buffer.clear();     //切换至写模式
            }


        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void ByteBufferTest() {

    }
}
