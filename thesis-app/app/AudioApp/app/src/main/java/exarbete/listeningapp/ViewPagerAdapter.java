package exarbete.listeningapp;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * This class handles paging between fragments views in an activity.
 */
public class ViewPagerAdapter extends FragmentPagerAdapter {

    private final List<Fragment> fragments = new ArrayList<>();
    private final List<String> fragmentLabels = new ArrayList<>();

    /**
     * Instantiates a new View pager adapter.
     *
     * @param manager FragmentManager used by the adapter.
     */
    public ViewPagerAdapter(FragmentManager manager) {
        super(manager);
    }

    /**
     * Adds a fragment to the fragment list.
     *
     * @param fragment the fragment
     * @param label    the label of the fragment
     */
    public void addFragment(Fragment fragment, String label) {
        fragments.add(fragment);
        fragmentLabels.add(label);
    }

    @Override
    public Fragment getItem(int position) {
        return fragments.get(position);
    }

    @Override
    public int getCount() {
        return fragments.size();
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return fragmentLabels.get(position);
    }
}
