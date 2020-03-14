package exarbete.listeningapp.retrofit;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

/**
 * Created by Daniel on 2016-05-03.
 */
public interface StoreListeningSessionService {
    @FormUrlEncoded
    @POST("store_listening_session.php")
    Call<MessageResponse> storeListeningSession(@Field("userID") long userID,
                                                @Field("sessionID") long sessionID,
                                                @Field("startTime") String startTime,
                                                @Field("endTime") String endTime);
}
