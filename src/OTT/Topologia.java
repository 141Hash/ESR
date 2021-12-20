package OTT;

import java.util.HashMap;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;

public class Topologia {
    private HashMap<String, Set<String>> topologia;
    private ReentrantLock lock;

    public Topologia() {
        this.topologia = new HashMap<>();
        this.lock      = new ReentrantLock();
    }

    public Topologia(HashMap<String, Set<String>> topologia) {
        this.topologia = topologia;
        this.lock      = new ReentrantLock();
    }

    public HashMap<String, Set<String>> getTopologia() {
        lock.lock();
        try {
            return topologia;
        } finally {
            lock.unlock();
        }
    }

    public void setTopologia(HashMap<String, Set<String>> topologia) {
        lock.lock();
        try {
            this.topologia = topologia;
        } finally {
            lock.unlock();
        }
    }

    public Set<String> getVizinhos (String ipAdress) {
        lock.lock();
        try {
            return  this.topologia.get(ipAdress);
        } finally {
            lock.unlock();
        }
    }
}
