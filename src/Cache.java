import java.util.ArrayList;

public class Cache {

    private final String type;
    private ArrayList<Set> sets = new ArrayList<Set>();

    public Cache(String type){
        this.type = type;
    }

    public void Append(Set set){
        sets.add(set);
    }

    public String getType() {
        return type;
    }

    public ArrayList<Set> getSets() {
        return sets;
    }

    @Override
    public String toString() {
        return "Cache type=" + type + "\n" +
                "\tsets=" + sets + "\n";
    }
}
