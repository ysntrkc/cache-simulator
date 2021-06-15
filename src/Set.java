import java.util.Arrays;

public class Set {

    private int index;
    private Line[] lines;

    public Set(){

    }

    public Set(int index){
        this.index = index;
    }

    public void CreateLine(int size){
        lines = new Line[size];
        for(int i = 0; i < size; i++){
            lines[i] = new Line();
        }
    }

    public int getIndex() {
        return index;
    }

    public Line[] getLines() {
        return lines;
    }

    @Override
    public String toString() {
        return "Set index=" + index + "\n" +
                "\t\tlines=" + Arrays.toString(lines);
    }
}
