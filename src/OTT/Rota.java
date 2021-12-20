package OTT;

import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.locks.ReentrantLock;

public class Rota {
    private int custo;
    private String origem;
    private Set<String> destinos;
    private boolean estado;
    private ReentrantLock lock;

    public Rota() {
        this.custo = -1;
        this.origem = null;
        this.destinos = new TreeSet<>();
        this.estado = false;
        this.lock = new ReentrantLock();
    }

    public int getCusto() {
        lock.lock();
        try {
            return custo;
        } finally {
            lock.unlock();
        }
    }

    public String getOrigem() {
        lock.lock();
        try {
            return origem;
        } finally {
            lock.unlock();
        }
    }

    public Set<String> getDestinos() {
        lock.lock();
        try {
            return destinos;
        } finally {
            lock.unlock();
        }
    }

    public boolean getEstado() {
        lock.lock();
        try {
            return estado;
        } finally {
            lock.unlock();
        }
    }

    public void setCusto(int custo) {
        lock.lock();
        try {
            this.custo = custo;
        } finally {
            lock.unlock();
        }
    }

    public void setOrigem(String origem) {
        lock.lock();
        try {
            this.origem = origem;
        } finally {
            lock.unlock();
        }
    }

    public void setDestinos(Set<String> destinos) {
        lock.lock();
        try {
            this.destinos = destinos;
        } finally {
            lock.unlock();
        }
    }

    public void setEstado(boolean estado) {
        lock.lock();
        try {
            this.estado = estado;
        } finally {
            lock.unlock();
        }
    }

    public void addDestino (String destino) {
        lock.lock();
        try {
            this.destinos.add(destino);
        } finally {
            lock.unlock();
        }
    }

    public void removeDestino (String destino) {
        lock.lock();
        try {
            this.destinos.remove(destino);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public String toString() {
        return "Rota{" +
                "custo=" + custo +
                ", origem='" + origem + '\'' +
                ", destinos=" + destinos +
                ", estado='" + estado + '\'' +
                '}';
    }
}
