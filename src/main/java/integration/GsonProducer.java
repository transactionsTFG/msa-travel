package integration;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;

import com.google.gson.Gson;

@ApplicationScoped
public class GsonProducer {
    @Produces
    private Gson gson = new Gson();
}
