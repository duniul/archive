package comhem.talang.vendo.retrofit;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

/**
 * Created by Daniel on 2016-08-25.
 */
public interface GetCustomersService {

    public static final int ALL = 1;

    @FormUrlEncoded
    @POST("get_customers.php")
    Call<GetCustomersResponse> getCustomers(@Field("event") int event);
}
