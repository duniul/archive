package exarbete.listeningapp.retrofit;

/**
 * Created by Daniel on 2016-05-03.
 */
public class CheckUserResponse {

    private String tag;
    private boolean error;
    private String message;
    private long userID;

    public CheckUserResponse(String tag, boolean error, String message, long userID) {
        this.tag = tag;
        this.error = error;
        this.message = message;
        this.userID = userID;
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

    public long getUserID() {
        return userID;
    }
}
