import java.util.ArrayList;

public class Set {

    private final int index;
    private ArrayList<Line> lines = new ArrayList<Line>();

    public Set(int index){
        this.index = index;
    }

    public void Append(Line line){
        lines.add(line);
    }

    public int getIndex() {
        return index;
    }

    public ArrayList<Line> getLines() {
        return lines;
    }

    @Override
    public String toString() {
        return "Set index=" + index + "\n" +
                "\t\tlines=" + lines;
    }
}
