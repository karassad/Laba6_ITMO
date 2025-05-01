package shared;

import java.io.Serializable;

/**
 * Class representing a response from server to client.
 */
public class Response implements Serializable {

    private final String responseText;

    public Response(Request responseText) {
        this.responseText = String.valueOf(responseText);
    }

    public String getResponseText() {
        return responseText;
    }
}
