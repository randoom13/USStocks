package amber.random.com.usstocks.ui.fragments.companies;

import android.annotation.TargetApi;
import android.database.Cursor;
import android.os.Build;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.MotionEvent;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import java.lang.ref.WeakReference;

import amber.random.com.usstocks.R;
import amber.random.com.usstocks.ui.fragments.base.SelectableAdapter;

import static amber.random.com.usstocks.database.DataBaseHelperProxy.COMPANY_ID;
import static amber.random.com.usstocks.database.DataBaseHelperProxy.COMPANY_NAME;


public class CompanyHolder extends RecyclerView.ViewHolder implements View.OnClickListener,
        View.OnLongClickListener, CompoundButton.OnCheckedChangeListener {
    public final TextView companyId;
    public final TextView companyName;
    public final CheckBox mSelector;
    private final WeakReference<SelectableAdapter> mAdapterWR;
    private boolean mMultiSelectMode = false;

    public CompanyHolder(View view, SelectableAdapter adapter) {
        super(view);
        mAdapterWR = new WeakReference<SelectableAdapter>(adapter);
        mSelector = (CheckBox) view.findViewById(R.id.selector);
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
        mSelector.setOnCheckedChangeListener(this);
    }

    public void setMultiSelectMode(boolean multiSelectMode) {
        if (multiSelectMode == mMultiSelectMode)
            return;

        this.mMultiSelectMode = multiSelectMode;
        if (isSelected())
            itemView.setActivated(!mMultiSelectMode);

        mSelector.setVisibility(mMultiSelectMode ? View.VISIBLE : View.GONE);
    }

    @Override
    public boolean onLongClick(View v) {
        SelectableAdapter adapter = mAdapterWR.get();
        if (null == adapter || mMultiSelectMode)
            return false;
        return adapter.isLongClick(this);
    }

    public boolean isSelected() {
        return mSelector.isChecked();
    }

    public void setSelected(boolean isSelected) {
        if (!mMultiSelectMode)
            itemView.setActivated(isSelected);

        mSelector.setOnCheckedChangeListener(null);
        mSelector.setChecked(isSelected);
        mSelector.setOnCheckedChangeListener(this);
    }

    private void setSelectedInternal(boolean isSelected) {
        SelectableAdapter adapter = mAdapterWR.get();
        if (null != adapter) {
            adapter.setSelected(this, isSelected);
        }
    }

    @Override
    public void onClick(View v) {
        setSelectedInternal(!isSelected());
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        setSelectedInternal(isChecked);
    }

    void bindModel(Cursor cursor, String maxIdFormat) {
        String name = cursor.getString(cursor.getColumnIndex(COMPANY_NAME));
        companyName.setText(Html.fromHtml(name));
        String id = cursor.getString(cursor.getColumnIndex(COMPANY_ID));
        companyId.setText(Html.fromHtml(id));
        float maxWidth = companyId.getPaint().measureText(maxIdFormat);
        companyId.setWidth((int) maxWidth);
    }
}
