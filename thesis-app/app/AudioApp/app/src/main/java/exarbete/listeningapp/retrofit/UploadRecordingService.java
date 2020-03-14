package exarbete.listeningapp.retrofit;

import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

/**
 * Created by Daniel on 2016-05-02.
 */
public interface UploadRecordingService {
    @Multipart
    @POST("upload_recording.php")
    Call<UploadResponse> upload(@Part("userID") long userID,
                                @Part("sessionID") long sessionID,
                                @Part("recordingID") long recordingID,
                                @Part("filename") String filename,
                                @Part("userFilename") String userFilename,
                                @Part("recordingStartDate") String recordingStartDate,
                                @Part("recordingEndDate") String recordingEndDate,
                                @Part("duration") int duration,
                                @Part("lastEdited") String lastEdited,
                                @Part("file\"; filename=\"recording\" ") RequestBody file);
}