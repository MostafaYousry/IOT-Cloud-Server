import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.*;
import java.net.*;
import java.util.HashMap;
import java.util.ArrayList;

public class MQTTBroker {

    //list of the topics
    //when the message contains publish or subscribe, we must get the topic object for the specified topic in the message to handel the operation

    private static final MQTTBroker mqttBrokerInstance = new MQTTBroker();
    private static HashMap<String, MQTTTopic> topics = new HashMap<>();
    ServerSocket MyServer;

    {
        try {
            MyServer = new ServerSocket(4444);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private MQTTBroker() {
    }

    public static MQTTBroker getInstance() {
        return mqttBrokerInstance;
    }

    public void brokerAlwaysOn() throws Exception {
        DatagramSocket socket = new DatagramSocket(5555, InetAddress.getByName("0.0.0.0"));
        socket.setBroadcast(true);
        System.out.println("Listen on " + socket.getLocalAddress() + " from " + socket.getInetAddress() + " port " + socket.getBroadcast());
        HttpServer server = HttpServer.create(new InetSocketAddress(6666), 0);
        HttpContext context = server.createContext("/");
        context.setHandler(MQTTBroker::handleRequest);
        server.start();

        while (true) {

        }
    }

    private MQTTTopic getTopic(String topicName) {
        return topics.get(topicName);
    }

    //create topic method
    private void createTopic(String topicName) {
        MQTTTopic newTopic = new MQTTTopic();
        topics.put(topicName, newTopic);
    }

    //callback function
    private static void handleRequest(HttpExchange exchange) throws IOException {
        System.out.println(getBody(exchange));
        String response = "Hello Button!";
        exchange.sendResponseHeaders(200, response.getBytes().length);//response code and length
        OutputStream os = exchange.getResponseBody();
        os.write(response.getBytes());
        os.close();
    }

    private static String getBody(HttpExchange request) throws IOException {

        String body = null;
        StringBuilder stringBuilder = new StringBuilder();
        BufferedReader bufferedReader = null;

        try {
            InputStream inputStream = request.getRequestBody();
            if (inputStream != null) {
                bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                char[] charBuffer = new char[128];
                int bytesRead = -1;
                while ((bytesRead = bufferedReader.read(charBuffer)) > 0) {
                    stringBuilder.append(charBuffer, 0, bytesRead);
                }
            } else {
                stringBuilder.append("");
            }
        } catch (IOException ex) {
            throw ex;
        } finally {
            if (bufferedReader != null) {
                try {
                    bufferedReader.close();
                } catch (IOException ex) {
                    throw ex;
                }
            }
        }

        body = stringBuilder.toString();
        return body;
    }


    private void SendUDPPacket(String IP, int port, String message) throws UnknownHostException, SocketException {
        byte[] buffer = message.getBytes();
        InetAddress address = InetAddress.getByName(IP);
        DatagramPacket packet2 = new DatagramPacket(buffer, buffer.length, address, port);
        DatagramSocket socket2 = new DatagramSocket();
        try {
            socket2.send(packet2);
        } catch (IOException e) {
            System.out.println("Failed To Send Message");
        }
    }

    private void listen() throws Exception {
        String data = null;
        Socket client = this.MyServer.accept();
        String clientAddress = client.getInetAddress().getHostAddress();
        System.out.println("\r\nNew connection from " + clientAddress + " " + client.getPort());
        BufferedReader in = new BufferedReader(
                new InputStreamReader(client.getInputStream()));
        while ((data = in.readLine()) != null) {
            System.out.println("\r\nMessage from " + clientAddress + ": " + data);
        }
    }

    private void ReceiveUDP(DatagramSocket socket)
    {
        byte[] buf = new byte[512];
        DatagramPacket packet = new DatagramPacket(buf, buf.length);
        System.out.println("Waiting for data");
        try {
            socket.receive(packet);
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println(packet.getAddress());
        System.out.println(packet.getPort());
        System.out.println("Data received");
    }
}
