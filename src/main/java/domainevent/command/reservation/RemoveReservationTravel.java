package domainevent.command.reservation;

import javax.ejb.Local;
import javax.ejb.Stateless;

import com.oracle.state.ext.listener.StateCallback.Event;

import business.qualifier.RemoveReservationTravelQualifier;
import domainevent.command.handler.BaseHandler;
import domainevent.command.handler.CommandHandler;
import msa.commons.commands.removereservation.RemoveReservationCommand;
import msa.commons.event.EventData;
import msa.commons.event.EventId;
import msa.commons.event.eventoperation.reservation.DeleteReservation;

@Stateless
@RemoveReservationTravelQualifier
@Local(CommandHandler.class)
public class RemoveReservationTravel extends BaseHandler {

    @Override
    public void handleCommand(String json) {
        EventData e = this.gson.fromJson(json, EventData.class);
        if (DeleteReservation.DELETE_RESERVATION_ONLY_AIRLINE_BEGIN.equals(e.getOperation()) || DeleteReservation.DELETE_RESERVATION_ONLY_HOTEL_BEGIN.equals(e.getOperation()))
            this.jmsEventPublisher.publish(EventId.REMOVE_RESERVATION_TRAVEL, e);
        //TODO: Implements Commint and Rollback
    }
    
}
