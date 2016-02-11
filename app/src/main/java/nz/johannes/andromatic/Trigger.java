package nz.johannes.andromatic;

public class Trigger {

    private String type;
    private String match;

    public Trigger(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }

    public String getMatch() {
        return match;
    }

    public void setMatch(String match) {
        this.match = match;
    }

}
