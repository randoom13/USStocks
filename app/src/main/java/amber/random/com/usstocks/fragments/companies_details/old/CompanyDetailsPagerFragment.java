package amber.random.com.usstocks.fragments.companies_details.old;


import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import amber.random.com.usstocks.fragments.companies_details.CompaniesDetailsFragment;


public class CompanyDetailsPagerFragment extends FragmentStatePagerAdapter {
    private final int[] mCompanies_ids;
    public CompanyDetailsPagerFragment(FragmentManager fm, int[] companies_ids) {
        super(fm);
        mCompanies_ids = companies_ids;
    }

    @Override
    public Fragment getItem(int position) {
        return CompaniesDetailsFragment.newInstance(mCompanies_ids[position]);
    }

    @Override
    public int getCount() {
        return mCompanies_ids.length;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return String.valueOf(mCompanies_ids[position]);
    }
}
