package amber.random.com.usstocks.ui.fragments;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import javax.inject.Inject;

import amber.random.com.usstocks.App;
import amber.random.com.usstocks.R;
import amber.random.com.usstocks.preference.AppPreferences;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class TokenDialogFragment extends DialogFragment
        implements DialogInterface.OnClickListener {
    private static final String sDescResId = "desc_id";
    private static final String sShowCancelButton = "cancel_button";

    @Inject
    protected AppPreferences mAppPreferences;
    private EditText mToken;
    private TokenDialogListener mListener;
    private Disposable mSaveTokenDisposable;

    public static TokenDialogFragment newInstance(int descResId, boolean showCancelButton) {
        TokenDialogFragment fragment = new TokenDialogFragment();
        Bundle bundle = new Bundle();
        bundle.putInt(sDescResId, descResId);
        bundle.putBoolean(sShowCancelButton, showCancelButton);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        saveNewToken();
        super.onSaveInstanceState(outState);
    }

    private void initializeView(View view) {
        mToken = (EditText) view.findViewById(R.id.token);
        TextView TokenDesc = (TextView) view.findViewById(R.id.token_desc);
        if (getArguments() != null) {
            Integer resId = getArguments().getInt(sDescResId);
            if (null != resId)
                TokenDesc.setText(getText(resId));
        }

        String token = mAppPreferences.getToken();
        if (!TextUtils.isEmpty(token)) {
            mToken.setText(token);
            mToken.setSelection(token.length());
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        App.getRequestComponent().inject(this);
        View view = getActivity().getLayoutInflater().inflate(R.layout.token_dialog, null);
        initializeView(view);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity()).setView(view)
                .setTitle(R.string.token_dialog_title);
        if (null != getArguments() && Boolean.TRUE.equals(getArguments().getBoolean(sShowCancelButton)))
            builder.setNegativeButton(android.R.string.cancel, (dialog, which) -> dialog.dismiss());

        return builder
                .setPositiveButton(android.R.string.ok, this)
                .setOnKeyListener((dialog, keyCode, event) -> {
                    if (keyCode == KeyEvent.KEYCODE_BACK)
                        onListener(true);
                    return true;
                })
                .create();
    }

    public void setClickListener(TokenDialogListener listener) {
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
        mListener = null;
        disposeSaveTokenDisposable();
        super.onDestroyView();
    }

    private Boolean saveNewToken() {
        mAppPreferences.setToken(mToken.getText().toString());
        return true;
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
