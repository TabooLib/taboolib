package io.izzel.taboolib.client;

import io.izzel.taboolib.client.packet.Packet;
import io.izzel.taboolib.client.packet.PacketSerializer;
import io.izzel.taboolib.client.packet.impl.PacketHeartbeat;
import io.izzel.taboolib.client.packet.impl.PacketQuit;
import io.izzel.taboolib.client.server.ClientConnection;
import org.bukkit.util.NumberConversions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * 研究了一个小时 log4j 愣是没整明白，不搞那些高端日志工具了
 *
 * @author sky
 * @since 2018-08-22 20:45
 */
public class TabooLibServer {

    private static ServerSocket server = null;
    private static final Logger logger = LoggerFactory.getLogger(TabooLibServer.class);
    private static final ExecutorService executorService = Executors.newCachedThreadPool();
    private static final ConcurrentHashMap<Integer, ClientConnection> client = new ConcurrentHashMap<>();

    public static void main(String[] args) {
        println("TabooLib Communication Network Starting...");

        if (!TabooLibSettings.load()) {
            println("Settings loading failed: " + TabooLibSettings.getError().toString());
            return;
        }

        try {
            server = new ServerSocket(NumberConversions.toInt(TabooLibSettings.getSettings().getProperty("channel.port")));
            println("Starting server on port " + server.getInetAddress().getHostName() + ":" + server.getLocalPort());
        } catch (IOException e) {
            println("Server starting failed: " + e.toString());
            return;
        }

        Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(() -> {
            /*
                向所有已连接的客户端发送心跳包
             */
            sendPacket(new PacketHeartbeat(0));
            /*
                检测无效的客户端连接，如果超过 5000 毫秒没有收到客户端的回应（上一次心跳包的回应）则注销链接
             */
            client.entrySet().stream().filter(connection -> !connection.getValue().isAlive()).map(connection -> new PacketQuit(connection.getKey(), "Lost connection")).forEach(TabooLibServer::sendPacket);
        }, 0, 1, TimeUnit.SECONDS);

        /*
            异步接收连接请求
         */
        Executors.newSingleThreadScheduledExecutor().execute(() -> {
            while (!server.isClosed()) {
                try {
                    Socket socket = server.accept();
                    ClientConnection connection = new ClientConnection(socket);
                    client.put(socket.getPort(), connection);
                    executorService.execute(connection);
                    println("Client accepted: " + socket.getPort() + " online: " + client.size());
                } catch (Exception e) {
                    println("Client accept failed: " + e.toString());
                }
            }
        });
    }

    public static void sendPacket(Packet packet) {
        sendPacket(PacketSerializer.serialize(packet));
    }

    public static void sendPacket(String origin) {
        // 在服务端尝试解析动作并运行
        PacketSerializer.unSerialize(origin).readOnServer();
        // 将动作发送至所有客户端
        for (ClientConnection connection : TabooLibServer.getClient().values()) {
            try {
                connection.getWriter().println(origin);
            } catch (Exception e) {
                TabooLibServer.println("Packet sending failed: " + e.toString());
            }
        }
    }

    public static void println(Object obj) {
        logger.info(obj.toString());
    }

    public static Optional<Map.Entry<Integer, ClientConnection>> getConnection(int port) {
        return client.entrySet().stream().filter(entry -> entry.getKey().equals(port)).findFirst();
    }

    public static ServerSocket getServer() {
        return server;
    }

    public static ConcurrentHashMap<Integer, ClientConnection> getClient() {
        return client;
    }

    public static ExecutorService getExecutorService() {
        return executorService;
    }

    public static Logger getLogger() {
        return logger;
    }
}
