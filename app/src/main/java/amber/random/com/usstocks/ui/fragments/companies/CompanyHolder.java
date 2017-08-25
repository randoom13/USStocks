package amber.random.com.usstocks.ui.fragments.companies;

import android.annotation.TargetApi;
import android.database.Cursor;
import android.os.Build;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;

import java.lang.ref.WeakReference;

import amber.random.com.usstocks.R;
import amber.random.com.usstocks.ui.fragments.base.SelectableAdapter;

import static amber.random.com.usstocks.database.DataBaseHelperProxy.COMPANY_ID;
import static amber.random.com.usstocks.database.DataBaseHelperProxy.COMPANY_NAME;
import static amber.random.com.usstocks.database.DataBaseHelperProxy.COMPANY_PREVIOUS_NAMES;


public class CompanyHolder extends RecyclerView.ViewHolder implements View.OnClickListener,
        View.OnLongClickListener {
    public final TextView companyId;
    public final TextView companyName;
    public final CheckBox selector;
    private final WeakReference<SelectableAdapter> mAdapterWR;
    private boolean mMultiSelectMode = false;

    public CompanyHolder(View view, SelectableAdapter adapter) {
        super(view);
        mAdapterWR = new WeakReference<SelectableAdapter>(adapter);
        selector = (CheckBox) view.findViewById(R.id.selector);
        companyId = (TextView) view.findViewById(R.id.companyId);
        companyName = (TextView) view.findViewById(R.id.companyName);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            view.setOnTouchListener(new View.OnTouchListener() {
                @TargetApi(Build.VERSION_CODES.LOLLIPOP)
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    v.getBackground().setHotspot(event.getX(), event.getY());
                    return false;
                }
            });
        }
        view.setLongClickable(true);
        view.setOnClickListener(this);
        view.setOnLongClickListener(this);
    }

    public void setMultiSelectMode(boolean isMultiSelectMode) {
        mMultiSelectMode = isMultiSelectMode;
        selector.setEnabled(mMultiSelectMode);
    }

    @Override
    public boolean onLongClick(View v) {
        SelectableAdapter adapter = mAdapterWR.get();
        return null != adapter && !mMultiSelectMode && adapter.isLongClick(this);
    }

    public boolean isSelected() {
        return selector.isChecked();
    }

    public void setSelected(boolean isSelected) {
        selector.setChecked(isSelected);
    }

    @Override
    public void onClick(View v) {
        SelectableAdapter adapter = mAdapterWR.get();
        if (null != adapter)
            adapter.setSelected(this, !isSelected());
    }

    void bindModel(Cursor cursor, String maxIdFormat) {
        String id = cursor.getString(cursor.getColumnIndex(COMPANY_ID));
        companyId.setText(Html.fromHtml(id));
        float maxWidth = companyId.getPaint().measureText(maxIdFormat);
        companyId.setWidth((int) maxWidth);
        String previousNames = cursor.getString(cursor.getColumnIndex(COMPANY_PREVIOUS_NAMES));
        String name = cursor.getString(cursor.getColumnIndex(COMPANY_NAME));
        StringBuilder builder = new StringBuilder(name);
        if (!TextUtils.isEmpty(previousNames)) {
            builder.append(",<br/>(");
            builder.append(previousNames);
            builder.append(")");
        }
        companyName.setText(Html.fromHtml(builder.toString()));
    }
}
