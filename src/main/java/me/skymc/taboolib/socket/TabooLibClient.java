package me.skymc.taboolib.socket;

import com.ilummc.tlib.resources.TLocale;
import me.skymc.taboolib.TabooLib;
import me.skymc.taboolib.other.NumberUtils;
import me.skymc.taboolib.socket.packet.Packet;
import me.skymc.taboolib.socket.packet.PacketSerializer;
import org.bukkit.Bukkit;

import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @Author sky
 * @Since 2018-08-22 22:27
 */
public class TabooLibClient {

    private static Socket socket;
    private static BufferedReader reader;
    private static PrintWriter writer;
    private static ExecutorService executorService = Executors.newCachedThreadPool();
    private static Packet packet;
    private static boolean notify = false;
    private static long latestResponse = System.currentTimeMillis();

    public static void init() {
        if (TabooLibSettings.load()) {
            connect();
            Bukkit.getScheduler().runTaskTimerAsynchronously(TabooLib.instance(), TabooLibClient::reconnect, 0, 100);
        } else {
            TLocale.sendToConsole("COMMUNICATION.FAILED-LOAD-SETTINGS", TabooLibSettings.getThrowable().toString());
        }
    }

    public static void sendPacket(Packet packet) {
        writer.println(PacketSerializer.serialize(packet));
    }

    public static void reconnect() {
        if (System.currentTimeMillis() - latestResponse > NumberUtils.getInteger(TabooLibSettings.getSettings().getProperty("channel.timeout"))) {
            connect();
        }
    }

    public static void connect() {
        try {
            if (socket != null) {
                socket.close();
            }
        } catch (Exception ignored) {
        }

        try {
            socket = new Socket("localhost", NumberUtils.getInteger(TabooLibSettings.getSettings().getProperty("channel.port")));
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), TabooLibSettings.getCharset()));
            writer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), TabooLibSettings.getCharset()), true);
            notify = false;
            TLocale.sendToConsole("COMMUNICATION.SUCCESS-CONNECTED");
        } catch (SocketException e) {
            /*
                防止未启用终端服务器导致重复提示连接失败信息
             */
            if (!notify) {
                notify = true;
                TLocale.sendToConsole("COMMUNICATION.FAILED-CONNECT-SERVER");
            }
            return;
        } catch (IOException e) {
            TLocale.sendToConsole("COMMUNICATION.FAILED-CONNECT-CLIENT", e.getMessage());
            return;
        }

        executorService.execute(() -> {
            try {
                while (!socket.isClosed() && (packet = PacketSerializer.unSerialize(reader.readLine())) != null) {
                    packet.readOnClient();
                }
            } catch (IOException e) {
                TLocale.sendToConsole("COMMUNICATION.FAILED-READING-PACKET", e.getMessage());
            }
        });
    }

    // *********************************
    //
    //        Getter and Setter
    //
    // *********************************

    public static Socket getSocket() {
        return socket;
    }

    public static BufferedReader getReader() {
        return reader;
    }

    public static PrintWriter getWriter() {
        return writer;
    }

    public static ExecutorService getExecutorService() {
        return executorService;
    }

    public static Packet getLatestPacket() {
        return packet;
    }

    public static long getLatestResponse() {
        return latestResponse;
    }

    public static void setLatestResponse(long latestResponse) {
        TabooLibClient.latestResponse = latestResponse;
    }
}
