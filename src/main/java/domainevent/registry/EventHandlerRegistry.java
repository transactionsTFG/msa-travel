package domainevent.registry;

import java.util.EnumMap;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.inject.Inject;

import business.qualifier.CreateReservationTravelQualifier;
import business.qualifier.UpdateReservationTravelQualifier;
import business.qualifier.ValidateUserReservationTravelQualifier;
import domainevent.command.handler.CommandHandler;
import msa.commons.event.EventId;

@Singleton
@Startup
public class EventHandlerRegistry {
    private Map<EventId, CommandHandler> handlers = new EnumMap<>(EventId.class);
    private CommandHandler validateUserReservationTravel;
    private CommandHandler createReservationTravel;
    private CommandHandler updateReservationTravel;

    @PostConstruct
    public void init() {
        this.handlers.put(EventId.VALIDATE_USER, this.validateUserReservationTravel);
        this.handlers.put(EventId.CREATE_RESERVATION_TRAVEL, this.createReservationTravel);
        this.handlers.put(EventId.UPDATE_RESERVATION_TRAVEL, this.updateReservationTravel);
    }

    public CommandHandler getHandler(EventId eventId) {
        return this.handlers.get(eventId);
    }

    @Inject
    public void setValidateUserReservationTravel(@ValidateUserReservationTravelQualifier CommandHandler validateUserAirlineReservationTravel) {
        this.validateUserReservationTravel = validateUserAirlineReservationTravel;
    }

    @Inject
    public void setCreateReservationTravel(@CreateReservationTravelQualifier CommandHandler createReservationTravel) {
        this.createReservationTravel = createReservationTravel;
    }

    @Inject
    public void setUpdateReservationTravel(@UpdateReservationTravelQualifier CommandHandler updateReservationTravel) {
        this.updateReservationTravel = updateReservationTravel;
    }


}
