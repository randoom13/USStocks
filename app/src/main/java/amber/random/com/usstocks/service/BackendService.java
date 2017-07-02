package amber.random.com.usstocks.service;

import java.util.List;

import amber.random.com.usstocks.models.Company;
import amber.random.com.usstocks.models.Indicator;
import io.reactivex.Flowable;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface BackendService {
    @GET("companies/xbrl?format=json")
    Flowable<List<Company>> getAllCompanies(@Query("token") String token);

    @GET("indicators/xbrl/meta")
    Flowable<List<Indicator>> getAllIndicators(@Query("token") String token);
}
