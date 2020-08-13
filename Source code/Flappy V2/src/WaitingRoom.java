
import java.awt.Color;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JEditorPane;
import javax.swing.JTextArea;
import javax.swing.JTextPane;
import javax.swing.text.AttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author Crap
 */
public class WaitingRoom extends Thread {

    private DatagramSocket socket;
    private JTextPane ta;
    private byte[] buf = new byte[256];
    private ArrayList<String> players = new ArrayList<String>();
    private ArrayList<String> readyPlayers = new ArrayList<String>();
    private User user;

    public WaitingRoom(JTextPane ta, DatagramSocket socket, User user) throws IOException {
        this.ta = ta;
        this.socket = socket;
        this.user = user;
    }

    public void run() {

        try {

            updateTextArea();
        } catch (IOException ex) {
            Logger.getLogger(WaitingRoom.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InterruptedException ex) {
            Logger.getLogger(WaitingRoom.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private static final String ALPHA_NUMERIC_STRING = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";

    public static String randomAlphaNumeric(int count) {
        StringBuilder builder = new StringBuilder();
        while (count-- != 0) {
            int character = (int) (Math.random() * ALPHA_NUMERIC_STRING.length());
            builder.append(ALPHA_NUMERIC_STRING.charAt(character));
        }
        return builder.toString();
    }

    private void updateTextArea() throws IOException, InterruptedException {
        //ta.removeAll();
        while (true) {

            //System.out.println(this.toString());
            DatagramPacket packet = new DatagramPacket(buf, buf.length);
            // System.out.println(packet.getAddress());
            socket.receive(packet);
            // System.out.println("TADAAAAAAAAAA");

            String recieved = new String(packet.getData(), 0, packet.getLength());
            String[] command = recieved.split(" ");
            if (command[0].equals("Entry")) {
                addUnique(command[1]);
            } else if (command[0].equals("READY")) {
                addUnique(command[1]);
                addUniqueReady(command[1]);
            }

            ta.setText(null);

            for (String player : players) {
                if (readyPlayers.contains(player)) {
                    appendToPane(ta, player + "\n", Color.GREEN);

                } else {
                    appendToPane(ta, player + "\n", Color.RED);

                }
            }

            if (players.size() == readyPlayers.size() && players.size() != 0) {
                System.out.println("All: " + players.size() + " Players are ready at thread: " + this.toString());

                Broadcast.setRunning(false);

                appendToPane(ta, "Game will start in 5\n", Color.RED);
                Thread.currentThread().sleep(1000);
                appendToPane(ta, "Game will start in 4\n", Color.RED);
                Thread.currentThread().sleep(1000);
                appendToPane(ta, "Game will start in 3\n", Color.RED);
                Thread.currentThread().sleep(1000);
                appendToPane(ta, "Game will start in 2\n", Color.RED);
                Thread.currentThread().sleep(1000);
                appendToPane(ta, "Game will start in 1\n", Color.RED);
                Thread.currentThread().sleep(1000);
                String gameID = randomAlphaNumeric(32);
                new Launcher("multi", players, user.getName());
                players = new ArrayList<String>();
                readyPlayers = new ArrayList<String>();
                break;
            }

        }

    }

    private void appendToPane(JTextPane tp, String msg, Color c) {
        StyleContext sc = StyleContext.getDefaultStyleContext();
        AttributeSet aset = sc.addAttribute(SimpleAttributeSet.EMPTY, StyleConstants.Foreground, c);

        aset = sc.addAttribute(aset, StyleConstants.FontFamily, "Lucida Console");
        aset = sc.addAttribute(aset, StyleConstants.Alignment, StyleConstants.ALIGN_JUSTIFIED);

        int len = tp.getDocument().getLength();
        tp.setCaretPosition(len);
        tp.setCharacterAttributes(aset, false);
        tp.replaceSelection(msg);
    }

    private void addUnique(String player) {
        if (!players.contains(player)) {
            players.add(player);
        }
    }

    private void addUniqueReady(String player) {
        if (!readyPlayers.contains(player)) {
            readyPlayers.add(player);
        }
    }

}
