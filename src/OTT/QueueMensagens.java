package OTT;

import java.util.LinkedList;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class QueueMensagens {
    private LinkedList<String> messagesToSend;
    private ReentrantLock lock;
    private Condition con;

    public QueueMensagens() {
        this.messagesToSend = new LinkedList<>();
        this.lock = new ReentrantLock();
        this.con = lock.newCondition();
    }

    public void add(String mensagem) {
        lock.lock();
        try {
            messagesToSend.add(mensagem);
            con.signalAll();
        } finally {
            lock.unlock();
        }
    }

    public void addFirst(String mensagem) {
        lock.lock();
        try {
            messagesToSend.addFirst(mensagem);
            con.signalAll();
        } finally {
            lock.unlock();
        }
    }

    public String remove() throws InterruptedException {
        lock.lock();
        try {
            while (messagesToSend.isEmpty())
                con.await();

            return messagesToSend.isEmpty() ? null : messagesToSend.remove();
        } finally {
            lock.unlock();
        }
    }

    public boolean isEmpty() {
        lock.lock();
        try {
            return messagesToSend.isEmpty();
        } finally {
            lock.unlock();
        }
    }

    public void signalCon() {
        lock.lock();
        try {
            while(!messagesToSend.isEmpty());

            con.signalAll();
        } finally {
            lock.unlock();
        }
    }
}
