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

public class ThreadOTTReceiver extends Thread {
    private boolean isBootstrapper;
    private String ipOTT;
    private BufferedReader dis;
    private DadosNodo dadosNodo;
    private RTPpacketQueue rtPpacketQueue;

    public ThreadOTTReceiver(boolean isBootstrapper, String ipOTT, BufferedReader dis, DadosNodo dadosNodo, RTPpacketQueue rtPpacketQueue) {
        this.isBootstrapper = isBootstrapper;
        this.ipOTT = ipOTT;
        this.dis = dis;
        this.dadosNodo = dadosNodo;
        this.rtPpacketQueue = rtPpacketQueue;
    }

    public void adicionaMensagemControloVizinhos (String[] mensagemControlo) {

        try {

            ArrayList<String> historico = new ArrayList<>(Arrays.asList(mensagemControlo[2].split("-")));

            String ipOrigem = historico.get(historico.size() - 1);
            int nrSaltos    = Integer. parseInt(mensagemControlo[1]);

            if (dadosNodo.getRotaFluxo().getCusto() == -1 || dadosNodo.getRotaFluxo().getCusto() >= nrSaltos) {
                dadosNodo.getRotaFluxo().setCusto(nrSaltos);
                dadosNodo.getRotaFluxo().setOrigem(ipOrigem);
                for (String vizinho : this.dadosNodo.getIpsVizinhos()) {
                    if (!vizinho.equals(ipOrigem)) {
                        dadosNodo.getRotaFluxo().addDestinoVizinho(vizinho);
                    }
                }
                System.out.println(dadosNodo.getRotaFluxo().toString());
            }

            else if (!ipOrigem.equals(dadosNodo.getOrigemFluxo())) {
                String ipAdress = InetAddress.getLocalHost().getHostAddress();

                String dontUseMeAsDestiny = "DontUseMeAsDestiny#" + ipAdress + "\n";
                dadosNodo.getVizinho(ipOrigem).addMessagesToSend(dontUseMeAsDestiny);
            }

            dadosNodo.getRotaFluxo().removeDestino(dadosNodo.getOrigemFluxo());

            nrSaltos++;

            int counterFowards = 0;
            for (String vizinho : dadosNodo.getIpsVizinhos()) {
                if (!historico.contains(vizinho) && dadosNodo.getVizinho(vizinho) != null) {
                    String nextMessage = mensagemControlo[0] + "#" + nrSaltos + "#" + mensagemControlo[2] + "-" + ipOTT + "\n";
                    dadosNodo.getVizinho(vizinho).addMessagesToSend(nextMessage);
                    counterFowards++;
                }
            }

            if (counterFowards == 0) {
                String ipAdress = InetAddress.getLocalHost().getHostAddress();

                String totalDestinies = "TotalDestinies#" + ipAdress + "\n";
                dadosNodo.getVizinho(dadosNodo.getOrigemFluxo()).addMessagesToSend(totalDestinies);
            }

        } catch (Exception ignored) { }

    }

    private void adicionaDestinosTotais(String[] mensagemControlo) throws UnknownHostException {
        ArrayList<String> destinos = new ArrayList<>(Arrays.asList(mensagemControlo[1].split("-")));

        int nrDestinos = destinos.size();
        if (nrDestinos > 1) {
            String destino = destinos.get(nrDestinos - 1);
            for (int i = 0; i < nrDestinos - 1; i++) {
                dadosNodo.getRotaFluxo().addDestinoVizinhoToTotalDestinies(destino, destinos.get(i));
            }
        }

        if (dadosNodo.getOrigemFluxo() != null) {
            String ipAdress = InetAddress.getLocalHost().getHostAddress();
            String messageToOrigem = mensagemControlo[0] + "#" + mensagemControlo[1] + "-" + ipAdress + "\n";
            dadosNodo.getVizinho(dadosNodo.getOrigemFluxo()).addMessagesToSend(messageToOrigem);
        }

    }

    private void removeMeFromDestiny(String[] mensagemControlo) {

        String ipDestino = mensagemControlo[1];
        if (dadosNodo.getRotaFluxo().getDestinosVizinhos().containsKey(ipDestino))
            dadosNodo.removeDestinoRota(ipDestino);

    }

    public void enviaPedidoParaVerStream (String[] mensagemControlo) {
        String[] ips = mensagemControlo[1].split("-");
        String ipDestino = ips[ips.length-1];

        if (dadosNodo.getDestinosVizinhos().containsKey(ipDestino)) {
            dadosNodo.addDestinoQuerVerStream(ipDestino);
        }
        else {
            for (String vizinho : dadosNodo.getDestinosVizinhos().keySet()) {
                if (dadosNodo.getDestinosVizinhos().get(vizinho).contains(ipDestino)) {
                    dadosNodo.addDestinoQuerVerStream(vizinho);
                }
            }
        }

        if (dadosNodo.getOrigemFluxo() != null) {
            dadosNodo.getVizinho(dadosNodo.getOrigemFluxo()).addMessagesToSend(mensagemControlo[0] + "#" + mensagemControlo[1]  + "-" + this.ipOTT + "\n");
        }
        else {
            if (!this.isBootstrapper)
                System.out.println("Não é destino de nenhum nodo no momento");
        }
    }

    public void enviaPedidoParaPausarStream (String[] mensagemControlo) {
        String ipDestino = mensagemControlo[1];

        dadosNodo.removeDestinoQuerVerStream(ipDestino);

        if (dadosNodo.getDestinosQueremVerStream().size() == 0 && !this.isBootstrapper) {
            if (!OTT.querVerStream)
                dadosNodo.getVizinho(dadosNodo.getOrigemFluxo()).addMessagesToSend(mensagemControlo[0] + "#" + this.ipOTT + "\n");
        }
    }

    private void removeNodeThatLeft(String[] mensagemControlo) {
        if (mensagemControlo[1].equals(dadosNodo.getOrigemFluxo())) {
            dadosNodo.setRota(new Rota());
        }

        else if (dadosNodo.getDestinosVizinhos().containsKey(mensagemControlo[1])) {
            dadosNodo.removeDestinoRota(mensagemControlo[1]);

            dadosNodo.removeDestinoQuerVerStream(mensagemControlo[1]);
            if (dadosNodo.getDestinosQueremVerStream().size() == 0 && !this.isBootstrapper) {
                if (!OTT.querVerStream)
                    dadosNodo.getVizinho(dadosNodo.getOrigemFluxo()).addMessagesToSend("PauseVideo#" + this.ipOTT + "\n");
            }

        }

        dadosNodo.removeVizinho(mensagemControlo[1]);

        try {
            Thread.sleep(500);
            rtPpacketQueue.sionalIfEmpty();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }


    public void run () {
	    String line;
    	try {
    	    while (!OTT.EXIT) {
                line = dis.readLine();

                if (line != null) {

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
                        System.out.println("GetVideo");
                    }
                    else if (mensagemControlo.length == 2 && mensagemControlo[0].equals("PauseVideo")) {
                        enviaPedidoParaPausarStream(mensagemControlo);
                    }
                    else {
                        System.out.println("I don't know what to do");
                    }

                    System.out.println(dadosNodo.getRotaFluxo().toString());

                }

		    }
	    } catch (IOException e) {
    	    e.printStackTrace();
        }
    }


}
