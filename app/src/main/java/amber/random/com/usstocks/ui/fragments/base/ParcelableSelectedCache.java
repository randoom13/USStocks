package amber.random.com.usstocks.ui.fragments.base;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class ParcelableSelectedCache extends HashMap<Integer, Boolean>
        implements Parcelable {
    public static Creator<ParcelableSelectedCache> CREATOR
            = new Creator<ParcelableSelectedCache>() {
        @Override
        public ParcelableSelectedCache[] newArray(int size) {
            return new ParcelableSelectedCache[size];
        }

        @Override
        public ParcelableSelectedCache createFromParcel(Parcel source) {
            return new ParcelableSelectedCache(source);
        }
    };

    private ParcelableSelectedCache(Parcel source) {
        int selectedIdsSize = source.readInt();
        int unSelectedIdsSize = source.readInt();
        for (int index = 0; index < selectedIdsSize; index++) {
            put(source.readInt(), true);
        }
        for (int index = 0; index < unSelectedIdsSize; index++) {
            put(source.readInt(), false);
        }
    }

    public ParcelableSelectedCache() {
        super();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        Set<Integer> selectedIds = new HashSet<Integer>();
        Set<Integer> unSelectedIds = new HashSet<Integer>();
        for (Entry<Integer, Boolean> item : entrySet()) {
            if (item.getValue())
                selectedIds.add(item.getKey());
            else
                unSelectedIds.add(item.getKey());
        }
        dest.writeInt(selectedIds.size());
        dest.writeInt(unSelectedIds.size());
        for (int id : selectedIds) {
            dest.writeInt(id);
        }
        for (int id : unSelectedIds) {
            dest.writeInt(id);
        }
    }
}
