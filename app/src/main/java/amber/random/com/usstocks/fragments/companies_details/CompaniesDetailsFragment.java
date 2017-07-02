package amber.random.com.usstocks.fragments.companies_details;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import amber.random.com.usstocks.fragments.base.BaseContractListFragment;


public class CompaniesDetailsFragment extends BaseContractListFragment<CompaniesDetailsFragment.Contract> {
    public static final String COMPANY_ID = "company_id";
    private int mCompanyId;

    public static CompaniesDetailsFragment newInstance(int companyId) {
        CompaniesDetailsFragment fragment = new CompaniesDetailsFragment();
        Bundle bundle = new Bundle();
        bundle.putInt(COMPANY_ID, companyId);
        fragment.setArguments(bundle);
        return fragment;
    }

    private static int getIdFrom(Bundle bundle){
        int id = -1;
        if (bundle != null)
            id = bundle.getInt(COMPANY_ID);
        if (id == 0)
            id = -1;
        return id;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mCompanyId = getIdFrom(getArguments());
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    public interface Contract {

    }
}
