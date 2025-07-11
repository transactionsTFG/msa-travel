package domainevent.command.airlinereservation;

import java.util.ArrayList;

import javax.ejb.Local;
import javax.ejb.Stateless;

import business.qualifier.ValidateUserAirlineReservationTravelQualifier;
import business.travel.TravelDTO;
import domainevent.command.handler.BaseHandler;
import domainevent.command.handler.CommandHandler;
import msa.commons.commands.createreservation.CreateReservationCommand;
import msa.commons.event.EventData;
import msa.commons.event.EventId;

@Stateless
@ValidateUserAirlineReservationTravelQualifier
@Local(CommandHandler.class)
public class ValidateUserAirlineReservationTravel extends BaseHandler {

    @Override
    public void handleCommand(String json) {
        CreateReservationCommand c = this.gson.fromJson(json, CreateReservationCommand.class);
        TravelDTO travelDTO =  this.travelService.getTravelById(c.getIdReservationTravel());
        EventData eventData = new EventData(travelDTO.getSagaId(), new ArrayList<>(), c);
        this.jmsEventPublisher.publish(EventId.USER_AGENCY_VALIDATE_USER_BEGIN, eventData);
    }
    
}
