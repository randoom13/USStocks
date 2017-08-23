package amber.random.com.usstocks.ui.fragments.companies;

import android.annotation.TargetApi;
import android.database.Cursor;
import android.os.Build;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

import java.lang.ref.WeakReference;

import amber.random.com.usstocks.R;
import amber.random.com.usstocks.ui.fragments.base.SelectableAdapter;

import static amber.random.com.usstocks.database.DataBaseHelperProxy.COMPANY_ID;
import static amber.random.com.usstocks.database.DataBaseHelperProxy.COMPANY_NAME;


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
        setSelection(result);
        return result;
    }

    public void setSelection(boolean isSelected) {
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
            setSelection(isChecked);
        }
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
