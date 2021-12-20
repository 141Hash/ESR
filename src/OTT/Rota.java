package OTT;

import java.util.HashMap;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.locks.ReentrantLock;

public class Rota {
    private int custo;
    private String origem;
    private HashMap<String, Set<String>> destinosVizinhos;
    private boolean estado;
    private ReentrantLock lock;

    public Rota() {
        this.custo = -1;
        this.origem = null;
        this.destinosVizinhos = new HashMap<>();
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

    public HashMap<String, Set<String>> getDestinosVizinhos() {
        lock.lock();
        try {
            return destinosVizinhos;
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

    public void setDestinos(HashMap<String, Set<String>> destinosVizinhos) {
        lock.lock();
        try {
            this.destinosVizinhos = destinosVizinhos;
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

    public void addDestinoVizinho (String destino) {
        lock.lock();
        try {
            this.destinosVizinhos.put(destino, null);
        } finally {
            lock.unlock();
        }
    }

    public void removeDestino (String destino) {
        lock.lock();
        try {
            this.destinosVizinhos.remove(destino);
        } finally {
            lock.unlock();
        }
    }

    public void addDestinoVizinhoToTotalDestinies (String destino, String proximoVizinho) {
        lock.lock();
        try {
            this.destinosVizinhos.get(destino).add(proximoVizinho);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public String toString() {
        return "Rota{" +
                "custo=" + custo +
                ", origem='" + origem + '\'' +
                ", destinos=" + destinosVizinhos.toString() +
                ", estado='" + estado + '\'' +
                '}';
    }
}
