package amber.random.com.usstocks.models;

import java.util.List;

public class Indicator {

    public final String id;
    public final List<IndicatorInfo> infos;
    public int Total;

    public Indicator(String id, int total, List<IndicatorInfo> infos) {
        this.infos = infos;
        this.id = id;
        Total = total;
    }
}
