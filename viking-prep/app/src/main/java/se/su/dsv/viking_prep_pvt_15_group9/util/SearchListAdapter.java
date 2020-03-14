package se.su.dsv.viking_prep_pvt_15_group9.util;

import android.app.Activity;
import android.app.FragmentManager;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;

import java.util.List;

import se.su.dsv.viking_prep_pvt_15_group9.MainActivity;
import se.su.dsv.viking_prep_pvt_15_group9.R;
import se.su.dsv.viking_prep_pvt_15_group9.SelectedProfileFragment;
import se.su.dsv.viking_prep_pvt_15_group9.helper.AppRequestManager;
import se.su.dsv.viking_prep_pvt_15_group9.helper.SQLiteManager;
import se.su.dsv.viking_prep_pvt_15_group9.model.Person;

/**
 * Created by Daniel on 2015-05-28.
 */
public class SearchListAdapter extends BaseAdapter implements AdapterView.OnItemClickListener {
    private Activity mainActivity;
    private LayoutInflater inflater;
    private List<Person> people;
    private SQLiteManager localDatabase;
    private ImageLoader imageLoader = AppRequestManager.getInstance().getImageLoader();

    public SearchListAdapter(Activity mainActivity, List<Person> people) {
        this.mainActivity = mainActivity;
        this.people = people;

        localDatabase = ((MainActivity) mainActivity).getLocalDatabase();
    }

    @Override
    public int getCount() {
        return people.size();
    }

    @Override
    public Object getItem(int location) {
        return people.get(location);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if (inflater == null) {
            inflater = (LayoutInflater) mainActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.list_row_search, null);
        }

        if (imageLoader == null) {
            imageLoader = AppRequestManager.getInstance().getImageLoader();
        }

        NetworkImageView thumbnail = (NetworkImageView) convertView.findViewById(R.id.thumbnail);
        TextView fullName = (TextView) convertView.findViewById(R.id.full_name);
        TextView location = (TextView) convertView.findViewById(R.id.location);
        TextView age = (TextView) convertView.findViewById(R.id.age);

        // getting person data for the row
        Person p = people.get(position);

        thumbnail.setImageUrl(p.getPictureUrl(), AppRequestManager.getInstance().getImageLoader());
        fullName.setText(p.getFullName());
        location.setText(p.getLocation());
        age.setText(Integer.toString(p.getAge()) + " år");

        return convertView;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        FragmentManager fragmentManager = mainActivity.getFragmentManager();

        Person clickedPerson = people.get(position);
        int userID = clickedPerson.getUserID();
        String uniqueID = clickedPerson.getUniqueID();
        String name = clickedPerson.getName();
        String surname = clickedPerson.getSurname();
        String email = clickedPerson.getEmail();
        String area = clickedPerson.getArea();
        String city = clickedPerson.getCity();
        String dateOfBirth = clickedPerson.getDateOfBirth();
        String sex = clickedPerson.getSex();
        String createdAt = clickedPerson.getCreatedAt();
        String updatedAt = clickedPerson.getUpdatedAt();
        String pictureUrl = clickedPerson.getPictureUrl();

        localDatabase.deleteUser(2);
        localDatabase.addUser(2, userID, uniqueID, name, surname, email, area, city, dateOfBirth, sex, createdAt, updatedAt, pictureUrl);

        fragmentManager.beginTransaction()
                .replace(R.id.content_frame, SelectedProfileFragment.newInstanceOf()).addToBackStack("")
                .commit();

    }

    public void setPeopleList(List<Person> list) {
        people = list;
    }
}