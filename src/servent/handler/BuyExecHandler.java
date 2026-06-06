package servent.handler;

import app.AppConfig;
import servent.message.BuyExecMessage;
import servent.message.Message;
import servent.message.MessageType;

/**
 * Owner-side execution of a buyer's BUY_EXEC.  Atomically checks stock and
 * replies with success or OUT_OF_STOCK via BUY_EXEC_REPLY.
 *
 * The mutex guarantees only one in-flight BUY_EXEC per item at a time, so
 * no additional locking is needed here.
 */
public class BuyExecHandler implements MessageHandler {

	private final Message clientMessage;

	public BuyExecHandler(Message clientMessage) {
		this.clientMessage = clientMessage;
	}

	@Override
	public void run() {
		if (clientMessage.getMessageType() != MessageType.BUY_EXEC) {
			AppConfig.timestampedErrorPrint("BuyExecHandler got wrong type");
			return;
		}
		BuyExecMessage msg = (BuyExecMessage) clientMessage;
		AppConfig.chordState.executeBuyAtOwner(msg.getItemId(), msg.getQty(), msg.getOriginPort());
	}
}
