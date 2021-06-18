import java.util.Arrays;

public class Cache {

    private final String type;
    private Set[] sets;

    public Cache(String type){
        this.type = type;
    }

    public void CreateSet(int size){
        sets = new Set[size];
        for(int i = 0; i < size; i++){
            sets[i] = new Set();
            sets[i].setIndex(i);
        }
    }

    public void setSets(Set[] sets) {
        this.sets = sets;
    }

    public String getType() {
        return type;
    }

    public Set[] getSets() {
        return sets;
    }

    @Override
    public String toString() {
        return "Cache type=" + type + "\n" + Arrays.toString(sets) + "\n";
    }
}
