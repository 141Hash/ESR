package OTT;

import java.time.LocalTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;

public class DadosNodo {
    public Map<String, DadosVizinho> vizinhos;
    public Rota rotaFluxo;
    public Set<String> destinosQueremVerStream;
    private ReentrantLock lockVizinhos;
    private ReentrantLock lockRota;
    private ReentrantLock lockDestinosStream;

    public DadosNodo(Map<String,DadosVizinho> vizinhos, Rota rotaFluxo, Set<String> destinosQueremVerStream) {
        this.vizinhos                = vizinhos;
        this.rotaFluxo               = rotaFluxo;
        this.destinosQueremVerStream = destinosQueremVerStream;
        this.lockVizinhos       = new ReentrantLock();
        this.lockRota           = new ReentrantLock();
        this.lockDestinosStream = new ReentrantLock();
    }

    public void addVizinho (String vizinho, DadosVizinho dadosVizinho) {
        lockVizinhos.lock();
        try {
            this.vizinhos.put(vizinho, dadosVizinho);
        } finally {
            lockVizinhos.unlock();
        }
    }

    public void removeVizinho (String vizinho) {
        lockVizinhos.lock();
        try {
            this.vizinhos.remove(vizinho);
        } finally {
            lockVizinhos.unlock();
        }
    }

    public Set<String> getIpsVizinhos () {
        lockVizinhos.lock();
        try {
            return this.vizinhos.keySet();
        } finally {
            lockVizinhos.unlock();
        }
    }

    public DadosVizinho getVizinho (String vizinho) {
        lockVizinhos.lock();
        try {
            return this.vizinhos.get(vizinho);
        } finally {
            lockVizinhos.unlock();
        }
    }

    public String getOrigemFluxo () {
        lockRota.lock();
        try {
            return this.rotaFluxo.getOrigem();
        } finally {
            lockRota.unlock();
        }
    }

    public Set<String> getDestinosQueremVerStream () {
        lockDestinosStream.lock();
        try {
            return this.destinosQueremVerStream;
        } finally {
            lockDestinosStream.unlock();
        }
    }

    public void updateTime(String ip) {
        lockVizinhos.lock();
        try {
            vizinhos.get(ip).updateTime();
        } finally {
            lockVizinhos.unlock();
        }
    }

    public boolean removeIdle (boolean isBootstrapper, String ipAdress) {
        lockVizinhos.lock();
        lockRota.lock();
        lockDestinosStream.lock();
        try {
            List<String> vizinhosToRemove = new ArrayList<>();

            for (String vizinho : vizinhos.keySet()) {
                if (vizinhos.get(vizinho) != null)
                    if (vizinhos.get(vizinho).getLastUpdate().until(LocalTime.now(ZoneId.of("UTC")), ChronoUnit.SECONDS) > (BeaconReceiver.SLEEP_TIME / 1000))
                        vizinhosToRemove.add(vizinho);
            }

            for (String ip : vizinhosToRemove) {
                if (rotaFluxo.getOrigem() != null && rotaFluxo.getOrigem().equals(ip)) {
                        rotaFluxo = new Rota();
                }
                else if (rotaFluxo.getDestinosVizinhos().containsKey(ip)) {
                    rotaFluxo.removeDestino(ip);

                    this.destinosQueremVerStream.remove(ip);
                    if (this.destinosQueremVerStream.size() == 0 && !isBootstrapper) {
                        if (!OTT.querVerStream)
                            this.vizinhos.get(this.rotaFluxo.getOrigem()).addMessagesToSend("PauseVideo#" + ipAdress + "\n");
                    }

                }

                this.vizinhos.remove(ip);
                System.out.println("Vizinho " + ip + " removido por idle\n");
            }

            return (vizinhosToRemove.size() > 0);
        } finally {
            lockVizinhos.unlock();
            lockRota.unlock();
            lockDestinosStream.unlock();
        }
    }

    public Rota getRotaFluxo () {
        lockRota.lock();
        try {
            return this.rotaFluxo;
        } finally {
            lockRota.unlock();
        }
    }

    public void addDestinoRota(String ipDestino) {
        lockRota.lock();
        try {
            this.rotaFluxo.addDestinoVizinho(ipDestino);
        } finally {
            lockRota.unlock();
        }
    }


    public void removeDestinoRota(String ipDestino) {
        lockRota.lock();
        try {
            this.rotaFluxo.removeDestino(ipDestino);
        } finally {
            lockRota.unlock();
        }
    }

    public Map<String, Set<String>> getDestinosVizinhos() {
        lockRota.lock();
        try {
            return this.rotaFluxo.getDestinosVizinhos();
        } finally {
            lockRota.unlock();
        }
    }

    public void addDestinoQuerVerStream(String ipDestino) {
        lockDestinosStream.lock();
        try {
            this.destinosQueremVerStream.add(ipDestino);
        } finally {
            lockDestinosStream.unlock();
        }
    }

    public void removeDestinoQuerVerStream(String ipDestino) {
        lockDestinosStream.lock();
        try {
            this.destinosQueremVerStream.remove(ipDestino);
        } finally {
            lockDestinosStream.unlock();
        }
    }

    public void setRota (Rota rotaFluxo) {
        lockRota.lock();
        try {
            this.rotaFluxo = rotaFluxo;
        } finally {
            lockRota.unlock();
        }
    }

    public void addMessagesToAll(String message) {
        lockVizinhos.lock();
        try {
            for (DadosVizinho dadosVizinho : this.vizinhos.values()) {
                dadosVizinho.addMessagesToSend(message);
            }
        } finally {
            lockVizinhos.unlock();
        }
    }

    @Override
    public String toString() {
        return "DadosNodo{" +
                "vizinhos=" + vizinhos.toString() +
                ", rotaFluxo=" + rotaFluxo.toString() +
                ", destinosQueremVerStream=" + destinosQueremVerStream.toString() +
                '}';
    }
}
