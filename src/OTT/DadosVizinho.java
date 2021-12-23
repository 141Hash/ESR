package OTT;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.net.Socket;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.LinkedList;
import java.util.concurrent.locks.ReentrantLock;

public class DadosVizinho {
    private String ipVizinho;
    private DataOutputStream dos;
    private BufferedReader dis;
    private Socket socket;
    private QueueMensagens messagesToSend;
    private ReentrantLock lock;
    private LocalTime lastUpdate;

    public DadosVizinho (String ipVizinho, DataOutputStream dos, BufferedReader dis, Socket socket) {
        this.ipVizinho = ipVizinho;
        this.dos = dos;
        this.dis = dis;
        this.socket = socket;
        this.messagesToSend = new QueueMensagens();
        this.lock = new ReentrantLock();
        this.lastUpdate = LocalTime.now(ZoneId.of("UTC"));
    }

    public String getIpVizinho() {
        lock.lock();
        try {
            return ipVizinho;
        } finally {
            lock.unlock();
        }
    }

    public DataOutputStream getDos() {
        lock.lock();
        try {
            return dos;
        } finally {
            lock.unlock();
        }
    }

    public BufferedReader getDis() {
        lock.lock();
        try {
            return dis;
        } finally {
            lock.unlock();
        }
    }

    public Socket getSocket() {
        lock.lock();
        try {
            return socket;
        } finally {
            lock.unlock();
        }
    }

    public void addMessagesToSend(String message) {
        lock.lock();
        try {
            this.messagesToSend.add(message);
        } finally {
            lock.unlock();
        }
    }

    public QueueMensagens getMessagesToSend() {
        lock.lock();
        try {
            return messagesToSend;
        } finally {
            lock.unlock();
        }
    }

    public LocalTime getLastUpdate() {
        lock.lock();
        try {
            return this.lastUpdate;
        } finally {
            lock.unlock();
        }
    }

    public void updateTime() {
        lock.lock();
        try {
            this.lastUpdate = LocalTime.now(ZoneId.of("UTC"));
        } finally {
            lock.unlock();
        }
    }
}
