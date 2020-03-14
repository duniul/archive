package comhem.talang.vendo.retrofit;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

/**
 * Created by Daniel on 2016-08-26.
 */
public interface DeleteCustomerService {

    @FormUrlEncoded
    @POST("delete_customers.php")
    Call<BasicResponse> deleteCustomer(@Field("pid") long pid);
}
