package amber.random.com.usstocks.database.tables;

public class CommonIndicatorsDataTable {
    public final String mID = "indicator_id";
    public final String mINDICATOR_VALUE = "indicator_value";
    public final String mName = "common_indicators_data";
    public final String mINDICATOR_YEAR = "indicator_year";

    public final String mCreateScript;

    public CommonIndicatorsDataTable() {
        mCreateScript = String.format("CREATE TABLE IF NOT EXISTS %s("
                + "%s TEXT NOT NULL PRIMARY KEY," +
                "%s INTEGER NOT NULL, %s INTEGER NOT NULL)", mName, mID, mINDICATOR_YEAR, mINDICATOR_VALUE);
    }
}
