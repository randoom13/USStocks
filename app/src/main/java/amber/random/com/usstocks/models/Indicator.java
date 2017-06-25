package amber.random.com.usstocks.models;

import java.util.List;

public class Indicator {
    public final int mId;
    public final List<String> mNames;

    public Indicator(int id, List<String> names) {
        mId = id;
        mNames = names;
    }
}
