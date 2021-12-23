package OTT;

import java.time.LocalTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Map;

public class BeaconReceiver extends Thread {

    private Map<String, DadosVizinho> vizinhos;
    public static final long SLEEP_TIME = 15000;

    public BeaconReceiver(Map<String, DadosVizinho> vizinhos) {
        this.vizinhos = vizinhos;
    }

    private void removeIdle(Map<String,DadosVizinho> vizinhos) {
        ArrayList<String> vizinhosToRemove = new ArrayList<>();

        for (String vizinho : this.vizinhos.keySet()) {
            if (this.vizinhos.get(vizinho) != null)
                if (this.vizinhos.get(vizinho).getLastUpdate().until(LocalTime.now(ZoneId.of("UTC")), ChronoUnit.SECONDS)>(BeaconReceiver.SLEEP_TIME / 1000))
                    vizinhosToRemove.add(vizinho);
        }

        for(String ip : vizinhosToRemove) {
            this.vizinhos.remove(ip);
            System.out.println("Vizinho " + ip + " removido por idle\n");
        }
    }

    public void run () {
        try {
            Thread.sleep(SLEEP_TIME);
            removeIdle(vizinhos);
        } catch (InterruptedException ignored) { }
    }

}
