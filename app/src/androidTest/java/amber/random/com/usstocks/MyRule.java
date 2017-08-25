package amber.random.com.usstocks;

import android.support.test.InstrumentationRegistry;

import amber.random.com.usstocks.injection.DebugRequestComponent;
import amber.random.com.usstocks.injection.modules.AppModule;
import amber.random.com.usstocks.injection.modules.NetworkModule;
import amber.random.com.usstocks.injection.modules.RequestModule;
import it.cosenonjaviste.daggermock.DaggerMockRule;

public class MyRule extends DaggerMockRule<DebugRequestComponent> {
    public MyRule() {
        super(DebugRequestComponent.class, new AppModule
                        ((App) InstrumentationRegistry.getInstrumentation()
                                .getTargetContext().getApplicationContext()),
                new NetworkModule(), new RequestModule());
        set(component -> App.setRequestComponent(component));
    }
}