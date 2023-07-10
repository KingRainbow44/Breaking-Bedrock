package lol.magix.breakingbedrock;

import com.google.common.net.HostAndPort;
import lol.magix.breakingbedrock.utils.NetworkUtils;

public final class PingTest {
    public static void main(String[] args) {
        try {
            System.out.println("Pinging server...");

            var pong = NetworkUtils.pingServer(
                    HostAndPort.fromParts("localhost", 19132)
            );

            System.out.println(pong);
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }
}
