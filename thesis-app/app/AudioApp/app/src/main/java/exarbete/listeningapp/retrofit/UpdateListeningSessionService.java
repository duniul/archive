package exarbete.listeningapp.retrofit;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

/**
 * Created by Daniel on 2016-05-05.
 */
public interface UpdateListeningSessionService {
    @FormUrlEncoded
    @POST("update_listening_session.php")
    Call<MessageResponse> updateListeningSession(@Field("sessionID") long sessionID,
                                                 @Field("endTime") String endTime);
}
