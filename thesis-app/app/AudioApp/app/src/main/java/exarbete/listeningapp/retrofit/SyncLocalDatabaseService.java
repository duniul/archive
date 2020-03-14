package exarbete.listeningapp.retrofit;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

/**
 * Created by Daniel on 2016-05-04.
 */
public interface SyncLocalDatabaseService {
    @FormUrlEncoded
    @POST("sync_local_database.php")
    Call<SyncLocalDatabaseResponse> sync(@Field("userID") long userID);
}
