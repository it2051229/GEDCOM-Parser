
import java.util.ArrayList;
import java.util.List;

public class Family {
    public String id = "";
    public String marriageDate = "";
    
    public Individual husband = null;
    public Individual wife = null;
    
    public List<Individual> children = new ArrayList<Individual>();
    public String marriagePlace = "";
}
