
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Objects;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author Crap
 */
public class Broadcast extends TimerTask {

    private DatagramSocket socket = null;
    private String broadcastMessage;
    private InetAddress address;
    private static boolean running = true;
    private static ArrayList<InetAddress> broadcastList = new ArrayList<>();

    public static void setRunning(boolean running) {
        Broadcast.running = running;
    }

    static ArrayList<InetAddress> listAllBroadcastAddresses() throws SocketException {
        if (!broadcastList.isEmpty()) {
            return broadcastList;
        }
        Enumeration<NetworkInterface> interfaces
                = NetworkInterface.getNetworkInterfaces();
        while (interfaces.hasMoreElements()) {
            NetworkInterface networkInterface = interfaces.nextElement();

            if (networkInterface.isLoopback() || !networkInterface.isUp()) {
                continue;
            }

            networkInterface.getInterfaceAddresses().stream()
                    .map(a -> a.getBroadcast())
                    .filter(Objects::nonNull)
                    .forEach(broadcastList::add);
        }
        return broadcastList;
    }

    public Broadcast(String broadcastMessage, InetAddress address) throws IOException {
        this.broadcastMessage = broadcastMessage;
        this.address = address;

    }

    public void stopBroadcasting() {
        this.cancel();
    }

    public void run() {
        //System.out.println("Broadcasting");
        if (!running) {
            this.cancel();
        }
        try {
            socket = new DatagramSocket();
        } catch (SocketException ex) {
            Logger.getLogger(Broadcast.class.getName()).log(Level.SEVERE, null, ex);
        }
        try {
            socket.setBroadcast(true);
        } catch (SocketException ex) {
            Logger.getLogger(Broadcast.class.getName()).log(Level.SEVERE, null, ex);
        }

        byte[] buffer = broadcastMessage.getBytes();

        DatagramPacket packet
                = new DatagramPacket(buffer, buffer.length, address, 4445);
        try {
            socket.send(packet);
            String sent = new String(packet.getData(), 0, packet.getLength());
            //System.out.println("Sent data: " + sent);
        } catch (IOException ex) {
            Logger.getLogger(Broadcast.class.getName()).log(Level.SEVERE, null, ex);
        }
        socket.close();

    }

}
