package livechords.livechordsjava.Model;

public class TextViewComponentUpdaterCommand {
    private int id;
    private int command;
    private Object parameter;

    public TextViewComponentUpdaterCommand(int id, int command, String parameter){
        this.id = id;
        this.command = command;
        this.parameter = parameter;
    }

    public int getId() {
        return id;
    }

    public int getCommand() {
        return command;
    }

    public Object getParameter() {
        return parameter;
    }
}
