package comhem.talang.vendo;

import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.WindowManager;

import comhem.talang.vendo.database.VendoSQLiteHelper;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();
    protected static final String FRAGMENT_TAG_CUSTOMER_LIST = "fragmentCustomerList";
    protected static final String FRAGMENT_TAG_CUSTOMER_PROFILE = "fragmentCustomerProfile";
    protected static final String FRAGMENT_TAG_EDIT_CUSTOMER = "fragmentEditCustomer";
    protected static final String FRAGMENT_TAG_REGISTER_CUSTOMER = "fragmentRegisterCustomer";

    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Sets up the toolbar.
        toolbar = (Toolbar) findViewById(R.id.main_activity_toolbar);
        setSupportActionBar(toolbar);
        setNavigationIcon(R.drawable.ic_business);

        VendoSQLiteHelper.initialize(MainActivity.this);

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

        if (savedInstanceState == null) {
            CustomerListFragment customerListFragment = new CustomerListFragment();
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.add(R.id.main_content_frame, customerListFragment, FRAGMENT_TAG_CUSTOMER_LIST).commit();
        }
    }

    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case android.R.id.home:
                FragmentManager fragmentManager = getSupportFragmentManager();
                if (fragmentManager.getBackStackEntryCount() > 0) {
                    fragmentManager.popBackStack();
                    setNavigationIcon(R.drawable.ic_business);
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        if (fragmentManager.getBackStackEntryCount() > 0) {
            setNavigationIcon(R.drawable.ic_business);
        }
        super.onBackPressed();
    }

    public void setNavigationIcon(int iconID) {
        if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
            toolbar.setNavigationIcon(ContextCompat.getDrawable(this, iconID));
        } else {
            toolbar.setNavigationIcon(ContextCompat.getDrawable(this, iconID));
        }
    }

}
