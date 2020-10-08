package it.eng.clearinghouse.chaincode;

import com.owlike.genson.Genson;
import it.eng.idsa.clearinghouse.model.NotificationContent;
import it.eng.idsa.clearinghouse.model.json.JsonHandler;
import org.hyperledger.fabric.contract.Context;
import org.hyperledger.fabric.contract.ContractInterface;
import org.hyperledger.fabric.contract.annotation.*;
import org.hyperledger.fabric.shim.ChaincodeException;
import org.hyperledger.fabric.shim.ChaincodeStub;
import org.hyperledger.fabric.shim.ledger.KeyValue;
import org.hyperledger.fabric.shim.ledger.QueryResultsIterator;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

@Contract(
        name = "ClearingHouseContract",
        info = @Info(
                title = "Clearing House Chaincode",
                description = "The Hyperlegendary Clearing House contract",
                version = "1.0.0-SNAPSHOT",
                license = @License(
                        name = "Apache 2.0 License",
                        url = "http://www.apache.org/licenses/LICENSE-2.0.html")))
@Default
public final class ClearingHouseContract implements ContractInterface {
    private static final Logger logger = Logger.getLogger(ClearingHouseContract.class.getName());
    private final Genson genson = new Genson();
    private boolean debug = false;
    private Map<String, String> debugMap;

    private enum CHErrors {
        MESSAGE_NOT_FOUND,
        MESSAGE_ALREADY_EXISTS
    }

    public ClearingHouseContract() {
    }

    public ClearingHouseContract(boolean debug) {
        this.debug = debug;
        if (debug) {
            debugMap = new HashMap<>();
        }
    }

    @Override
    public void unknownTransaction(Context ctx) {
        writeMethodInfo("unknownTransaction");
    }

    /**
     * @param context
     * @param notification
     * @return
     */
    @Transaction
    public String createNotificationContent(final Context context, String notification) throws Exception {
        ChaincodeStub stub = null;
        if (!debug)
            stub = context.getStub();
        writeMethodInfo("createNotificationContent");
        logger.info("NotificationContent arrived from JSONP Input ->\n" + notification);
        List<byte[]> args = null;
        if (!debug)
            args = stub.getArgs();
        NotificationContent deserializeLM = (NotificationContent) JsonHandler.convertFromJson(notification, NotificationContent.class);
        final String id = deserializeLM.getHeader().getId().toString();
        String notificationState = "";
        if (!debug)
            notificationState = stub.getStringState(id);
        else
            notificationState = debugMap.get(id);
        if (notificationState != null && !notificationState.isEmpty()) {
            String errorMessage = String.format("NotificationContent %s already exists", id);
            logger.severe(errorMessage);
            throw new ChaincodeException(errorMessage, CHErrors.MESSAGE_ALREADY_EXISTS.toString());
        }
        notificationState = JsonHandler.convertToJson(deserializeLM);
        if (!debug)
            stub.putStringState(id, notificationState);
        else
            debugMap.put(id, notificationState);
        logger.info("Message correctly inserted into Ledger :-)");
        return notificationState;
    }

    /**
     * @param ctx
     * @param id
     * @return
     */

    @Transaction
    public String getNotificationContent(final Context ctx, final String id) throws Exception {
        ChaincodeStub stub = null;
        if (!debug)
            stub = ctx.getStub();
        writeMethodInfo("getNotificationContent");
        logger.info("Trying to get NotificationContent with ID: " + id);
        String notificationState = null;
        if (!debug)
            notificationState = stub.getStringState(id);
        else
            notificationState = debugMap.get(id);
        if (notificationState == null || notificationState.isEmpty()) {
            String errorMessage = String.format("NotificationContent %s does not exist", id);
            logger.severe(errorMessage);
            throw new ChaincodeException(errorMessage, CHErrors.MESSAGE_NOT_FOUND.toString());
        }
        logger.info("NotificationContent retrieved\n: " + notificationState);
        return notificationState;
    }

    /**
     *
     * @param ctx
     * @param correlatedMessageId
     * @return
     * @throws Exception
     */

    @Transaction
    public String[] getNotificationContents(final Context ctx, final String correlatedMessageId) throws Exception {
        writeMethodInfo("getNotificationContents");
        ChaincodeStub stub = null;
        logger.info("Trying to get NotificationContents with Correlated Message-ID: " + correlatedMessageId);
        List<String> stringArgs = null;
        if (!debug) {
            stub = ctx.getStub();
            String QUERY_ALL ="{\n" +
                    "\"selector\": {" +
                        "\"body.header.@id\":"+"\""+correlatedMessageId+"\""+
                    "}\n" +
                    "}";
            final QueryResultsIterator<KeyValue> stateByRange = stub.getQueryResult(QUERY_ALL);
            if (null != stateByRange && stateByRange.iterator().hasNext()) {
                stringArgs = new ArrayList<>();
                for (KeyValue result : stateByRange) {
                    stringArgs.add(result.getStringValue());
                }
            }
        } else {
            stringArgs = new ArrayList<>(debugMap.values());
        }
        if (null == stringArgs || stringArgs.isEmpty()) {
            String errorMessage = String.format("NotificationContents for %s does not exist", correlatedMessageId);
            logger.severe(errorMessage);
            throw new ChaincodeException(errorMessage, CHErrors.MESSAGE_NOT_FOUND.toString());
        }
        return stringArgs.toArray(new String[stringArgs.size()]);

    }

    /**
     * @param ctx
     * @return
     */

    @Transaction
    public String[] getAllNotificationContents(final Context ctx) throws Exception {
        writeMethodInfo("getAllNotificationContents");
        ChaincodeStub stub = null;
        List<String> stringArgs = null;
        if (!debug) {
            stub = ctx.getStub();
            String QUERY_ALL ="{\n" +
                    "\"selector\": {}\n" +
                    "}";
            final QueryResultsIterator<KeyValue> stateByRange = stub.getQueryResult(QUERY_ALL);
            if (null != stateByRange && stateByRange.iterator().hasNext()) {
                logger.info((" stateByRange is FULL !!!"));
                stringArgs = new ArrayList<>();
                for (KeyValue result : stateByRange) {
                    logger.info((" stateByRange value " + result.getStringValue()));
                    stringArgs.add(result.getStringValue());
                }
            }
        } else {
            stringArgs = new ArrayList<>(debugMap.values());
        }
        if (null == stringArgs || stringArgs.isEmpty()) {
            String errorMessage = String.format("NotificationContents does not exist");
            logger.severe(errorMessage);
            throw new ChaincodeException(errorMessage, CHErrors.MESSAGE_NOT_FOUND.toString());
        }
        return stringArgs.toArray(new String[stringArgs.size()]);

    }

    private void writeMethodInfo(String methodName) {
        logger.info(this.getClass().getSimpleName() + " " + methodName + " called at " + LocalDateTime.now());
    }


}
