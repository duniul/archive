package comhem.talang.vendo;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import comhem.talang.vendo.database.VendoSQLiteHelper;

public class CustomerListFragment extends Fragment {

    private RecyclerView customerListRecyclerView;
    private CustomerListRecyclerAdapter customerListRecyclerAdapter;
    private List<Customer> customerList;
    private List<Customer> searchResultsList;
    private List<Customer> currentList;
    private SwipeRefreshLayout swipeRefreshLayout;
    private TextView noCustomersText;
    private FloatingActionButton registerFAB;


    public CustomerListFragment() {
        // Required empty public constructor
    }

    public static CustomerListFragment newInstance() {
        CustomerListFragment fragment = new CustomerListFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        VendoSQLiteHelper.getInstance().syncDatabaseWithServer();

        customerList = VendoSQLiteHelper.getInstance().getAllCustomers();
        searchResultsList = new ArrayList<Customer>();
        currentList = new ArrayList<Customer>();
        currentList.addAll(customerList);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View fragmentView = inflater.inflate(R.layout.fragment_customer_list, container, false);

        // Sets up the recycler view to be used in the navigation drawer.
        customerListRecyclerView = (RecyclerView) fragmentView.findViewById(R.id.customer_list_recycler);
        customerListRecyclerView.setHasFixedSize(true);

        // Create OnClickListener for customers.
        CustomerListRecyclerAdapter.OnCustomerClickListener customerClickListener = new CustomerListRecyclerAdapter.OnCustomerClickListener() {
            @Override
            public void onItemClick(Customer customer) {
                CustomerProfileFragment customerProfileFragment = CustomerProfileFragment.newInstance(customer);
                FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction()
                        .setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left, R.anim.slide_in_left, R.anim.slide_out_right);
                transaction.replace(R.id.main_content_frame, customerProfileFragment, MainActivity.FRAGMENT_TAG_CUSTOMER_PROFILE);
                transaction.addToBackStack(null);
                transaction.commit();
            }
        };

        // Attach adapter.
        customerListRecyclerAdapter = new CustomerListRecyclerAdapter(getActivity(), currentList, customerClickListener);
        customerListRecyclerView.setAdapter(customerListRecyclerAdapter);

        // Adds a layout manager to the recycler view.
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity());
        customerListRecyclerView.setLayoutManager(layoutManager);
        customerListRecyclerView.addItemDecoration(new CustomerListRecyclerDivider(getActivity())) ;

        swipeRefreshLayout = (SwipeRefreshLayout) fragmentView.findViewById(R.id.customer_list_swipe_refresh);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshCustomerList();
            }
        });

        registerFAB = (FloatingActionButton) fragmentView.findViewById(R.id.customer_list_fab);
        OnFabClickListener onFabClickListener = new OnFabClickListener();
        registerFAB.setOnClickListener(onFabClickListener);

        noCustomersText = (TextView) fragmentView.findViewById(R.id.no_customers_text);
        refreshCustomerList();

        // Inflate the layout for this fragment
        return fragmentView;
    }

    private void refreshCustomerList() {

        customerList.clear();
        customerList.addAll(VendoSQLiteHelper.getInstance().getAllCustomers());

        currentList.clear();
        currentList.addAll(customerList);

        if (customerListRecyclerAdapter.getItemCount() <= 0) {
            noCustomersText.setVisibility(View.VISIBLE);
        } else {
            noCustomersText.setVisibility(View.INVISIBLE);
        }

        customerListRecyclerAdapter.notifyDataSetChanged();
        swipeRefreshLayout.setRefreshing(false);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_customer_list, menu);

        MenuItem searchItem = menu.findItem(R.id.menu_item_search);
        SearchView searchView = new SearchView(getActivity());
        searchView.setQueryHint("Sök efter kunder...");
        EditText searchEditText = (EditText) searchView.findViewById(android.support.v7.appcompat.R.id.search_src_text);
        searchEditText.setTextColor(ContextCompat.getColor(getActivity(), R.color.icons));
        searchEditText.setHintTextColor(ContextCompat.getColor(getActivity(), R.color.primaryDark));

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return true;
            }

            @Override
            public boolean onQueryTextChange(String query) {
                customerSearch(query);
                return true;
            }
        });
        searchItem.setActionView(searchView);

        super.onCreateOptionsMenu(menu,inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_item_refresh:
                refreshCustomerList();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private class OnFabClickListener implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            RegisterCustomerFragment registerCustomerFragment = RegisterCustomerFragment.newInstance();
            FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction()
                    .setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left, R.anim.slide_in_left, R.anim.slide_out_right);
            transaction.replace(R.id.main_content_frame, registerCustomerFragment, MainActivity.FRAGMENT_TAG_REGISTER_CUSTOMER);
            transaction.addToBackStack(null);
            transaction.commit();

            ((MainActivity) getActivity()).setNavigationIcon(R.drawable.ic_arrow_back);
        }
    }

    private void customerSearch(String query) {

        List<Long> savedPIDs = new ArrayList<Long>();

        searchResultsList.clear();
        currentList.clear();
        customerListRecyclerAdapter.notifyDataSetChanged();

        for (Customer u : customerList) {
            if (Long.toString(u.getPid()).toLowerCase().contains(query.toLowerCase())
                    || (u.getFirstName() + " " + u.getLastName()).toLowerCase().contains(query.toLowerCase())
                    || (u.getAddress() + ", " + u.getPostalCode() + " " + u.getPostalArea()).toLowerCase().contains(query.toLowerCase())) {

                if(!savedPIDs.contains(u.getPid())) {
                    savedPIDs.add(u.getPid());
                    searchResultsList.add(u);
                }
            }
        }

        currentList.addAll(searchResultsList);
        customerListRecyclerAdapter.notifyDataSetChanged();

    }
}
