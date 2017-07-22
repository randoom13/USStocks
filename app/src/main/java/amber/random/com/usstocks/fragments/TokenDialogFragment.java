package amber.random.com.usstocks.fragments;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import javax.inject.Inject;

import amber.random.com.usstocks.R;
import amber.random.com.usstocks.injection.App;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class TokenDialogFragment extends DialogFragment
        implements DialogInterface.OnClickListener {
    private static final String sToken = "token";
    private static final String sDescResId = "desc";
    @Inject
    protected SharedPreferences mSharedPreferences;
    private EditText mToken;
    private String mTokenKey;
    private TokenDialogListener mListener;
    private Disposable mSaveTokenDisposable;

    public static TokenDialogFragment newInstance(String tokenKey, int descResId) {
        TokenDialogFragment fragment = new TokenDialogFragment();
        Bundle bundle = new Bundle();
        bundle.putString(sToken, tokenKey);
        bundle.putInt(sDescResId, descResId);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        saveNewToken();
        super.onSaveInstanceState(outState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View form = inflater.inflate(R.layout.token_dialog, container);
        return form;
    }

    private void initializeView(View view) {
        mToken = (EditText) view.findViewById(R.id.token);
        TextView TokenDesc = (TextView) view.findViewById(R.id.token_desc);
        if (getArguments() != null) {
            if (TextUtils.isEmpty(mTokenKey))
                mTokenKey = getArguments().getString(sToken);
            if (!TextUtils.isEmpty(mTokenKey)) {
                String token = mSharedPreferences.getString(mTokenKey, "");
                mToken.setText(token);
            }
            Integer resId = getArguments().getInt(sDescResId);
            if (null != resId)
                TokenDesc.setText(getText(resId));
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        App.getRequestComponent().inject(this);
        View view = getView();
        initializeView(view);
        return new AlertDialog.Builder(getActivity()).
                setTitle(R.string.token_dialog_title).setView(view)
                .setPositiveButton(android.R.string.ok, this)
                .setOnKeyListener((dialog, keyCode, event) -> {
                    if (keyCode == KeyEvent.KEYCODE_BACK)
                        onListener(true);
                    return true;
                })
                .create();
    }

    public void addClickListener(TokenDialogListener listener) {
        mListener = listener;
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        disposeSaveTokenDisposable();
        mSaveTokenDisposable = Observable.fromCallable(this::saveNewToken)
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(r -> onListener(false));
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mListener = null;
        disposeSaveTokenDisposable();
    }

    private Boolean saveNewToken() {
        if (!TextUtils.isEmpty(mTokenKey)) {
            mSharedPreferences.edit().putString(mTokenKey, mToken.getText().toString()).commit();
            return true;
        }
        return false;
    }

    private void onListener(boolean isClose) {
        if (mListener != null) {
            mListener.onClick(isClose);
        }
    }

    private void disposeSaveTokenDisposable() {
        if (mSaveTokenDisposable != null && !mSaveTokenDisposable.isDisposed())
            mSaveTokenDisposable.dispose();
    }

    public interface TokenDialogListener {
        void onClick(boolean isClose);
    }
}
