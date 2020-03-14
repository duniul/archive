package comhem.talang.vendo;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.text.SimpleDateFormat;

import comhem.talang.vendo.database.VendoSQLiteHelper;
import comhem.talang.vendo.retrofit.BasicResponse;
import comhem.talang.vendo.retrofit.DeleteCustomerService;
import comhem.talang.vendo.retrofit.GetCustomersResponse;
import comhem.talang.vendo.retrofit.GetCustomersService;
import comhem.talang.vendo.retrofit.ServiceGenerator;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CustomerProfileFragment extends Fragment {

    private static final String CUSTOMER_KEY = "customerKey";
    private Customer customer;

    public CustomerProfileFragment() {
        // Required empty public constructor
    }

    public static CustomerProfileFragment newInstance(Customer customer) {
        CustomerProfileFragment fragment = new CustomerProfileFragment();
        Bundle bundle = new Bundle();
        bundle.putSerializable(CUSTOMER_KEY, customer);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View fragmentView = inflater.inflate(R.layout.fragment_customer_profile, container, false);

        ((MainActivity) getActivity()).setNavigationIcon(R.drawable.ic_arrow_back);

        customer = (Customer) getArguments().getSerializable(CUSTOMER_KEY);

        TextView customerFullName = (TextView) fragmentView.findViewById(R.id.customer_full_name);
        customerFullName.setText(customer.getFirstName() + " " + customer.getLastName());

        TextView customerPID = (TextView) fragmentView.findViewById(R.id.customer_pid);
        String unformattedPID = Long.toString(customer.getPid());
        String formattedPID = new StringBuilder(unformattedPID).insert(unformattedPID.length() - 4, "-").toString();
        customerPID.setText(formattedPID);

        TextView customerFullAddress = (TextView) fragmentView.findViewById(R.id.customer_full_address);
        String fullAddress = customer.getAddress() + ", " + customer.getPostalCode() + " " + customer.getPostalArea();
        customerFullAddress.setText(fullAddress);

        TextView customerRegisteredDate = (TextView) fragmentView.findViewById(R.id.customer_registered_date);
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        customerRegisteredDate.setText(dateFormat.format(customer.getDateRegistered()));

        // Inflate the layout for this fragment
        return fragmentView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_customer_profile, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_item_edit:
                EditCustomerFragment editCustomerFragment = EditCustomerFragment.newInstance(customer);
                FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction()
                        .setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left, R.anim.slide_in_left, R.anim.slide_out_right);
                transaction.replace(R.id.main_content_frame, editCustomerFragment, MainActivity.FRAGMENT_TAG_EDIT_CUSTOMER);
                transaction.addToBackStack(null);
                transaction.commit();

                return true;
            case R.id.menu_item_delete:
                new AlertDialog.Builder(getActivity())
                        .setTitle("Radera kund")
                        .setMessage("Är du säker på att du vill radera den här kunden?")
                        .setPositiveButton("Nej", null)
                        .setNegativeButton("Ja", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                DeleteCustomerService deleteCustomerService = ServiceGenerator.createService(DeleteCustomerService.class);
                                Call<BasicResponse> getCustomersCall = deleteCustomerService.deleteCustomer(customer.getPid());
                                getCustomersCall.enqueue(new Callback<BasicResponse>() {
                                    @Override
                                    public void onResponse(Call<BasicResponse> call, Response<BasicResponse> response) {
                                        BasicResponse basicResponse = response.body();
                                        if (response.isSuccessful()) {


                                        } else {

                                        }
                                    }

                                    @Override
                                    public void onFailure(Call<BasicResponse> call, Throwable t) {

                                    }
                                });

                                VendoSQLiteHelper.getInstance().deleteCustomer(customer.getPid());

                                FragmentManager manager = getActivity().getSupportFragmentManager();
                                FragmentTransaction transaction = manager.beginTransaction();
                                transaction.remove(CustomerProfileFragment.this);
                                transaction.commit();
                                manager.popBackStack();
                                ((MainActivity)

                                        getActivity()

                                ).

                                        setNavigationIcon(R.drawable.ic_business);

                                manager.executePendingTransactions();
                                Snackbar.make(manager.findFragmentByTag(MainActivity.FRAGMENT_TAG_CUSTOMER_LIST).

                                                getView()

                                                .

                                                        findViewById(R.id.customer_list_fab),

                                        customer.getFirstName() + " " + customer.getLastName() + " har raderats.",
                                        Snackbar.LENGTH_SHORT)
                                        .

                                                show();
                            }

                        })
                        .show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
