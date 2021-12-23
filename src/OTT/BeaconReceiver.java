package OTT;

public class BeaconReceiver extends Thread {

    private DadosNodo dadosNodo;
    private String ipAdress;
    private boolean isBootstrapper;
    private RTPpacketQueue rtPpacketQueue;
    public static final long SLEEP_TIME = 15000;

    public BeaconReceiver(DadosNodo dadosNodo, String ipAdress, boolean isBootstrapper, RTPpacketQueue rtPpacketQueue) {
        this.dadosNodo = dadosNodo;
        this.ipAdress = ipAdress;
        this.isBootstrapper = isBootstrapper;
        this.rtPpacketQueue = rtPpacketQueue;
    }


    public void run () {
        while (true) {
            try {
                Thread.sleep(SLEEP_TIME);
                if (dadosNodo.removeIdle(isBootstrapper, ipAdress))
                    rtPpacketQueue.signalCon();
            } catch (InterruptedException ignored) { }
        }
    }

}
