package shared;

import java.io.Serializable;

public class Message implements Serializable {

    private String text;

    public Message(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }


    public void setText(String text) {
        this.text = text;
    }

    @Override
    public String toString() {
        return String.format("Message from client:\n ", text);
    }

}
