package OTT;

import java.time.LocalTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;

public class BeaconReceiver extends Thread {

    private Map<String, DadosVizinho> vizinhos;
    private Rota rotaFluxo;
    private Set<String> destinosQueremVerStream;
    private String ipAdress;
    private boolean isBootstrapper;
    public static final long SLEEP_TIME = 15000;

    public BeaconReceiver(Map<String, DadosVizinho> vizinhos, Rota rotaFluxo, Set<String> destinosQueremVerStream, String ipAdress, boolean isBootstrapper) {
        this.vizinhos = vizinhos;
        this.rotaFluxo = rotaFluxo;
        this.destinosQueremVerStream = destinosQueremVerStream;
        this.ipAdress = ipAdress;
        this.isBootstrapper = isBootstrapper;
    }

    private void removeIdle() {
        ArrayList<String> vizinhosToRemove = new ArrayList<>();

        for (String vizinho : this.vizinhos.keySet()) {
            if (this.vizinhos.get(vizinho) != null)
                if (this.vizinhos.get(vizinho).getLastUpdate().until(LocalTime.now(ZoneId.of("UTC")), ChronoUnit.SECONDS) > (BeaconReceiver.SLEEP_TIME / 1000))
                    vizinhosToRemove.add(vizinho);
        }

        for (String ip : vizinhosToRemove) {
            if (this.rotaFluxo.getOrigem() != null) {
                if (this.rotaFluxo.getOrigem().equals(ip)) {
                    this.rotaFluxo = new Rota();
                }
            }

            else if (this.rotaFluxo.getDestinosVizinhos().containsKey(ip)) {
                this.rotaFluxo.removeDestino(ip);

                this.destinosQueremVerStream.remove(ip);
                if (this.destinosQueremVerStream.size() == 0 && !this.isBootstrapper) {
                    if (!OTT.querVerStream)
                        this.vizinhos.get(this.rotaFluxo.getOrigem()).addMessagesToSend("PauseVideo#" + this.ipAdress + "\n");
                }

            }

            this.vizinhos.remove(ip);
            System.out.println("Vizinho " + ip + " removido por idle\n");
        }
    }

    public void run () {
        while (true) {
            try {
                Thread.sleep(SLEEP_TIME);
                removeIdle();
            } catch (InterruptedException ignored) { }
        }
    }

}
