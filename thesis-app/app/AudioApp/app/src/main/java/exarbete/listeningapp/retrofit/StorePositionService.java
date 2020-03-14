package exarbete.listeningapp.retrofit;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

/**
 * Created by Daniel on 2016-05-04.
 */
public interface StorePositionService {
    @FormUrlEncoded
    @POST("store_position.php")
    Call<MessageResponse> storePosition(@Field("sessionID") long sessionID,
                                        @Field("datetime") String datetime,
                                        @Field("latitude") double latitude,
                                        @Field("longitude") double longitude);
}
