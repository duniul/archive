package exarbete.listeningapp.retrofit;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

/**
 * Created by Daniel on 2016-05-09.
 */
public interface DeleteRecordingService {
    @FormUrlEncoded
    @POST("delete_recording.php")
    Call<MessageResponse> deleteRecording(@Field("recordingID") long recordingID,
                                          @Field("url") String url);
}
