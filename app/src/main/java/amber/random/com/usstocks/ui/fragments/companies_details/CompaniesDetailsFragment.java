package amber.random.com.usstocks.ui.fragments.companies_details;

import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.lsjwzh.widget.recyclerviewpager.RecyclerViewPager;
import com.lsjwzh.widget.recyclerviewpager.TabLayoutSupport;

import javax.inject.Inject;

import amber.random.com.usstocks.App;
import amber.random.com.usstocks.R;
import amber.random.com.usstocks.database.DataBaseHelperProxy;
import amber.random.com.usstocks.ui.fragments.base.BaseContractFragment;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class CompaniesDetailsFragment extends BaseContractFragment<CompaniesDetailsFragment.Contract> {
    private static final String sFILTER = "filter";
    @Inject
    protected DataBaseHelperProxy mDataBaseHelper;

    private Disposable mDisposable;

    public static CompaniesDetailsFragment newInstance(String filter) {
        CompaniesDetailsFragment fragment = new CompaniesDetailsFragment();
        Bundle bundle = new Bundle();
        bundle.putString(sFILTER, filter);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onDestroyView() {
        if (mDisposable != null && !mDisposable.isDisposed())
            mDisposable.dispose();

        super.onDestroyView();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.main_company_details_fragment, container, false);
        initializeBar(view);
        RecyclerViewPager pager = (RecyclerViewPager) view.findViewById(R.id.pager);
        pager.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false));
        TabLayout tabLayout = (TabLayout) view.findViewById(R.id.tabs);
        App.getRequestComponent().inject(this);
        mDisposable = Observable.fromCallable(() -> {
            String filter = "";
            if (getArguments() != null)
                filter = getArguments().getString(sFILTER);
            final Cursor cursor = mDataBaseHelper.getSelectedCompanies(filter);
            cursor.moveToFirst();
            return cursor;
        })
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .onErrorReturn(ex -> {
                    Log.e(this.getClass().getSimpleName(), "Can't load cursor", ex);
                    return null;
                })
                .subscribe(c -> {
                    if (c != null) {
                        pager.setAdapter(new PageAdapter(getActivity().getLayoutInflater(), c));
                        TabLayoutSupport.setupWithViewPager(tabLayout, pager, new TabAdapter(c));
                    }
                });
        return view;
    }

    public interface Contract {

    }
}
