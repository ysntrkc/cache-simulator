public class Line {

    private boolean valid;
    private String tag;
    private String data;
    private int time;

    public Line() {
    }

    public Line(String tag, boolean valid, String data, int time) {
        this.tag = tag;
        this.valid = valid;
        this.data = data;
        this.time = time;
    }

    public boolean isValid() {
        return valid;
    }

    public void setValid(boolean valid) {
        this.valid = valid;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public int getTime() {
        return time;
    }

    public void setTime(int time){
        this.time = time;
    }

    @Override
    public String toString() {
        return "Line \n" +
                "\t\t\tvalid=" + valid + "\n" +
                "\t\t\ttag=" + tag + "\n" +
                "\t\t\tdata" + data + "\n" +
                "\t\t\ttime=" + time + "\n";
    }
}

