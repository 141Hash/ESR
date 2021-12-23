package OTT;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Map;
import java.util.Set;

public class ThreadOTTPedidos extends Thread {

    private DatagramSocket ds;
    private RTPpacketQueue rtpQueue;
    private DadosNodo dadosNodo;

    public ThreadOTTPedidos(DatagramSocket ds, RTPpacketQueue rtpQueue, DadosNodo dadosNodo) {
        this.ds = ds;
        this.rtpQueue = rtpQueue;
        this.dadosNodo = dadosNodo;
    }

    public void run() {

        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
            String pedido = reader.readLine();

            while (!pedido.equals("EXIT")) {

                if (pedido.startsWith("PLAYER")) {
                    Thread threadCliente = new Thread(() -> { Cliente cli = new Cliente(ds, rtpQueue, dadosNodo); });
                    threadCliente.start();
                }

                System.out.println(pedido);
                pedido = reader.readLine();
            }

            for (String vizinho: this.dadosNodo.getIpsVizinhos()) {
                if (this.dadosNodo.getVizinho(vizinho) != null)
                    this.dadosNodo.getVizinho(vizinho).addMessagesToSend("Leaving#" + InetAddress.getLocalHost().getHostAddress() + "\n");
            }

            Thread.sleep(1000);
            OTT.EXIT = true;

            for (String vizinho: this.dadosNodo.getIpsVizinhos()) {
                if (this.dadosNodo.getVizinho(vizinho) != null)
                    this.dadosNodo.getVizinho(vizinho).getMessagesToSend().signalCon();
            }

            Runtime.getRuntime().halt(0);

        }
        catch (IOException | InterruptedException ignored) { }

    }

}
