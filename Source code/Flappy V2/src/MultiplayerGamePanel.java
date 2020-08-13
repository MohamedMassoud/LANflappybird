
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.DataLine;

public class MultiplayerGamePanel extends JPanel implements Runnable {

    private MultiplayerGame game;
    private String playerName;
    private Map<String, Integer> sortedScores = new HashMap<>();
    public Clip clip;

    public MultiplayerGamePanel(ArrayList<String> players, String playerName) throws IOException {
        playMusic();
        game = new MultiplayerGame(players, playerName);
        this.playerName = playerName;
        new Thread(this).start();
    }

    private void playMusic() throws FileNotFoundException {
        try {
            File yourFile = new File("lib/mario.wav");
            AudioInputStream stream;
            AudioFormat format;
            DataLine.Info info;

            stream = AudioSystem.getAudioInputStream(yourFile);
            format = stream.getFormat();
            info = new DataLine.Info(Clip.class, format);
            clip = (Clip) AudioSystem.getLine(info);
            clip.open(stream);
            clip.loop(Clip.LOOP_CONTINUOUSLY);

        } catch (Exception e) {
            //whatevers
        }
    }

    public void update() throws IOException, InterruptedException {
        game.update();
        revalidate();
        repaint();
    }

    /**
     *
     * @param g
     */
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2D = (Graphics2D) g;
        for (Render r : game.getRenders()) {
            if (r.transform != null) {
                g2D.drawImage(r.image, r.transform, null);
            } else {
                g.drawImage(r.image, r.x, r.y, null);
            }
        }

        g2D.setColor(Color.BLACK);

        if (!game.started) {
            g2D.setFont(new Font("TimesRoman", Font.PLAIN, 20));
            g2D.drawString("Press SPACE to start", 150, 240);
        } else {
            g2D.setFont(new Font("TimesRoman", Font.ITALIC, 15));
            Map<String, Integer> scores = game.getScores();
            sortedScores = sortByValue(scores, false);

            int i = 20;
            for (String key : sortedScores.keySet()) {
                if (key.equals(playerName)) {
                    g2D.setColor(Color.RED);
                } else {
                    g2D.setColor(Color.BLACK);
                }
                g2D.drawString(key + ": " + sortedScores.get(key), 350, i);
                i += 20;
            }

        }

        if (game.gameover) {
            clip.stop();
            g2D.setColor(Color.red);
            g2D.setFont(new Font("TimesRoman", Font.PLAIN, 20));
            if (!ScoreTimer.getEnd()) {
                g2D.drawString("Game Over", 175, 240);
            } else {
                g2D.drawString("Winner is " + sortedScores.keySet().toArray()[0], 175, 240);
            }
        }
    }

    public void run() {
        try {
            while (true) {
                update();
                Thread.sleep(25);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static Map<String, Integer> sortByValue(Map<String, Integer> unsortMap, final boolean order) {
        LinkedList<Entry<String, Integer>> list = new LinkedList<>(unsortMap.entrySet());

        // Sorting the list based on values
        list.sort((o1, o2) -> order ? o1.getValue().compareTo(o2.getValue()) == 0
                ? o1.getKey().compareTo(o2.getKey())
                : o1.getValue().compareTo(o2.getValue()) : o2.getValue().compareTo(o1.getValue()) == 0
                ? o2.getKey().compareTo(o1.getKey())
                : o2.getValue().compareTo(o1.getValue()));
        return list.stream().collect(Collectors.toMap(Entry::getKey, Entry::getValue, (a, b) -> b, LinkedHashMap::new));

    }
}
