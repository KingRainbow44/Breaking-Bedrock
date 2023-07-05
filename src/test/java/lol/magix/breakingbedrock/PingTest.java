package lol.magix.breakingbedrock;

import lol.magix.breakingbedrock.utils.NetworkUtils;
import net.minecraft.client.network.ServerAddress;

public final class PingTest {
    public static void main(String[] args) {
        try {
            System.out.println("Pinging server...");

            var pong = NetworkUtils.pingServer(
                    new ServerAddress("localhost", 19132)
            );

            System.out.println(pong);
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }
}
