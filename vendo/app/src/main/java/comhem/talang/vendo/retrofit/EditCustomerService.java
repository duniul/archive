package comhem.talang.vendo.retrofit;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

/**
 * Created by Daniel on 2016-08-25.
 */
public interface EditCustomerService {
    @FormUrlEncoded
    @POST("edit_customer.php")
    Call<BasicResponse> editCustomer(@Field("pid") long pid,
                                     @Field("firstName") String firstName,
                                     @Field("lastName") String lastName,
                                     @Field("address") String address,
                                     @Field("postalArea") String postalArea,
                                     @Field("postalCode") String postalCode,
                                     @Field("dateModified") String dateModified);
}
