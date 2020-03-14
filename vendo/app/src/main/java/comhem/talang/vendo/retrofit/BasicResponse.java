package comhem.talang.vendo.retrofit;

/**
 * Created by Daniel on 2016-08-25.
 */
public class BasicResponse {

    private String tag;
    private boolean error;
    private String message;
    private long pid;

    public BasicResponse(String tag, boolean error, String message) {
        this.tag = tag;
        this.error = error;
        this.message = message;
    }

    public String getTag() {
        return tag;
    }

    public boolean isError() {
        return error;
    }

    public String getMessage() {
        return message;
    }
}
