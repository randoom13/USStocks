package amber.random.com.usstocks.fragments.companies;

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
import amber.random.com.usstocks.fragments.base.SelectableAdapter;


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
        if (result)
            itemView.setActivated(true);
        return result;
    }

    @Override
    public void onClick(View v) {
        boolean isChecked = !itemView.isActivated();
        SelectableAdapter adapter = mAdapterWR.get();
        if (null != adapter)
            adapter.setSelected(getAdapterPosition(), isChecked);
        itemView.setActivated(isChecked);
    }

    void bindModel(Cursor cursor, String maxIdFormat) {
        String id = cursor.getString(cursor.getColumnIndex(DataBaseHelper.sCompanyId));
        companyId.setText(id);
        float maxWidth = companyId.getPaint().measureText(maxIdFormat);
        companyId.setWidth((int) maxWidth);
        String name = cursor.getString(cursor.getColumnIndex(DataBaseHelper.sCompanyName));
        companyName.setText(name);
    }
}
