package amber.random.com.usstocks.fragments.companies.old;

import android.view.View;
import android.widget.TextView;

import amber.random.com.usstocks.R;

public class CompanyHolder {
    public final TextView mCompanyId;
    public final TextView mCompanyName;

    public CompanyHolder(View view) {
        mCompanyId = (TextView) view.findViewById(R.id.companyId);
        mCompanyName = (TextView) view.findViewById(R.id.companyName);
    }
}
