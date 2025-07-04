package domainevent.publisher;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.jms.ConnectionFactory;
import javax.jms.JMSContext;
import javax.jms.Queue;
import javax.jms.TextMessage;

import com.google.gson.Gson;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import msa.commons.event.EventId;
import msa.commons.consts.JMSQueueNames;
import msa.commons.consts.PropertiesConsumer;
import msa.commons.event.Event;
import msa.commons.event.EventData;

@Stateless
public class JMSEventPublisher implements IJMSCommandPublisher {
    private static final Logger LOGGER = LogManager.getLogger(JMSEventPublisher.class);
    private ConnectionFactory connectionFactory;
    private Queue orchestratorQueue;
    private Gson gson;

    @Override
    public void publish(EventId eventId, EventData data){
        try(JMSContext jmsContext = connectionFactory.createContext()) {
            Event sendMsg = new Event(eventId, data);
            final String msg = this.gson.toJson(sendMsg);
            TextMessage txt = jmsContext.createTextMessage(msg);
            txt.setStringProperty(PropertiesConsumer.ORIGIN_QUEUE, JMSQueueNames.AGENCY_ORCHESTATOR_QUEUE);
            LOGGER.info("Publicando en Cola {}, Evento Id: {}, Mensaje: {}", JMSQueueNames.AGENCY_ORCHESTATOR_QUEUE, eventId, msg);
            jmsContext.createProducer().send(this.orchestratorQueue, txt);
        } catch (Exception e) {
            LOGGER.error("Error al publiar el mensaje: {}", e.getMessage());
        }
    }

    @Inject
    public void setConnectionFactory(ConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }
    @Inject
    public void setOrchestratorQueue(Queue orchestratorQueue) {
        this.orchestratorQueue = orchestratorQueue;
    }
    @Inject
    public void setGson(Gson gson) {
        this.gson = gson;
    }
}
