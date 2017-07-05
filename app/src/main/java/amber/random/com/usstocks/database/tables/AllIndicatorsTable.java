package amber.random.com.usstocks.database.tables;

public class AllIndicatorsTable {
    public final String mID = "indicator_id";
    public final String mName = "all_indicators";
    public final String mTOTAL = "indicator_total";
    public final String mCreateScript;

    public AllIndicatorsTable() {
        mCreateScript = String.format("CREATE TABLE IF NOT EXISTS %s("
                + "%s TEXT NOT NULL PRIMARY KEY, %s INTEGER NOT NULL)", mName, mID, mTOTAL);
    }
}
