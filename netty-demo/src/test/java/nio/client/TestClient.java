package nio.client;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;

import org.junit.Test;

public class TestClient {
    @Test
    public void client01() throws IOException {
        SocketChannel sc = SocketChannel.open();
        sc.connect(new InetSocketAddress("localhost", 9999));
        sc.write(Charset.defaultCharset().encode("0123456789abced"));
        System.out.println("waiting...");
    }

}
