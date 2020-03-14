package comhem.talang.vendo;


import android.app.Activity;
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
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import comhem.talang.vendo.database.VendoSQLiteHelper;
import comhem.talang.vendo.retrofit.BasicResponse;
import comhem.talang.vendo.retrofit.EditCustomerService;
import comhem.talang.vendo.retrofit.ServiceGenerator;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EditCustomerFragment extends Fragment {

    private static final String CUSTOMER_EDIT_KEY = "customerEditKey";
    private Customer customer;
    private EditText firstNameField;
    private EditText lastNameField;
    private EditText addressField;
    private EditText postalAreaField;
    private EditText postalCodeField;

    public EditCustomerFragment() {
        // Required empty public constructor
    }

    public static EditCustomerFragment newInstance(Customer customer) {
        EditCustomerFragment fragment = new EditCustomerFragment();
        Bundle bundle = new Bundle();
        bundle.putSerializable(CUSTOMER_EDIT_KEY, customer);
        fragment.setArguments(bundle);
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
        View fragmentView = inflater.inflate(R.layout.fragment_edit_customer, container, false);

        customer = (Customer) getArguments().getSerializable(CUSTOMER_EDIT_KEY);

        ((MainActivity) getActivity()).setNavigationIcon(R.drawable.ic_arrow_back);

        EditText pidField = (EditText) fragmentView.findViewById(R.id.edit_pid);
        String unformattedPID = Long.toString(customer.getPid());
        String formattedPID = new StringBuilder(unformattedPID).insert(unformattedPID.length() - 4, "-").toString();
        pidField.setText(formattedPID);

        firstNameField = (EditText) fragmentView.findViewById(R.id.edit_first_name);
        firstNameField.setText(customer.getFirstName());

        lastNameField = (EditText) fragmentView.findViewById(R.id.edit_last_name);
        lastNameField.setText(customer.getLastName());

        addressField = (EditText) fragmentView.findViewById(R.id.edit_address);
        addressField.setText(customer.getAddress());

        postalCodeField = (EditText) fragmentView.findViewById(R.id.edit_postal_code);
        postalCodeField.setText(customer.getPostalCode());

        postalAreaField = (EditText) fragmentView.findViewById(R.id.edit_postal_area);
        postalAreaField.setText(customer.getPostalArea());

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
                    .setTitle("Redigera kund")
                    .setMessage("Spara ändringar?")
                    .setPositiveButton("Nej", null)
                    .setNegativeButton("Ja", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            customer.editInformation(
                                    firstNameField.getText().toString(),
                                    lastNameField.getText().toString(),
                                    addressField.getText().toString(),
                                    postalAreaField.getText().toString(),
                                    postalCodeField.getText().toString());

                            EditCustomerService editCustomerService = ServiceGenerator.createService(EditCustomerService.class);
                            Call<BasicResponse> editCustomersCall = editCustomerService.editCustomer(
                                    customer.getPid(),
                                    customer.getFirstName(),
                                    customer.getLastName(),
                                    customer.getAddress(),
                                    customer.getPostalArea(),
                                    customer.getPostalCode(),
                                    customer.getDateModifiedAsString());

                            editCustomersCall.enqueue(new Callback<BasicResponse>() {
                                @Override
                                public void onResponse(Call<BasicResponse> call, Response<BasicResponse> response) {
                                    BasicResponse basicResponse = response.body();
                                    if (response.isSuccessful()) {
                                        VendoSQLiteHelper.getInstance().setOnlyLocallyModified(customer.getPid(), false);

                                    } else {
                                        VendoSQLiteHelper.getInstance().setOnlyLocallyModified(customer.getPid(), true);
                                    }
                                }

                                @Override
                                public void onFailure(Call<BasicResponse> call, Throwable t) {
                                    VendoSQLiteHelper.getInstance().setOnlyLocallyModified(customer.getPid(), true);
                                }
                            });

                            VendoSQLiteHelper.getInstance().editCustomer(customer, true);

                            FragmentManager manager = getActivity().getSupportFragmentManager();
                            FragmentTransaction transaction = manager.beginTransaction();
                            transaction.remove(EditCustomerFragment.this);
                            transaction.commit();
                            manager.popBackStack();
                            ((MainActivity) getActivity()).setNavigationIcon(R.drawable.ic_arrow_back);

                            InputMethodManager inputMethodManager = (InputMethodManager) getActivity().getSystemService(Activity.INPUT_METHOD_SERVICE);
                            inputMethodManager.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(), 0);

                            manager.executePendingTransactions();
                            Snackbar.make(manager.findFragmentByTag(MainActivity.FRAGMENT_TAG_CUSTOMER_PROFILE).getView().findViewById(R.id.customer_profile_coordinator),
                                    customer.getFirstName() + " " + customer.getLastName() + " har redigerats.",
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
