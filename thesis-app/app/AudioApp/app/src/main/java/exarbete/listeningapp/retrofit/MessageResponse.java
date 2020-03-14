package exarbete.listeningapp.retrofit;

/**
 * Created by Daniel on 2016-05-02.
 */
public class MessageResponse {

    private String tag;
    private boolean error;
    private String message;

    public MessageResponse(String tag, boolean error, String message) {
        this.tag = tag;
        this.error = error;
        this.message = message;
    }

    public String getTag() {
        return tag;
    }

    public boolean getError() {
        return error;
    }

    public String getMessage() {
        return message;
    }
}
