package domainevent.command.reservation;

import javax.ejb.Local;
import javax.ejb.Stateless;

import business.qualifier.ValidateUserReservationTravelQualifier;
import domainevent.command.handler.BaseHandler;
import domainevent.command.handler.CommandHandler;
import msa.commons.event.EventData;
import msa.commons.event.EventId;

@Stateless
@ValidateUserReservationTravelQualifier
@Local(CommandHandler.class)
public class ValidateUserAirlineReservationTravel extends BaseHandler {

    @Override
    public void handleCommand(String json) {
        EventData e = this.gson.fromJson(json, EventData.class);
        this.jmsEventPublisher.publish(EventId.VALIDATE_USER, e);
    }
    
}
