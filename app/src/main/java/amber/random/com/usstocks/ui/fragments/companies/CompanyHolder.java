package amber.random.com.usstocks.ui.fragments.companies;

import android.annotation.TargetApi;
import android.database.Cursor;
import android.os.Build;
import android.support.v7.widget.RecyclerView;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

import java.lang.ref.WeakReference;

import amber.random.com.usstocks.R;
import amber.random.com.usstocks.database.DataBaseHelper;
import amber.random.com.usstocks.ui.fragments.base.SelectableAdapter;


public class CompanyHolder extends RecyclerView.ViewHolder implements View.OnClickListener,
        View.OnLongClickListener {
    public final TextView companyId;
    public final TextView companyName;
    private final WeakReference<SelectableAdapter> mAdapterWR;

    public CompanyHolder(View view, SelectableAdapter adapter) {
        super(view);
        mAdapterWR = new WeakReference<SelectableAdapter>(adapter);
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


    @Override
    public boolean onLongClick(View v) {
        SelectableAdapter adapter = mAdapterWR.get();
        boolean result = null != adapter && adapter.isLongClick(getAdapterPosition());
        setSelection(result, false);
        return result;
    }

    public void setSelection(boolean isSelected, boolean force) {
        if (force || itemView.isActivated() != isSelected)
            itemView.setActivated(isSelected);
    }

    public boolean isSelected() {
        return itemView.isActivated();
    }

    @Override
    public void onClick(View v) {
        boolean isChecked = !isSelected();
        SelectableAdapter adapter = mAdapterWR.get();
        if (null != adapter) {
            adapter.setSelected(getAdapterPosition(), isChecked);
            setSelection(isChecked, true);
        }
    }

    void bindModel(Cursor cursor, String maxIdFormat) {
        String id = cursor.getString(cursor.getColumnIndex(DataBaseHelper.COMPANY_ID));
        companyId.setText(id);
        float maxWidth = companyId.getPaint().measureText(maxIdFormat);
        companyId.setWidth((int) maxWidth);
        String name = cursor.getString(cursor.getColumnIndex(DataBaseHelper.COMPANY_NAME));
        companyName.setText(name);
    }
}
