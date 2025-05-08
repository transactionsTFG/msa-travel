package domainevent.registry;

import java.util.EnumMap;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.inject.Inject;

import business.qualifier.CommitUserQualifier;
import business.qualifier.BeginCreateAirlineReservationEventQualifier;
import business.qualifier.BeginCreateHotelBookingEventQualifier;
import business.qualifier.GetTypeQualifierV2;
import business.qualifier.RollbackUserQualifier;
import domainevent.command.handler.EventHandler;
import msa.commons.event.EventId;

@Singleton
@Startup
public class EventHandlerRegistry {
    private Map<EventId, EventHandler> handlers = new EnumMap<>(EventId.class);
    private EventHandler validateTypeUserHandler;
    private EventHandler commitUserEventHandler;
    private EventHandler rollbackUserEventHandler;
    private EventHandler beginCreateAirlineReservationHandler;
    private EventHandler beginCreateHotelBookingHandler;

    @PostConstruct
    public void init() {
        this.handlers.put(EventId.VALIDATE_TYPE_USER, validateTypeUserHandler);
        this.handlers.put(EventId.CREATE_USER, commitUserEventHandler);
        this.handlers.put(EventId.FAILED_USER, rollbackUserEventHandler);
        this.handlers.put(EventId.RESERVATION_AIRLINE_CREATE_RESERVATION_BEGIN_SAGA,
                beginCreateAirlineReservationHandler);
        this.handlers.put(EventId.BEGIN_CREATE_HOTEL_BOOKING, beginCreateHotelBookingHandler);
    }

    public EventHandler getHandler(EventId eventId) {
        return this.handlers.get(eventId);
    }

    @Inject
    public void setValidateTypeUserCommand(@GetTypeQualifierV2 EventHandler validateTypeUserHandler) {
        this.validateTypeUserHandler = validateTypeUserHandler;
    }

    @Inject
    public void setCommitUserEventHandler(@CommitUserQualifier EventHandler commitUserEventHandler) {
        this.commitUserEventHandler = commitUserEventHandler;
    }

    @Inject
    public void setRollbackUserEventHandler(@RollbackUserQualifier EventHandler rollbackUserEventHandler) {
        this.rollbackUserEventHandler = rollbackUserEventHandler;
    }

    @Inject
    public void setBeginCreateAirlineReservationHandler(
            @BeginCreateAirlineReservationEventQualifier EventHandler beginCreateAirlineReservationHandler) {
        this.beginCreateAirlineReservationHandler = beginCreateAirlineReservationHandler;
    }

    @Inject
    public void setBeginCreateHotelBookingHandler(
            @BeginCreateHotelBookingEventQualifier EventHandler beginCreateHotelBookingHandler) {
        this.beginCreateHotelBookingHandler = beginCreateHotelBookingHandler;
    }

}
