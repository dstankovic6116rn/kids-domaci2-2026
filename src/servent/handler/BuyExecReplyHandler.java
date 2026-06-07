package servent.handler;

import app.AppConfig;
import servent.message.BuyExecReplyMessage;
import servent.message.Message;
import servent.message.MessageType;

/**
 * Buyer side - owner has responded with success or OUT_OF_STOCK.
 * Prints the result line and exits the critical section.
 */
public class BuyExecReplyHandler implements MessageHandler {

	private final Message clientMessage;

	public BuyExecReplyHandler(Message clientMessage) {
		this.clientMessage = clientMessage;
	}

	@Override
	public void run() {
		if (clientMessage.getMessageType() != MessageType.BUY_EXEC_REPLY) {
			AppConfig.timestampedErrorPrint("BuyExecReplyHandler got wrong type");
			return;
		}
		BuyExecReplyMessage msg = (BuyExecReplyMessage) clientMessage;
		AppConfig.chordState.handleBuyExecReply(
				msg.getItemId(), msg.isSuccess(), msg.getQtyBought(), msg.getRemainingQty());
	}
}
