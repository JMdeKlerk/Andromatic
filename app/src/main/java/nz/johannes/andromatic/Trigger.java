package nz.johannes.andromatic;

import java.util.ArrayList;

public class Trigger {

    private String type;
    private String match;
    private ArrayList<String> extraData;

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

    public ArrayList<String> getExtraData() {
        return extraData;
    }

    public void setExtraData(ArrayList data) {
        this.extraData = data;
    }

}
