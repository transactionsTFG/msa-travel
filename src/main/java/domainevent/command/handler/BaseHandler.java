package domainevent.command.handler;

import javax.ejb.EJB;
import javax.inject.Inject;

import com.google.gson.Gson;

import business.service.TravelService;
import domainevent.publisher.IJMSCommandPublisher;

public abstract class BaseHandler implements EventHandler {
    protected TravelService travelService;
    protected IJMSCommandPublisher jmsEventPublisher;
    protected Gson gson;
    @EJB
    public void setTypeUserServices(TravelService travelService) {
        this.travelService = travelService;
    }

    @EJB
    public void setJmsEventPublisher(IJMSCommandPublisher jmsEventPublisher) {
        this.jmsEventPublisher = jmsEventPublisher;
    }

    @Inject
    public void setGson(Gson gson) {
        this.gson = gson;
    }
}
