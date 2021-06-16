import java.util.Arrays;

public class Set {

    private int index;
    private Line[] lines;

    public Set() {
    }

    public Set(int index) {
        this.index = index;
    }

    public void CreateLine(int size) {
        lines = new Line[size];
        for (int i = 0; i < size; i++) {
            lines[i] = new Line();
        }
    }

    public int GetMinTime() {
        int min = Integer.MAX_VALUE;
        int index = 0;
        for (int i = 0; i < lines.length; i++) {
            if (min > lines[i].getTime()) {
                min = lines[i].getTime();
                index = i;
            }
        }
        return index;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public Line[] getLines() {
        return lines;
    }

    public void setLinesIndex(Line line, int index) {
        lines[index] = line;
    }

    @Override
    public String toString() {
        return "Set index=" + index + "\n" +
                "\t\tlines=" + Arrays.toString(lines);
    }
}
