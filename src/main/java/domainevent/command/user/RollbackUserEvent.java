package domainevent.command.user;

import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.inject.Inject;

import business.qualifier.RollbackUserQualifier;
import domainevent.command.handler.BaseEventHandler;
import domainevent.command.handler.EventHandler;
import domainevent.publisher.jmseventpublisher.IEventPublisher;
import domainevent.publisher.userqueue.JMSUserPublisherQualifier;
import msa.commons.event.EventId;

@Stateless
@RollbackUserQualifier
@Local(EventHandler.class)
public class RollbackUserEvent extends BaseEventHandler {
    @Inject
    public void setJmsEventDispatcher(@JMSUserPublisherQualifier IEventPublisher jmsEventDispatcher) {
        this.jmsEventDispatcher = jmsEventDispatcher;
    }
    @Override
    public EventId getEventId() {
        return EventId.FAILED_USER;
    }
}
