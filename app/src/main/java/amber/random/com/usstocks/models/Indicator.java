package amber.random.com.usstocks.models;

import java.util.List;

public class Indicator {

    public final String mId;
    public final List<IndicatorInfo> mInfos;
    public int mTotal;

    public Indicator(String id, int total, List<IndicatorInfo> infos) {
        mInfos = infos;
        mId = id;
        mTotal = total;
    }
}
