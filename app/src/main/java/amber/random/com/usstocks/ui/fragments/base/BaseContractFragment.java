package amber.random.com.usstocks.ui.fragments.base;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

import amber.random.com.usstocks.R;

public class BaseContractFragment<T> extends Fragment {

    protected T mContract;
    private Toolbar mToolbar;

    protected void initializeBar(View view) {
        mToolbar = (Toolbar) view.findViewById(R.id.toolbar);
        ((AppCompatActivity) getActivity()).setSupportActionBar(mToolbar);
        mToolbar.setLogo(R.mipmap.ic_launcher);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        initializeContract();
    }

    @SuppressWarnings("deprecation")
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            initializeContract();
        }
    }

    private void initializeContract() {
        try {
            mContract = (T) getActivity();
        } catch (ClassCastException ex) {
            throw new IllegalStateException(getActivity().getClass().getSimpleName() +
                    "does not implement contract interface for " + getClass().getSimpleName());
        }
    }

    @Override
    public void onDetach() {
        mContract = null;
        super.onDetach();
    }

}
