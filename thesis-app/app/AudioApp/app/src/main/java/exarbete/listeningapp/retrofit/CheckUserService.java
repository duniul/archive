package exarbete.listeningapp.retrofit;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

/**
 * Created by Daniel on 2016-05-02.
 */
public interface CheckUserService {
    @FormUrlEncoded
    @POST("check_user.php")
    Call<CheckUserResponse> checkUser(@Field("googleID") String googleID, @Field("email") String userEmail);
}