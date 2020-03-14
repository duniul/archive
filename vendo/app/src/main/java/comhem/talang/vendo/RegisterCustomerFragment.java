package comhem.talang.vendo;


import android.app.Activity;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import java.util.List;

import comhem.talang.vendo.database.VendoSQLiteHelper;
import comhem.talang.vendo.retrofit.BasicResponse;
import comhem.talang.vendo.retrofit.RegisterCustomerService;
import comhem.talang.vendo.retrofit.ServiceGenerator;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegisterCustomerFragment extends Fragment {

    private EditText pidField;
    private EditText firstNameField;
    private EditText lastNameField;
    private EditText addressField;
    private EditText postalAreaField;
    private EditText postalCodeField;

    public RegisterCustomerFragment() {
        // Required empty public constructor
    }

    public static RegisterCustomerFragment newInstance() {
        RegisterCustomerFragment fragment = new RegisterCustomerFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View fragmentView = inflater.inflate(R.layout.fragment_register_customer, container, false);

        ((MainActivity) getActivity()).setNavigationIcon(R.drawable.ic_arrow_back);

        pidField = (EditText) fragmentView.findViewById(R.id.edit_pid);

        firstNameField = (EditText) fragmentView.findViewById(R.id.edit_first_name);
        lastNameField = (EditText) fragmentView.findViewById(R.id.edit_last_name);
        addressField = (EditText) fragmentView.findViewById(R.id.edit_address);
        postalCodeField = (EditText) fragmentView.findViewById(R.id.edit_postal_code);
        postalAreaField = (EditText) fragmentView.findViewById(R.id.edit_postal_area);

        return fragmentView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_edit_customer, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == R.id.menu_item_confirm) {

            new AlertDialog.Builder(getActivity())
                    .setTitle("Spara kund")
                    .setMessage("Vill du spara den här kunden?")
                    .setPositiveButton("Nej", null)
                    .setNegativeButton("Ja", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            long currentTime = System.currentTimeMillis();

                            final Customer customer = new Customer(
                                    Long.parseLong(pidField.getText().toString()),
                                    firstNameField.getText().toString(),
                                    lastNameField.getText().toString(),
                                    addressField.getText().toString(),
                                    postalAreaField.getText().toString(),
                                    postalCodeField.getText().toString(),
                                    currentTime,
                                    currentTime);


                            RegisterCustomerService registerCustomerService = ServiceGenerator.createService(RegisterCustomerService.class);
                            Call<BasicResponse> registerCustomerCall = registerCustomerService.registerCustomer(customer.getPid(),
                                    customer.getFirstName(),
                                    customer.getLastName(),
                                    customer.getAddress(),
                                    customer.getPostalArea(),
                                    customer.getPostalCode(),
                                    customer.getDateRegisteredAsString(),
                                    customer.getDateModifiedAsString());

                            registerCustomerCall.enqueue(new Callback<BasicResponse>() {
                                @Override
                                public void onResponse(Call<BasicResponse> call, Response<BasicResponse> response) {
                                    BasicResponse basicResponse = response.body();
                                    if (response.isSuccessful()) {
                                        Log.i("TEST1", basicResponse.getMessage());
                                        VendoSQLiteHelper.getInstance().setOnlyLocallyModified(customer.getPid(), false);

                                    } else {
                                        Log.i("TEST2", "hit kommer vi");
                                        VendoSQLiteHelper.getInstance().setOnlyLocallyModified(customer.getPid(), true);
                                    }
                                }

                                @Override
                                public void onFailure(Call<BasicResponse> call, Throwable t) {
                                    Log.i("TEST3", "hit kommer vi");
                                    VendoSQLiteHelper.getInstance().setOnlyLocallyModified(customer.getPid(), true);
                                }
                            });


                            VendoSQLiteHelper.getInstance().storeCustomer(customer, true);

                            FragmentManager manager = getActivity().getSupportFragmentManager();
                            FragmentTransaction transaction = manager.beginTransaction();
                            transaction.remove(RegisterCustomerFragment.this);
                            transaction.commit();
                            manager.popBackStack();
                            ((MainActivity) getActivity()).setNavigationIcon(R.drawable.ic_business);

                            InputMethodManager inputMethodManager = (InputMethodManager) getActivity().getSystemService(Activity.INPUT_METHOD_SERVICE);
                            inputMethodManager.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(), 0);

                            manager.executePendingTransactions();
                            Snackbar.make(manager.findFragmentByTag(MainActivity.FRAGMENT_TAG_CUSTOMER_LIST).getView().findViewById(R.id.customer_list_coordinator),
                                    customer.getFirstName() + " " + customer.getLastName() + " har sparats.",
                                    Snackbar.LENGTH_SHORT)
                                    .show();
                        }
                    })
                    .show();

            return true;

        } else {

            return super.onOptionsItemSelected(item);
        }
    }

}
