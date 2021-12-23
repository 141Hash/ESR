package OTT;

import javax.xml.crypto.Data;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.*;

public class ThreadOTTReceiver extends Thread{
    private boolean isBootstrapper;
    private String ipOTT;
    private BufferedReader dis;
    private Socket s;
    private Map<String, DadosVizinho> vizinhos;
    private Rota rotaFluxo;
    private DatagramSocket ds;
    private PacketQueue pq;
    private Set<String> destinosQueremVerStream;

    public ThreadOTTReceiver(boolean isBootstrapper, String ipOTT, BufferedReader dis, Socket s, Map<String, DadosVizinho> vizinhos, Rota rotaFluxo, DatagramSocket ds, PacketQueue pq, Set<String> destinosQueremVerStream) {
        this.isBootstrapper = isBootstrapper;
        this.ipOTT = ipOTT;
        this.dis = dis;
        this.s = s;
        this.vizinhos = vizinhos;
        this.rotaFluxo = rotaFluxo;
        this.ds = ds;
        this.pq = pq;
        this.destinosQueremVerStream = destinosQueremVerStream;
    }

    public void adicionaMensagemControloVizinhos (String[] mensagemControlo) throws UnknownHostException {

        ArrayList<String> historico = new ArrayList<>(Arrays.asList(mensagemControlo[2].split("-")));

        String ipOrigem = historico.get(historico.size() - 1);
        int nrSaltos    = Integer. parseInt(mensagemControlo[1]);

        if (rotaFluxo.getCusto() == -1 || rotaFluxo.getCusto() > nrSaltos) {
            rotaFluxo.setCusto(nrSaltos);
            rotaFluxo.setOrigem(ipOrigem);
            for (String vizinho : this.vizinhos.keySet()) {
                if (!vizinho.equals(ipOrigem)) {
                    rotaFluxo.addDestinoVizinho(vizinho);
                }
            }
        }
        else if (!ipOrigem.equals(rotaFluxo.getOrigem())) {
            String ipAdress = InetAddress.getLocalHost().getHostAddress();

            String dontUseMeAsDestiny = "DontUseMeAsDestiny#" + ipAdress + "\n";
            this.vizinhos.get(ipOrigem).addMessagesToSend(dontUseMeAsDestiny);
        }

        System.out.println(rotaFluxo.toString());
        nrSaltos++;

        int counterFowards = 0;
        for (String vizinho : this.vizinhos.keySet()) {
            if (!historico.contains(vizinho) && this.vizinhos.get(vizinho) != null) {
                String nextMessage = mensagemControlo[0] + "#" + nrSaltos + "#" + mensagemControlo[2] + "-" + ipOTT + "\n";
                this.vizinhos.get(vizinho).addMessagesToSend(nextMessage);
                counterFowards++;
            }
        }

        if (counterFowards == 0) {
            String ipAdress = InetAddress.getLocalHost().getHostAddress();

            String totalDestinies = "TotalDestinies#" + ipAdress + "\n";
            this.vizinhos.get(this.rotaFluxo.getOrigem()).addMessagesToSend(totalDestinies);
        }

    }

    private void adicionaDestinosTotais(String[] mensagemControlo) throws UnknownHostException {
        ArrayList<String> destinos = new ArrayList<>(Arrays.asList(mensagemControlo[1].split("-")));

        int nrDestinos = destinos.size();
        if (nrDestinos > 1) {
            String destino = destinos.get(nrDestinos - 1);
            for (int i = 0; i < nrDestinos - 1; i++) {
                this.rotaFluxo.addDestinoVizinhoToTotalDestinies(destino, destinos.get(i));
            }
        }

        if (this.rotaFluxo.getOrigem() != null) {
            String ipAdress = InetAddress.getLocalHost().getHostAddress();
            String messageToOrigem = mensagemControlo[0] + "#" + mensagemControlo[1] + "-" + ipAdress + "\n";
            this.vizinhos.get(this.rotaFluxo.getOrigem()).addMessagesToSend(messageToOrigem);
        }

    }

    private void removeMeFromDestiny(String[] mensagemControlo) {

        String ipDestino = mensagemControlo[1];
        if (rotaFluxo.getDestinosVizinhos().containsKey(ipDestino))
            rotaFluxo.removeDestino(ipDestino);

    }

    public void enviaPedidoParaVerStream (String[] mensagemControlo) {
        String[] ips = mensagemControlo[1].split("-");
        String ipDestino = ips[ips.length-1];

        if (this.rotaFluxo.getDestinosVizinhos().containsKey(ipDestino)) {
            this.destinosQueremVerStream.add(ipDestino);
        }
        else {
            for (String vizinho : this.rotaFluxo.getDestinosVizinhos().keySet()) {
                if (this.rotaFluxo.getDestinosVizinhos().get(vizinho).contains(ipDestino)) {
                    this.destinosQueremVerStream.add(vizinho);
                }
            }
        }

        this.vizinhos.get(this.rotaFluxo.getOrigem()).addMessagesToSend(mensagemControlo[0] + "#" + mensagemControlo[1]  + "-" + this.ipOTT + "\n");
    }

    public void enviaPedidoParaPausarStream (String[] mensagemControlo) {
        String ipDestino = mensagemControlo[1];

        this.destinosQueremVerStream.remove(ipDestino);

        if (this.destinosQueremVerStream.size() == 0 && !this.isBootstrapper) {
            if (!OTT.querVerStream)
                this.vizinhos.get(this.rotaFluxo.getOrigem()).addMessagesToSend(mensagemControlo[0] + "#" + this.ipOTT + "\n");
        }
    }

    private void removeNodeThatLeft(String[] mensagemControlo) {
        if (mensagemControlo[1].equals(this.rotaFluxo.getOrigem())) {
            this.rotaFluxo = new Rota();
        }
        else if (this.rotaFluxo.getDestinosVizinhos().containsKey(mensagemControlo[1])) {
            this.rotaFluxo.removeDestino(mensagemControlo[1]);
        }
        this.vizinhos.remove(mensagemControlo[1]);
    }


    public void run () {
	    String line;
    	try {
    	    while (!OTT.EXIT) {
                line = dis.readLine();

                String[] mensagemControlo = line.split("#");

   		        if (mensagemControlo.length > 2 && mensagemControlo[0].equals("RouteControl")) {
   		            adicionaMensagemControloVizinhos(mensagemControlo);
                }
   		        else if (mensagemControlo.length == 2 && mensagemControlo[0].equals("TotalDestinies")) {
                    adicionaDestinosTotais(mensagemControlo);
                }
   		        else if (mensagemControlo.length == 2 && mensagemControlo[0].equals("DontUseMeAsDestiny")) {
   		            removeMeFromDestiny(mensagemControlo);
                }
                else if (mensagemControlo.length == 2 && mensagemControlo[0].equals("Leaving")) {
                    removeNodeThatLeft(mensagemControlo);
                }
   		        else if (mensagemControlo.length == 2 && mensagemControlo[0].equals("GetVideo")) {
   		            enviaPedidoParaVerStream(mensagemControlo);
                }
                else if (mensagemControlo.length == 2 && mensagemControlo[0].equals("PauseVideo")) {
                    enviaPedidoParaPausarStream(mensagemControlo);
                } else {

                }
   		        System.out.println(line);

		    }
	    } catch (IOException e){
        	System.out.println(e.getMessage());
        	e.printStackTrace();
        }
    }


}
