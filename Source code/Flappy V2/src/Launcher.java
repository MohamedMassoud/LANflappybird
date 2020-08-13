
import java.io.IOException;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.*;

public class Launcher {

    public static int WIDTH = 500;
    public static int HEIGHT = 512;
    private User user;
    private ArrayList<String> players;
    private String playerName;

    public Launcher(String gameType) throws SocketException, UnknownHostException {
        if (gameType.equals("single")) {
            singlePlayer();
        }

    }

    public Launcher(String gameType, User user) throws SocketException, UnknownHostException {
        if (gameType.equals("waiting")) {
            //System.out.println("HERE");
            this.user = user;
            waiting();

        }
    }

    public Launcher(String gameType, ArrayList<String> players, String playerName) throws IOException {
        this.players = players;
        this.playerName = playerName;
        if (gameType.equals("multi")) {
            multiPlayer();
        }
    }

    public void multiPlayer() throws IOException {

        JFrame frame = new JFrame();
        frame.setVisible(true);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setSize(WIDTH, HEIGHT);
        frame.setLocationRelativeTo(null);
        frame.setResizable(false);

        Keyboard keyboard = Keyboard.getInstance();
        frame.addKeyListener(keyboard);

        MultiplayerGamePanel panel = new MultiplayerGamePanel(players, playerName);

        frame.add(panel);

    }

    public void singlePlayer() {

        JFrame frame = new JFrame();
        frame.setVisible(true);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setSize(WIDTH, HEIGHT);
        frame.setLocationRelativeTo(null);
        frame.setResizable(false);

        Keyboard keyboard = Keyboard.getInstance();
        frame.addKeyListener(keyboard);

        GamePanel panel = new GamePanel();
        frame.add(panel);

    }

    public void waiting() throws SocketException, UnknownHostException {
        createConnection();
    }

    public void createConnection() throws SocketException, UnknownHostException {

        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                try {
                    //System.out.println("Created: " + this.toString());
                    new ConnectionGUI(user).setVisible(true);

                } catch (SocketException ex) {
                    Logger.getLogger(ConnectionGUI.class.getName()).log(Level.SEVERE, null, ex);
                } catch (UnknownHostException ex) {
                    Logger.getLogger(ConnectionGUI.class.getName()).log(Level.SEVERE, null, ex);
                }

            }

        });
        // ConnectionGUI cg = new ConnectionGUI(user);
        /*System.out.println("HERRRRRRRRRRRRRR " + user.getBroadcast());
        cg.main();*/

    }

}
