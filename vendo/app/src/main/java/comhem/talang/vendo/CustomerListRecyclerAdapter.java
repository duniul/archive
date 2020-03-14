package comhem.talang.vendo;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.graphics.Color;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by Daniel on 2016-08-25.
 */
public class CustomerListRecyclerAdapter extends RecyclerView.Adapter<CustomerListRecyclerAdapter.ViewHolder> {

    private static int colorCounter;

    private Activity activity;
    private List<Customer> customers;
    private OnCustomerClickListener listener;

    public interface OnCustomerClickListener {
        void onItemClick(Customer customer);
    }

    public void add(Customer customer, int position) {

        if (position == -1) {
            position = getItemCount();
        }

        customers.add(position, customer);
        notifyItemInserted(position);
    }

    public void remove(int position) {
        if (position < getItemCount()) {
            customers.remove(position);
            notifyItemRemoved(position);
        }
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        View itemColoring;
        TextView fullNameField;
        TextView pidField;
        TextView fullAddressField;

        public ViewHolder(View view) {
            super(view);
            itemColoring = (View) itemView.findViewById(R.id.customer_item_coloring);
            fullNameField = (TextView) itemView.findViewById(R.id.list_item_full_name);
            pidField = (TextView) itemView.findViewById(R.id.list_item_pid);
            fullAddressField = (TextView) itemView.findViewById(R.id.list_item_full_address);
        }

        public void bind(final Customer customer, final OnCustomerClickListener listener) {
            String fullName = customer.getFirstName() + " " + customer.getLastName();
            String unformattedPID = Long.toString(customer.getPid());
            String formattedPID = new StringBuilder(unformattedPID).insert(unformattedPID.length() - 4, "-").toString();
            String fullAddress = customer.getAddress() + ", " + customer.getPostalCode() + " " + customer.getPostalArea();

            itemColoring.setBackgroundColor(pickItemColor());
            fullNameField.setText(fullName);
            pidField.setText(formattedPID);
            fullAddressField.setText(fullAddress);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    listener.onItemClick(customer);
                }
            });
        }

        private int pickItemColor() {

            int color = 0;

            switch (colorCounter) {
                case 0:
                    color = Color.parseColor("#FFD401");
                    break;
                case 1:
                    color = Color.parseColor("#00A650");
                    break;
                case 2:
                    color = Color.parseColor("#00CEFF");
                    break;
                case 3:
                    color = Color.parseColor("#0971C6");
                    break;
                case 4:
                    color = Color.parseColor("#B27EE3");
                    break;
                case 5:
                    color = Color.parseColor("#FF70B6");
                    break;
                case 6:
                    color = Color.parseColor("#FF4627");
                    break;
                default:
                    color = Color.parseColor("#FFD401");
                    break;
            }

            colorCounter++;
            if (colorCounter > 6) {
                colorCounter = 0;
            }

            return color;
        }
    }

    public CustomerListRecyclerAdapter(Activity activity, List<Customer> customers, OnCustomerClickListener listener) {
        this.activity = activity;
        this.listener = listener;
        if (customers != null) {
            this.customers = customers;
        } else  {
            customers = new ArrayList<Customer>();
        }

        colorCounter = new Random().nextInt(6);
    }

    @Override
    public CustomerListRecyclerAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final View view = LayoutInflater.from(activity).inflate(R.layout.list_item_customer, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.bind(customers.get(position), listener);
    }

    @Override
    public int getItemCount() {
        return customers.size();
    }

}
