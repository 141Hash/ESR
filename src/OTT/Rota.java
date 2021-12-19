package OTT;

import java.util.Set;
import java.util.TreeSet;

public class Rota {
    private int custo;
    private String origem;
    private Set<String> destinos;
    private boolean estado;

    public Rota() {
        this.custo = -1;
        this.origem = null;
        this.destinos = new TreeSet<>();
        this.estado = false;
    }

    public int getCusto() {
        return custo;
    }

    public String getOrigem() {
        return origem;
    }

    public Set<String> getDestinos() {
        return destinos;
    }

    public boolean getEstado() {
        return estado;
    }

    public void setCusto(int custo) {
        this.custo = custo;
    }

    public void setOrigem(String origem) {
        this.origem = origem;
    }

    public void setDestinos(Set<String> destinos) {
        this.destinos = destinos;
    }

    public void setEstado(boolean estado) {
        this.estado = estado;
    }

    public void addDestino (String destino) {
        this.destinos.add(destino);
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
