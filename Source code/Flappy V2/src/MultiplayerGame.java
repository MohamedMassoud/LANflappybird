
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Timer;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.DataLine;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

public class MultiplayerGame {

    public static final int PIPE_DELAY = 100;

    private Boolean paused;

    private int pauseDelay;
    private int restartDelay;
    private int pipeDelay;

    private Bird bird;
    private ArrayList<Pipe> pipes;
    private Keyboard keyboard;

    public int score;
    public HashMap<String, Integer> scores = new HashMap<>();
    public Boolean gameover;
    public Boolean started;
    private ArrayList<String> players = new ArrayList<>();
    private Broadcast bc = null;
    private Timer t;
    private ArrayList<Timer> timers = new ArrayList<>();
    private String playerName;
    public Clip clip;

    public MultiplayerGame(ArrayList<String> players, String playerName) throws IOException {
        this.playerName = playerName;
        this.players = players;

        broadcastScore();

        updateScores();

        keyboard = Keyboard.getInstance();

        restart();
    }

    private void playMusic(String path) throws FileNotFoundException {
        try {
            File yourFile = new File(path);
            AudioInputStream stream;
            AudioFormat format;
            DataLine.Info info;

            stream = AudioSystem.getAudioInputStream(yourFile);
            format = stream.getFormat();
            info = new DataLine.Info(Clip.class, format);
            clip = (Clip) AudioSystem.getLine(info);
            clip.open(stream);
            clip.start();

        } catch (Exception e) {
            //whatevers
        }
    }

    public HashMap<String, Integer> getScores() {
        return scores;
    }

    public String getPlayerName() {
        return playerName;
    }

    public void broadcastScore() throws IOException {
        System.out.println("broad");

        Broadcast.setRunning(true);

        ArrayList<InetAddress> addresses = null;

        addresses = Broadcast.listAllBroadcastAddresses();

        System.out.println(addresses);
        // User user = null;
        for (InetAddress add : addresses) {
            System.out.println("ADD: " + add);
            bc = new Broadcast("SCORE " + playerName + " " + score, add);
            //System.out.println("BROAD: " + "SCORE " + playerName + " " + score+" " + gameID);
            t = new Timer();
            timers.add(t);

            // This task is scheduled to run every 10 seconds
            // user = new User(add, name, bc);
            // user.setEntryTimer(t);
            t.scheduleAtFixedRate(bc, 0, 1000);
        }
        System.out.println("finished");
    }

    public void updateScores() throws SocketException {
        System.out.println("update");
        System.out.println("PLAYERS: " + players);
        for (String player : players) {
            scores.put(player, 0);
        }
        System.out.println("SCORES: " + scores);
        ScoreTimer st = new ScoreTimer(scores, this);
        st.start();
    }

    public void setScores(HashMap<String, Integer> scores) {
        this.scores = scores;
    }

    public void restart() {
        paused = false;
        started = false;
        gameover = false;

        score = 0;
        pauseDelay = 0;
        restartDelay = 0;
        pipeDelay = 0;

        bird = new Bird();
        pipes = new ArrayList<Pipe>();
    }

    public void update() throws IOException, InterruptedException {
        watchForStart();

        if (!started) {
            return;
        }

        //watchForPause();
        //watchForReset();
        if (paused) {
            return;
        }

        bird.update();

        if (gameover) {
            return;
        }

        movePipes();
        checkForCollisions();
    }

    public ArrayList<Render> getRenders() {
        ArrayList<Render> renders = new ArrayList<Render>();
        renders.add(new Render(0, 0, "lib/background.png"));
        for (Pipe pipe : pipes) {
            renders.add(pipe.getRender());
        }
        renders.add(new Render(0, 0, "lib/foreground.png"));
        renders.add(bird.getRender());
        return renders;
    }

    private void watchForStart() {
        if (!started && keyboard.isDown(KeyEvent.VK_SPACE)) {
            started = true;
        }
    }

    /* private void watchForPause() {
        if (pauseDelay > 0)
            pauseDelay--;

        if (keyboard.isDown(KeyEvent.VK_P) && pauseDelay <= 0) {
            paused = !paused;
            pauseDelay = 10;
        }
    }*/

 /* private void watchForReset() {
        if (restartDelay > 0)
            restartDelay--;

        if (keyboard.isDown(KeyEvent.VK_R) && restartDelay <= 0) {
            restart();
            restartDelay = 10;
            return;
        }
    }*/
    private void movePipes() {
        pipeDelay--;

        if (pipeDelay < 0) {
            pipeDelay = PIPE_DELAY;
            Pipe northPipe = null;
            Pipe southPipe = null;

            // Look for pipes off the screen
            for (Pipe pipe : pipes) {
                if (pipe.x - pipe.width < 0) {
                    if (northPipe == null) {
                        northPipe = pipe;
                    } else if (southPipe == null) {
                        southPipe = pipe;
                        break;
                    }
                }
            }

            if (northPipe == null) {
                Pipe pipe = new Pipe("north");
                pipes.add(pipe);
                northPipe = pipe;
            } else {
                northPipe.reset();
            }

            if (southPipe == null) {
                Pipe pipe = new Pipe("south");
                pipes.add(pipe);
                southPipe = pipe;
            } else {
                southPipe.reset();
            }

            northPipe.y = southPipe.y + southPipe.height + 175;
        }

        for (Pipe pipe : pipes) {
            pipe.update();
        }
    }

    private void checkForCollisions() throws IOException, InterruptedException {

        for (Pipe pipe : pipes) {
            if (pipe.collides(bird.x, bird.y, bird.width, bird.height)) {
                playMusic("lib/hit.wav");
                playMusic("lib/die.wav");
                gameover = true;
                bird.dead = true;
                Broadcast.setRunning(true);

                ArrayList<InetAddress> addresses = null;

                addresses = Broadcast.listAllBroadcastAddresses();

                System.out.println(addresses);
                // User user = null;
                for (InetAddress add : addresses) {
                    Broadcast b = new Broadcast("DEATH " + playerName, add);
                    Timer ti = new Timer();
                    //timers.add(t);

                    // This task is scheduled to run every 10 seconds
                    // user = new User(add, name, bc);
                    // user.setEntryTimer(t);
                    ti.scheduleAtFixedRate(b, 0, 1000);
                }

            } else if (pipe.x == bird.x && pipe.orientation.equalsIgnoreCase("south")) {

                playMusic("lib/point.wav");
                score++;

                for (int i = 0; i < timers.size(); i++) {
                    timers.get(i).cancel();
                    //timers.remove(i);
                }

                Broadcast.setRunning(true);

                ArrayList<InetAddress> addresses = null;

                addresses = Broadcast.listAllBroadcastAddresses();

                System.out.println(addresses);
                // User user = null;
                for (InetAddress add : addresses) {
                    System.out.println("ADD: " + add);
                    bc = new Broadcast("SCORE " + playerName + " " + score, add);
                    t = new Timer();
                    timers.add(t);

                    // This task is scheduled to run every 10 seconds
                    // user = new User(add, name, bc);
                    // user.setEntryTimer(t);
                    t.scheduleAtFixedRate(bc, 0, 1000);
                }

            }
        }

        // Ground + Bird collision
        if (bird.y + bird.height > Launcher.HEIGHT - 80) {
            playMusic("lib/die.wav");
            gameover = true;
            Broadcast.setRunning(true);

            ArrayList<InetAddress> addresses = null;

            addresses = Broadcast.listAllBroadcastAddresses();

            System.out.println(addresses);
            // User user = null;
            for (InetAddress add : addresses) {
                Broadcast b = new Broadcast("DEATH " + playerName, add);
                Timer ti = new Timer();
                //timers.add(t);

                // This task is scheduled to run every 10 seconds
                // user = new User(add, name, bc);
                // user.setEntryTimer(t);
                ti.scheduleAtFixedRate(b, 0, 1000);
            }

            bird.y = Launcher.HEIGHT - 80 - bird.height;
            /* for(int i=0; i<timers.size(); i++){
                  timers.get(i).cancel();
                  //timers.remove(i);
              }*/
        }
    }
}
