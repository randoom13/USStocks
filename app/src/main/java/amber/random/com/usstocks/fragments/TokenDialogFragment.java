package amber.random.com.usstocks.fragments;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import amber.random.com.usstocks.R;

public class TokenDialogFragment extends DialogFragment
        implements DialogInterface.OnClickListener {
    private static final String TOKEN = "token";
    private static final String DESC = "desc";
    private TextView mTokenDesc;
    private EditText mToken;
    private String mTokenKey;
    private DialogInterface.OnClickListener mListener;

    public static TokenDialogFragment newInstance(String tokenKey, String desc) {
        TokenDialogFragment fragment = new TokenDialogFragment();
        Bundle bundle = new Bundle();
        bundle.putString(TOKEN, tokenKey);
        bundle.putString(DESC, desc);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View form = getActivity().getLayoutInflater().inflate(R.layout.token_dialog, null);
        mToken = (EditText) form.findViewById(R.id.token);
        mTokenDesc = (TextView) form.findViewById(R.id.token_desc);
        if (getArguments() != null) {
            mTokenKey = getArguments().getString(TOKEN);
            if (!TextUtils.isEmpty(mTokenKey)) {
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences
                        (getActivity().getApplicationContext());
                String token = prefs.getString(mTokenKey, "");
                mToken.setText(token);
            }

            mTokenDesc.setText(getArguments().getString(DESC));
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        return builder.setTitle(R.string.token_dialog_title).setView(form)
                .setPositiveButton(android.R.string.ok, this)
                .setNegativeButton(android.R.string.no, this).
                        create();
    }

    public void addListener(DialogInterface.OnClickListener listener) {
        mListener = listener;
    }

    @Override
    public void onOptionsMenuClosed(Menu menu) {
        super.onOptionsMenuClosed(menu);
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        if (!TextUtils.isEmpty(mTokenKey)) {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences
                    (getActivity().getApplicationContext());
            prefs.edit().putString(mTokenKey, mToken.getText().toString()).commit();
        }
        if (mListener != null)
            mListener.onClick(dialog, which);
    }

}