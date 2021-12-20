package OTT;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.Socket;
import java.util.*;

public class ThreadOTTReceiver extends Thread{
    private String ipOTT;
    private BufferedReader dis;
    private Socket s;
    private Map<String, DadosVizinho> vizinhos;
    private Rota rotaFluxo;

    public ThreadOTTReceiver(String ipOTT, BufferedReader dis, Socket s, Map<String, DadosVizinho> vizinhos, Rota rotaFluxo) {
        this.ipOTT = ipOTT;
        this.dis = dis;
        this.s = s;
        this.vizinhos = vizinhos;
        this.rotaFluxo = rotaFluxo;
    }

    public void adicionaMensagemControloVizinhos (String[] mensagemControlo) {

        ArrayList<String> historico = new ArrayList<>(Arrays.asList(mensagemControlo[2].split("-")));

        String ipOrigem = historico.get(historico.size() - 1);
        int nrSaltos    = Integer. parseInt(mensagemControlo[1]);

        if (rotaFluxo.getCusto() == -1 || rotaFluxo.getCusto() > nrSaltos) {
            rotaFluxo.setCusto(nrSaltos);
            rotaFluxo.setOrigem(ipOrigem);
            for (String vizinho : this.vizinhos.keySet()) {
                if (!vizinho.equals(ipOrigem)) {
                    rotaFluxo.addDestino(vizinho);
                }
            }
        } else {
            String dontUseMeAsDestiny = "DontUseMeAsDestiny#" + ipOrigem + "\n";
            this.vizinhos.get(ipOrigem).getMessagesToSend().add(dontUseMeAsDestiny);
        }

        System.out.println(rotaFluxo.toString());
        nrSaltos++;

        for (String vizinho : this.vizinhos.keySet()) {
            if (!historico.contains(vizinho) && this.vizinhos.get(vizinho) != null) {
                String nextMessage = mensagemControlo[0] + "#" + nrSaltos + "#" + mensagemControlo[2] + "-" + ipOTT + "\n";
                this.vizinhos.get(vizinho).getMessagesToSend().add(nextMessage);
            }
        }

    }

    private void removeMeFromDestiny(String[] mensagemControlo) {

        String ipDestino = mensagemControlo[1];
        if (rotaFluxo.getDestinos().contains(ipDestino))
            rotaFluxo.removeDestino(ipDestino);

    }


    public void run () {
	    String line;
    	try {       
   		    while ((line = dis.readLine()) != null) {

                String[] mensagemControlo = line.split("#");
   		        if (mensagemControlo.length > 2 && mensagemControlo[0].equals("RouteControl")) {
   		            adicionaMensagemControloVizinhos(mensagemControlo);
                }
   		        else if (mensagemControlo.length == 2 && mensagemControlo[0].equals("DontUseMeAsDestiny")) {
   		            removeMeFromDestiny(mensagemControlo);
                }
   		        System.out.println(line);

		    }
	    } catch(IOException e){
        	System.out.println(e.getMessage());
        }
    }

}
