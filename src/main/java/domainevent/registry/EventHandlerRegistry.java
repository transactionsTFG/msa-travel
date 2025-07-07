package domainevent.registry;

import java.util.EnumMap;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.inject.Inject;

import business.qualifier.ValidateUserAirlineReservationTravelQualifier;
import domainevent.command.handler.CommandHandler;
import msa.commons.event.EventId;

@Singleton
@Startup
public class EventHandlerRegistry {
    private Map<EventId, CommandHandler> handlers = new EnumMap<>(EventId.class);
    private CommandHandler validateUserAirlineReservationTravel;

    @PostConstruct
    public void init() {
        this.handlers.put(EventId.USER_AGENCY_VALIDATE_USER_BEGIN, this.validateUserAirlineReservationTravel);
    }

    public CommandHandler getHandler(EventId eventId) {
        return this.handlers.get(eventId);
    }

    @Inject
    public void setValidateUserAirlineReservationTravel(@ValidateUserAirlineReservationTravelQualifier CommandHandler validateUserAirlineReservationTravel) {
        this.validateUserAirlineReservationTravel = validateUserAirlineReservationTravel;
    }

}
