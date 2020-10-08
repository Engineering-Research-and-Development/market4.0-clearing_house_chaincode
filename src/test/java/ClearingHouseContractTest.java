import de.fraunhofer.iais.eis.LogNotification;
import de.fraunhofer.iais.eis.LogNotificationBuilder;
import it.eng.clearinghouse.chaincode.ClearingHouseContract;
import it.eng.idsa.clearinghouse.model.Body;
import it.eng.idsa.clearinghouse.model.NotificationContent;
import it.eng.idsa.clearinghouse.model.json.JsonHandler;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.GregorianCalendar;
import java.util.Random;

public class ClearingHouseContractTest {

    static String testChaincode() {
        ClearingHouseContract contract = new ClearingHouseContract(true);
        try {
            NotificationContent logNotification = createNotificationContent();
            String id = logNotification.getHeader().getId().toString();
            System.out.println("ID: " + id);
            String logNotificationJson = JsonHandler.convertToJson(logNotification);
            NotificationContent contractNotificationContent = (NotificationContent) JsonHandler.
                    convertFromJson(contract.createNotificationContent(null, logNotificationJson), NotificationContent.class);
            NotificationContent contractNotificationContent1 = (NotificationContent) JsonHandler
                    .convertFromJson(contract.getNotificationContent(null, id), NotificationContent.class);
            String[] allNotificationContents = contract.getAllNotificationContents(null);
            for (String notification : allNotificationContents) {
                NotificationContent logNotification1 = (NotificationContent) JsonHandler.convertFromJson(notification, NotificationContent.class);
                System.out.println("ID " + logNotification1.getHeader().getId() + " of " + logNotification1.toString());
            }
            if (contractNotificationContent.getHeader().getId().equals(contractNotificationContent1.getHeader().getId())) {
                System.out.println("!!!Everything works like a charm :-D ");
                System.out.println("Contract received \n " + logNotificationJson);
            } else {
                System.err.println("!!!Something goes wrong :-( ");

            }
            return id;
        } catch (Exception e) {
            System.err.println("!!!Something goes wrong :-( ");
            e.printStackTrace();
        }
        return null;
    }


    static NotificationContent createNotificationContent() {
        Random rand = new Random();
        LogNotification header = null;
        GregorianCalendar gcal = new GregorianCalendar();
        XMLGregorianCalendar xgcal = null;
        try {
            xgcal = DatatypeFactory.newInstance()
                    .newXMLGregorianCalendar(gcal);
            URI id = new URI("https://w3id.org/idsa/autogen/brokerQueryMessage/6bed5855-489b-4f47-82dc-08c5f1656101-" +
                    Math.abs(rand.nextInt(1000)));
            header = new LogNotificationBuilder(id)
                    ._modelVersion_("1.0.3")
                    ._issued_(xgcal)
                    ._correlationMessage_(new URI("https://w3id.org/idsa/autogen/brokerQueryMessage/6bed5855-489b-4f47-82dc-08c5f1656101"))
                    ._issuerConnector_(new URI("https://ids.tno.nl/test"))
                    ._recipientConnector_(null)
                    ._senderAgent_(null)
                    ._recipientAgent_(null)
                    ._transferContract_(null)
                    .build();
            NotificationContent logNotification = new NotificationContent(header, new Body());
            return logNotification;
        } catch (DatatypeConfigurationException e) {
            e.printStackTrace();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        } catch (Exception e) {
        }
        return null;
    }

    public static void main(String[] args) {
        testChaincode();
    }

}
