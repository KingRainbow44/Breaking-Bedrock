package lol.magix.breakingbedrock.objects;

import java.net.InetSocketAddress;

/**
 * Server connection details.
 * @param address The server address.
 * @param port The server port.
 * @param online Authentication requirement.
 */
public record ConnectionDetails(
        String address,
        int port,
        boolean online
) {
    /**
     * Converts the connection details to an InetSocketAddress.
     * @return The InetSocketAddress.
     */
    public InetSocketAddress toSocketAddress() {
        return new InetSocketAddress(this.address, this.port);
    }
}