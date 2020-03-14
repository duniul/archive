package exarbete.listeningapp.retrofit;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

/**
 * Created by Daniel on 2016-05-09.
 */
public interface DeleteSessionService {
    @FormUrlEncoded
    @POST("delete_session.php")
    Call<MessageResponse> deleteSession(@Field("sessionID") long sessionID);
}