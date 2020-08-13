
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JTextPane;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author Crap
 */
public class ScoreTimer extends Thread {

    private HashMap<String, Integer> scores;
    private MultiplayerGame game;
    private DatagramSocket socket;
    private byte[] buf = new byte[256];
    private static ArrayList<String> deadPlayers = new ArrayList<>();
    private static boolean end = false;

    public ScoreTimer(HashMap<String, Integer> scores, MultiplayerGame game) throws SocketException {

        this.scores = scores;
        this.game = game;

        socket = new DatagramSocket(null);
        socket.setReuseAddress(true);
        SocketAddress sa = new InetSocketAddress(4445);
        socket.bind(sa);

    }

    private void addUniqueDeath(String player) {
        if (!deadPlayers.contains(player)) {
            deadPlayers.add(player);
        }
    }

    public static boolean getEnd() {
        return end;
    }

    public void run() {
        try {
            update();

        } catch (IOException ex) {
            Logger.getLogger(ScoreTimer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void update() throws IOException {

        while (true) {
            for (int i = 0; i < scores.keySet().size(); i++) {
                DatagramPacket packet = new DatagramPacket(buf, buf.length);
                socket.receive(packet);
                String recieved = new String(packet.getData(), 0, packet.getLength());
                System.out.println("RECIEVED: " + recieved);
                String[] command = recieved.split(" ");
                if (command[0].equals("SCORE") && scores.keySet().contains(command[1])) {
                    scores.put(command[1], Integer.parseInt(command[2]));
                } else if (command[0].equals("DEATH")) {
                    addUniqueDeath(command[1]);

                    if (scores.keySet().size() == deadPlayers.size()) {
                        Broadcast.setRunning(false);
                        end = true;

                    }
                }
            }
            game.setScores(scores);
            //System.out.println(scores);
        }
    }
}
