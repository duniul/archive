package comhem.talang.vendo.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import comhem.talang.vendo.Customer;
import comhem.talang.vendo.retrofit.BasicResponse;
import comhem.talang.vendo.retrofit.EditCustomerService;
import comhem.talang.vendo.retrofit.GetCustomersResponse;
import comhem.talang.vendo.retrofit.GetCustomersService;
import comhem.talang.vendo.retrofit.RegisterCustomerService;
import comhem.talang.vendo.retrofit.ServiceGenerator;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by Daniel on 2016-08-25.
 */
public class VendoSQLiteHelper extends SQLiteOpenHelper {

    private static final String TAG = VendoSQLiteHelper.class.getSimpleName();

    //Variables for columns and values
    public static final int DATABASE_VERSION = 4;
    public static final String DATABASE_NAME = "VendoLocalDatabase";
    public static final String TABLE_CUSTOMER = "tableCustomer";
    public static final String KEY_PID = "pid";
    public static final String KEY_FIRST_NAME = "firstName";
    public static final String KEY_LAST_NAME = "lastName";
    public static final String KEY_ADDRESS = "address";
    public static final String KEY_POSTAL_AREA = "postalArea";
    public static final String KEY_POSTAL_CODE = "postalCode";
    public static final String KEY_DATE_REGISTERED = "dateRegistered";
    public static final String KEY_DATE_MODIFIED = "dateModified";
    public static final String KEY_ONLY_LOCALLY_MODIFIED = "onlyLocallyModified";

    private static VendoSQLiteHelper instance;
    private static SQLiteDatabase database;

    private VendoSQLiteHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        database = getWritableDatabase();
    }

    public static synchronized void initialize(Context context) {
        if (instance == null) {
            instance = new VendoSQLiteHelper(context);
        }
    }

    public static synchronized VendoSQLiteHelper getInstance() {
        if (instance == null) {
            throw new IllegalStateException(TAG +
                    " is not initialized, call initialize method first.");
        }

        return instance;
    }

    @Override
    public void onCreate(SQLiteDatabase db){
        String createCustomerTableQuery = "CREATE TABLE " + TABLE_CUSTOMER + "("
                + KEY_PID + " INTEGER PRIMARY KEY,"
                + KEY_FIRST_NAME + " INTEGER,"
                + KEY_LAST_NAME + " TEXT,"
                + KEY_ADDRESS + " TEXT,"
                + KEY_POSTAL_AREA + " TEXT,"
                + KEY_POSTAL_CODE + " TEXT,"
                + KEY_DATE_REGISTERED + " INTEGER,"
                + KEY_DATE_MODIFIED + " INTEGER,"
                + KEY_ONLY_LOCALLY_MODIFIED + " INTEGER)";

        db.execSQL(createCustomerTableQuery);

        Log.d(TAG, "Database table created.");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        // Delete old tables
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CUSTOMER);

        // Recreate tables
        onCreate(db);
    }

    public SQLiteDatabase getDatabase() {
        return database;
    }

    public int getNumberOfCustomers() {

        String sqlQuery = "SELECT * FROM " + TABLE_CUSTOMER;

        Cursor cursor = database.rawQuery(sqlQuery, null);
        int numberOfCustomers = cursor.getCount();
        cursor.close();

        return numberOfCustomers;
    }

    public Customer getCustomer(long pid) {

        String sqlQuery = "SELECT * " +
                          "FROM " + TABLE_CUSTOMER + " " +
                          "WHERE " + KEY_PID + "=" + pid;

        Cursor cursor = database.rawQuery(sqlQuery, null);
        cursor.moveToFirst();

        String firstName = cursor.getString(1);
        String lastName = cursor.getString(2);
        String address = cursor.getString(3);
        String postalArea = cursor.getString(4);
        String postalCode = cursor.getString(5);
        long dateRegistered = cursor.getLong(6);
        long dateModified = cursor.getLong(7);

        return new Customer(pid, firstName, lastName, address, postalArea, postalCode, dateRegistered, dateModified);
    }

    public List<Customer> getAllCustomers() {

        List<Customer> allCustomers = new ArrayList<Customer>();

        String sqlQuery = "SELECT * FROM " + TABLE_CUSTOMER;

        Cursor cursor = database.rawQuery(sqlQuery, null);
        cursor.moveToFirst();

        if (cursor.getCount() > 0) {
            do {
                long pid = cursor.getLong(0);
                String firstName = cursor.getString(1);
                String lastName = cursor.getString(2);
                String address = cursor.getString(3);
                String postalArea = cursor.getString(4);
                String postalCode = cursor.getString(5);
                long dateRegistered = cursor.getLong(6);
                long dateModified = cursor.getLong(7);

                allCustomers.add(new Customer(pid, firstName, lastName, address, postalArea, postalCode, dateRegistered, dateModified));

            } while(cursor.moveToNext());
        }

        cursor.close();

        return allCustomers;
    }

    public List<Customer> getOnlyLocallyChangedCustomers() {

        List<Customer> locallyChangedCustomers = new ArrayList<Customer>();

        String sqlQuery = "SELECT * " +
                          "FROM " + TABLE_CUSTOMER + " " +
                          "WHERE " + KEY_ONLY_LOCALLY_MODIFIED + "=" + 1;

        Cursor cursor = database.rawQuery(sqlQuery, null);
        cursor.moveToFirst();

        if (cursor.getCount() > 0) {
            do {
                long pid = cursor.getLong(0);
                String firstName = cursor.getString(1);
                String lastName = cursor.getString(2);
                String address = cursor.getString(3);
                String postalArea = cursor.getString(4);
                String postalCode = cursor.getString(5);
                long dateRegistered = cursor.getLong(6);
                long dateModified = cursor.getLong(7);

                locallyChangedCustomers.add(new Customer(pid, firstName, lastName, address, postalArea, postalCode, dateRegistered, dateModified));

            } while(cursor.moveToNext());
        }

        cursor.close();

        return locallyChangedCustomers;
    }

    public boolean isOnlyLocallyModified(long pid) {
        String sqlQuery = "SELECT " + KEY_ONLY_LOCALLY_MODIFIED +
                          "FROM " + TABLE_CUSTOMER + " " +
                          "WHERE " + KEY_PID + "=" + pid;

        Cursor cursor = database.rawQuery(sqlQuery, null);
        cursor.moveToFirst();

        if(cursor.getInt(0) == 1) {
            return true;
        } else {
            return false;
        }
    }

    public void setOnlyLocallyModified(long pid, boolean state) {
        ContentValues values = new ContentValues();
        if(state) {
            values.put(KEY_ONLY_LOCALLY_MODIFIED, 1);
        } else {
            values.put(KEY_ONLY_LOCALLY_MODIFIED, 0);
        }

        database.update(TABLE_CUSTOMER, values, KEY_PID + "=" + pid, null);
    }

    public void storeCustomer(Customer customer, boolean onlyLocallyModified){

        ContentValues values = new ContentValues();
        values.put(KEY_PID, customer.getPid());
        values.put(KEY_FIRST_NAME, customer.getFirstName());
        values.put(KEY_LAST_NAME, customer.getLastName());
        values.put(KEY_ADDRESS, customer.getAddress());
        values.put(KEY_POSTAL_AREA, customer.getPostalArea());
        values.put(KEY_POSTAL_CODE, customer.getPostalCode());
        values.put(KEY_DATE_REGISTERED, customer.getDateRegistered());
        values.put(KEY_DATE_MODIFIED, customer.getDateModified());

        if(onlyLocallyModified) {
            values.put(KEY_ONLY_LOCALLY_MODIFIED, 1);
        } else {
            values.put(KEY_ONLY_LOCALLY_MODIFIED, 0);
        }

        database.insert(TABLE_CUSTOMER, null, values);

        Log.d(TAG, "New customer added to SQLite database with PID: " + customer.getPid());
    }

    public void editCustomer(Customer customer, boolean onlyLocallyModified) {

        ContentValues values = new ContentValues();
        values.put(KEY_FIRST_NAME, customer.getFirstName());
        values.put(KEY_LAST_NAME, customer.getLastName());
        values.put(KEY_ADDRESS, customer.getAddress());
        values.put(KEY_POSTAL_AREA, customer.getPostalArea());
        values.put(KEY_POSTAL_CODE, customer.getPostalCode());
        values.put(KEY_DATE_MODIFIED, customer.getDateModified());

        if(onlyLocallyModified) {
            values.put(KEY_ONLY_LOCALLY_MODIFIED, 1);
        } else {
            values.put(KEY_ONLY_LOCALLY_MODIFIED, 0);
        }

        database.update(TABLE_CUSTOMER, values, KEY_PID + "=" + customer.getPid(), null);

        Log.d(TAG, "Customer with PID " + customer.getPid() + " was edited.");

    }

    public void deleteCustomer(long pid) {
        String sqlQuery = "DELETE FROM " + TABLE_CUSTOMER +
                          " WHERE " + KEY_PID + " = " + pid;

        database.execSQL(sqlQuery);
    }

    public void syncDatabaseWithServer() {
        final List<Customer> onlyLocallyChangedCustomers = getOnlyLocallyChangedCustomers();

        GetCustomersService getCustomersService = ServiceGenerator.createService(GetCustomersService.class);
        Call<GetCustomersResponse> getCustomersCall = getCustomersService.getCustomers(GetCustomersService.ALL);
        getCustomersCall.enqueue(new Callback<GetCustomersResponse>() {
            @Override
            public void onResponse(Call<GetCustomersResponse> call, Response<GetCustomersResponse> response) {
                GetCustomersResponse getCustomersResponse = response.body();
                if (response.isSuccessful()) {
                    Log.i("RetrofitTest", "SUCCESSFUL");
                    Log.i("RetrofitTest", "Response: " + getCustomersResponse.getMessage());
                    List<Customer> serverCustomers = getCustomersResponse.getCustomers();
                    List<Customer> synchedCustomerList = new ArrayList<Customer>();
                    for (Customer serverCustomer : serverCustomers) {
                        for(final Customer localCustomer : onlyLocallyChangedCustomers) {
                            if (localCustomer.getPid() == serverCustomer.getPid()) {
                                if (serverCustomer.getDateModified() < localCustomer.getDateModified()) {
                                    serverCustomer = localCustomer;
                                    setOnlyLocallyModified(localCustomer.getPid(), false);

                                    EditCustomerService editCustomerService = ServiceGenerator.createService(EditCustomerService.class);
                                    Call<BasicResponse> editCustomersCall = editCustomerService.editCustomer(
                                            localCustomer.getPid(),
                                            localCustomer.getFirstName(),
                                            localCustomer.getLastName(),
                                            localCustomer.getAddress(),
                                            localCustomer.getPostalArea(),
                                            localCustomer.getPostalCode(),
                                            localCustomer.getDateModifiedAsString());

                                    editCustomersCall.enqueue(new Callback<BasicResponse>() {
                                        @Override
                                        public void onResponse(Call<BasicResponse> call, Response<BasicResponse> response) {
                                            BasicResponse basicResponse = response.body();
                                            if (response.isSuccessful()) {
                                                VendoSQLiteHelper.getInstance().setOnlyLocallyModified(localCustomer.getPid(), false);

                                            } else {
                                                VendoSQLiteHelper.getInstance().setOnlyLocallyModified(localCustomer.getPid(), true);
                                            }
                                        }

                                        @Override
                                        public void onFailure(Call<BasicResponse> call, Throwable t) {
                                            VendoSQLiteHelper.getInstance().setOnlyLocallyModified(localCustomer.getPid(), true);
                                        }
                                    });
                                }

                                onlyLocallyChangedCustomers.remove(localCustomer);

                                break;
                            }
                        }

                        serverCustomers.remove(serverCustomer);
                        synchedCustomerList.add(serverCustomer);
                    }

                    if (!serverCustomers.isEmpty()) {
                        for (Customer customer : serverCustomers) {
                            storeCustomer(customer, false);
                        }
                    }

                    if (!onlyLocallyChangedCustomers.isEmpty()) {
                        for (final Customer customer : onlyLocallyChangedCustomers) {
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
                        }
                    }

                    database.execSQL("DROP TABLE IF EXISTS " + TABLE_CUSTOMER);
                    onCreate(database);

                    for (Customer customer : serverCustomers) {
                        storeCustomer(customer, false);
                    }

                } else {
                    Log.i("RetrofitTest", "NOT SUCCESSFUL");
                    Log.i("RetrofitTest", "Response: " + getCustomersResponse);
                }
            }

            @Override
            public void onFailure(Call<GetCustomersResponse> call, Throwable t) {

            }
        });
    }

}