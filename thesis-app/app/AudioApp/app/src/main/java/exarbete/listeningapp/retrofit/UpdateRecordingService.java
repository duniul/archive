package exarbete.listeningapp.retrofit;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

/**
 * Created by Daniel on 2016-05-05.
 */
public interface UpdateRecordingService {
    @FormUrlEncoded
    @POST("update_recording.php")
    Call<MessageResponse> updateRecording(@Field("recordingID") long recordingID,
                                          @Field("newFilename") String newFilename,
                                          @Field("lastEdited") String lastEdited);
}
