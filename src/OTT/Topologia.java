package OTT;

import java.util.HashMap;
import java.util.Set;

public class Topologia {
    private HashMap<String, Set<String>> topologia;

    public Topologia(HashMap<String, Set<String>> topologia) {
        this.topologia = topologia;
    }

    public HashMap<String, Set<String>> getTopologia() {
        return topologia;
    }

    public void setTopologia(HashMap<String, Set<String>> topologia) {
        this.topologia = topologia;
    }

    public Set<String> getVizinhos (String ipAdress) {
        return  this.topologia.get(ipAdress);
    }
}
