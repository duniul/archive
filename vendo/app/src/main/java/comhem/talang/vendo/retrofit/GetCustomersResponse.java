package comhem.talang.vendo.retrofit;

import java.util.List;

import comhem.talang.vendo.Customer;

/**
 * Created by Daniel on 2016-08-25.
 */
public class GetCustomersResponse {

    private String tag;
    private boolean error;
    private String message;
    private List<Customer> customers;

    public GetCustomersResponse(String tag, boolean error, String message, List<Customer> customers) {
        this.tag = tag;
        this.error = error;
        this.message = message;
        this.customers = customers;
    }

    public String getTag() {
        return tag;
    }

    public boolean isError() {
        return error;
    }

    public String getMessage() {
        return message;
    }

    public List<Customer> getCustomers() {
        return customers;
    }
}
