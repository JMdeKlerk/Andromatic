package nz.johannes.wifiler;

public class Action {

    private String state;
    private String command;
    private String data;

    public Action(String state, String command, String data) {
        this.state = state;
        this.command = command;
        this.data = data;
    }

    public String getCommand() {
        return this.command;
    }

    public String getData() {
        return this.data;
    }

    public String requiredState() {
        return this.state;
    }

    public static void doAction(Action action) {
        switch (action.getCommand()) {
            case ("SET_VOLUME"):
                break;
            case ("SEND_MESSAGE"):
                break;
        }
    }

}
