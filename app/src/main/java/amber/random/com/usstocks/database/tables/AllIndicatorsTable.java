package amber.random.com.usstocks.database.tables;

public class AllIndicatorsTable {
    public final String id = "indicator_id";
    public final String name = "all_indicators";
    public final String total = "indicator_total";
    public final String createScript;

    public AllIndicatorsTable() {
        createScript = String.format("CREATE TABLE IF NOT EXISTS %s("
                + "%s TEXT NOT NULL PRIMARY KEY, %s INTEGER NOT NULL)", name, id, total);
    }
}
