package io.izzel.taboolib.client.server;

import io.izzel.taboolib.client.TabooLibServer;
import io.izzel.taboolib.client.TabooLibSettings;
import io.izzel.taboolib.client.packet.impl.PacketJoin;
import io.izzel.taboolib.client.packet.impl.PacketQuit;
import org.bukkit.util.NumberConversions;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketException;

/**
 * @Author sky
 * @Since 2018-08-22 22:30
 */
public class ClientConnection implements Runnable {

    private final Socket socket;
    private BufferedReader reader;
    private PrintWriter writer;
    private String latestPacket;
    private long latestResponse = System.currentTimeMillis();

    public ClientConnection(Socket socket) {
        this.socket = socket;
        try {
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), TabooLibSettings.getCharset()));
            writer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), TabooLibSettings.getCharset()), true);
            TabooLibServer.sendPacket(new PacketJoin(socket.getPort()));
        } catch (Exception e) {
            TabooLibServer.println("Client joined failed: " + e.toString());
        }
    }

    @Override
    public void run() {
        try {
            while ((latestPacket = reader.readLine()) != null) {
                TabooLibServer.sendPacket(latestPacket);
            }
        } catch (SocketException e) {
            /*
                连接丢失，客户端退出
             */
            TabooLibServer.sendPacket(new PacketQuit(socket.getPort(), "SocketException: " + e.getMessage()));
        } catch (Exception e) {
            TabooLibServer.println("Client running failed: " + e.toString());
        }
    }

    public boolean isAlive() {
        return System.currentTimeMillis() - latestResponse < NumberConversions.toInt(TabooLibSettings.getSettings().getProperty("channel.timeout"));
    }

    // *********************************
    //
    //        Getter and Setter
    //
    // *********************************

    public Socket getSocket() {
        return socket;
    }

    public BufferedReader getReader() {
        return reader;
    }

    public PrintWriter getWriter() {
        return writer;
    }

    public String getLatestPacket() {
        return latestPacket;
    }

    public long getLatestResponse() {
        return latestResponse;
    }

    public void setLatestResponse(long latestResponse) {
        this.latestResponse = latestResponse;
    }
}
