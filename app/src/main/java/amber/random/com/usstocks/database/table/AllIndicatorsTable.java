package amber.random.com.usstocks.database.table;

public class AllIndicatorsTable {
    public final String mID = "indicator_id";
    public final String mINDICATOR_NAME = "indicator_name";
    public final String mName = "all_indicators";
    ;
    public final String mCreateScript;

    public AllIndicatorsTable() {
        mCreateScript = String.format("CREATE VIRTUAL TABLE IF NOT EXISTS %s USING fts3("
                + "%s INTEGER NOT NULL PRIMARY KEY UNIQUE," +
                "%s TEXT NOT NULL)", mName, mID, mINDICATOR_NAME);
    }
}
