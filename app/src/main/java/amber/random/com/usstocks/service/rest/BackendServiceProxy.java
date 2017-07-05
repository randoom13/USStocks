package amber.random.com.usstocks.service.rest;

import java.util.List;

import amber.random.com.usstocks.models.Company;
import amber.random.com.usstocks.models.Indicator;
import io.reactivex.Flowable;

public interface BackendServiceProxy {
    Flowable<List<Company>> getAllCompanies(String token);

    Flowable<List<Indicator>> getAllIndicators(String token);
}
