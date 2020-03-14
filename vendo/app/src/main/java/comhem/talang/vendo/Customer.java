package comhem.talang.vendo;

import android.text.Editable;

import java.io.Serializable;
import java.text.SimpleDateFormat;

/**
 * Created by Daniel on 2016-08-25.
 */
public class Customer implements Serializable {

    private long pid;
    private String firstName;
    private String lastName;
    private String address;
    private String postalArea;
    private String postalCode;
    private long dateRegistered;
    private long dateModified;
    private SimpleDateFormat dateFormat;

    public Customer(long pid, String firstName, String lastName, String address, String postalArea, String postalCode, long dateRegistered, long dateModified) {
        this.pid = pid;
        this.firstName = firstName;
        this.lastName = lastName;
        this.address = address;
        this.postalArea = postalArea;
        this.postalCode = postalCode;
        this.dateRegistered = dateRegistered;
        this.dateModified = dateModified;
        dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    }

    public long getPid() {
        return pid;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getAddress() {
        return address;
    }

    public String getPostalArea() {
        return postalArea;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public long getDateRegistered() {
        return dateRegistered;
    }

    public String getDateRegisteredAsString() {
        return dateFormat.format(dateRegistered);
    }

    public long getDateModified() {
        return dateModified;
    }

    public String getDateModifiedAsString() {
        return dateFormat.format(dateModified);
    }

    public void editInformation(String firstName, String lastName, String address, String postalArea, String postalCode) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.address = address;
        this.postalArea = postalArea;
        this.postalCode = postalCode;
        this.dateModified = System.currentTimeMillis();
    }
}
