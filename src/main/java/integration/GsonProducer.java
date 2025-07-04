package integration;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;

import com.google.gson.Gson;

import msa.commons.utils.GsonUtils;

@ApplicationScoped
public class GsonProducer {
    @Produces
    private Gson gson = GsonUtils.getInstance();
}
