package amber.random.com.usstocks.service.rest;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.jakewharton.retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;

import java.util.ArrayList;
import java.util.List;

import amber.random.com.usstocks.BuildConfig;
import amber.random.com.usstocks.exceptions.UnknownFormat;
import amber.random.com.usstocks.models.AutoValueGsonFactory;
import amber.random.com.usstocks.models.Company;
import amber.random.com.usstocks.models.Indicator;
import amber.random.com.usstocks.models.IndicatorInfo;
import io.reactivex.Flowable;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Query;

public class BackendServiceProxyImp implements BackendServiceProxy {
    private static final String sREGEX_END_LINE = "([ \\t\\r]*\\n[ \\t\\r]*)+";
    private JsonBackendService mJsonBackendService;
    private StringBackendService mStringBackendService;

    public BackendServiceProxyImp() {
        Retrofit StringRetrofit = new Retrofit.Builder()
                .baseUrl(BuildConfig.END_POINT)
                .addConverterFactory(ScalarsConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build();
        mStringBackendService = StringRetrofit.create(StringBackendService.class);
        Gson gson = new GsonBuilder().serializeNulls().
                registerTypeAdapterFactory(AutoValueGsonFactory.create()).create();
        Retrofit jsonRetrofit = new Retrofit.Builder().baseUrl(BuildConfig.END_POINT)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build();
        mJsonBackendService = jsonRetrofit.create(JsonBackendService.class);
    }

    private static Integer tryParse(String string) throws UnknownFormat {
        try {
            return Integer.valueOf(string);
        } catch (NumberFormatException ex) {
            throw new UnknownFormat(string);
        }
    }

    private static List<Integer> getDefaultYears(String firstString) throws UnknownFormat {
        String[] items = firstString.split(",");
        List<Integer> result = new ArrayList<>();

        try {
            for (int index = 2; index < items.length; index++) {
                result.add(Integer.valueOf(items[index]));
            }
        } catch (NumberFormatException ex) {
            throw new UnknownFormat(firstString);
        }
        return result;
    }

    public Flowable<List<Company>> getAllCompanies(String token) {
        return mJsonBackendService.getAllCompanies(token);
    }

    public Flowable<List<Indicator>> getAllIndicators(String token) {
        return mStringBackendService.getAllIndicators(token).map(str -> {
            String[] strings = str.split(sREGEX_END_LINE);
            List<Indicator> indicators = new ArrayList<>();
            String headerString = strings[0];
            List<Integer> defYears = getDefaultYears(headerString);

            for (int index = 1; index < strings.length; index++) {
                String string = strings[index];
                List<IndicatorInfo> values = new ArrayList<IndicatorInfo>();
                Integer total = null;
                String[] items = string.split(",");
                if (items.length <= 1)
                    throw new UnknownFormat(string);
                for (int ind = 1; ind < items.length; ind++) {
                    Integer number = tryParse(items[ind]);
                    if (number == null)
                        throw new UnknownFormat(string);

                    if (ind == 1)
                        total = number;
                    else if (values.size() < defYears.size())
                        values.add(new IndicatorInfo(defYears.get(values.size()), number));
                }
                if (total == null || values.isEmpty())
                    throw new UnknownFormat(string);

                indicators.add(new Indicator(items[0], total, values));
            }
            return indicators;
        });
    }

    private interface JsonBackendService {
        @GET("companies/xbrl?format=json")
        Flowable<List<Company>> getAllCompanies(@Query("token") String token);
    }

    private interface StringBackendService {
        @GET("indicators/xbrl/meta")
        Flowable<String> getAllIndicators(@Query("token") String token);
    }
}
