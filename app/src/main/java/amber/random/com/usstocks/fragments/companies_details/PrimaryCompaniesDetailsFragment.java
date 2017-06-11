package amber.random.com.usstocks.fragments.companies_details;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import amber.random.com.usstocks.R;


public class PrimaryCompaniesDetailsFragment extends Fragment {
    private static final String sCURRENT_FILTER = "current_filter";
    private ViewPager mViewPager = null;

    public static PrimaryCompaniesDetailsFragment newInstance(String filter) {
        PrimaryCompaniesDetailsFragment fragment = new PrimaryCompaniesDetailsFragment();
        Bundle bundle = new Bundle();
        bundle.putString(sCURRENT_FILTER, filter);
        fragment.setArguments(bundle);
        return fragment;
    }

    private static String getFilter(Bundle bundle) {
        String result = null;
        if (bundle != null)
            result = bundle.getString(sCURRENT_FILTER);
        if (result == null)
            result = "";
        return result;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        String filter = getFilter(savedInstanceState);
        View view = inflater.inflate(R.layout.main_company_details_fragment, container, false);
        mViewPager = (ViewPager) view.findViewById(R.id.pager);
        mViewPager.setAdapter(new CompaniesDetailsPagerFragment(this, filter));
        TabLayout tabs = (TabLayout) view.findViewById(R.id.tabs);
        tabs.setupWithViewPager(mViewPager);
        return view;
    }

}
