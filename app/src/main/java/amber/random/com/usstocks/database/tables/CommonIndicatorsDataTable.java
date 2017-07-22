package amber.random.com.usstocks.database.tables;

public class CommonIndicatorsDataTable {
    public final String id = "indicator_id";
    public final String indicatorValue = "indicator_value";
    public final String name = "common_indicators_data";
    public final String indicatorYear = "indicator_year";

    public final String CreateScript;

    public CommonIndicatorsDataTable() {
        CreateScript = String.format("CREATE TABLE IF NOT EXISTS %s("
                + "%s TEXT NOT NULL PRIMARY KEY," +
                "%s INTEGER NOT NULL, %s INTEGER NOT NULL)", name, id, indicatorYear, indicatorValue);
    }
}
