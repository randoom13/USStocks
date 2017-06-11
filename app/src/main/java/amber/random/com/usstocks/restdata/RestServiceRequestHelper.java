package amber.random.com.usstocks.restdata;

class RestServiceRequestHelper {
    private final static String sALL_COMPANIES = "https://api.usfundamentals.com/v1/companies/xbrl?format=json&token=%s";
    private final static String sALL_INDICATORS = "https://api.usfundamentals.com/v1/indicators/xbrl/meta?token=%s";
    private final static String sCOMPANIES_INDICATORS = "https://api.usfundamentals.com/v1/indicators/xbrl?companies=%s&token=%s";

    //   private final static String sCOMPANIES_INDICATORS ="https://api.usfundamentals.com/v1/indicators/xbrl?indicators=%s&companies=%s&token=%s";
    public static String getAllCompanies(String token) {
        return String.format(sALL_COMPANIES, token);
    }

    public static String getAllIndicators(String token) {
        return String.format(sALL_INDICATORS, token);
    }

    public static String getCompaniesIndicators(String token, Integer[] companies/*, String[] indicatorsNames*/) {
        return String.format(sCOMPANIES_INDICATORS, ToString(companies), token);
    }

    private static String ToString(Object[] params) {
        StringBuilder builder = new StringBuilder();
        boolean hasItems = false;
        for (Object item : params) {
            if (hasItems)
                builder.append(",");
            else hasItems = true;
            builder.append(item);
        }
        return builder.toString();
    }
}
