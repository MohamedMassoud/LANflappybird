
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Timer;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author Crap
 */
public class User {

    private ArrayList<InetAddress> addresses;
    private String name;
    private Broadcast bc;
    private Timer entryTimer;
    private Timer readyTimer;

    public User(ArrayList<InetAddress> addresses, String name, Broadcast bc) {
        this.addresses = addresses;
        this.name = name;
        this.bc = bc;

    }

    public Timer getEntryTimer() {
        return entryTimer;
    }

    public Timer getReadyTimer() {
        return entryTimer;
    }

    public void setEntryTimer(Timer entryTimer) {
        this.entryTimer = entryTimer;
    }

    public void setReadyTimer(Timer readyTimer) {
        this.readyTimer = readyTimer;
    }

    public ArrayList<InetAddress> getAddresses() {
        return addresses;
    }

    public String getName() {
        return name;
    }

    public Broadcast getBroadcast() {
        return bc;
    }

}
