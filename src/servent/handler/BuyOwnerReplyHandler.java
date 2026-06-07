package servent.handler;

import app.AppConfig;
import app.ItemMutexState;
import servent.message.BuyOwnerReplyMessage;
import servent.message.Message;
import servent.message.MessageType;

/**
 * Owner lookup reply received at the buyer. Stashes the resolved owner
 * port into the per item mutex state and proceeds with the Suzuki-Kasami
 * REQUEST.
 *
 * If the lookup failed print [MARKET-BUY-FAIL] and
 * clear the pending state and buy never enters the mutex.
 */
public class BuyOwnerReplyHandler implements MessageHandler {

	private final Message clientMessage;

	public BuyOwnerReplyHandler(Message clientMessage) {
		this.clientMessage = clientMessage;
	}

	@Override
	public void run() {
		if (clientMessage.getMessageType() != MessageType.BUY_OWNER_REPLY) {
			AppConfig.timestampedErrorPrint("BuyOwnerReplyHandler got wrong type");
			return;
		}
		BuyOwnerReplyMessage msg = (BuyOwnerReplyMessage) clientMessage;
		if (msg.getOwnerPort() == -1) {
			AppConfig.timestampedErrorPrint("Owner lookup failed for item " + msg.getItemId()
					+ " — no backup found.");
			// Clear pending state so the user can retry.
			ItemMutexState state = AppConfig.chordState.getOrCreateMutexState(msg.getItemId());
			synchronized (state) {
				state.myRequestPending = false;
				state.myPendingQty = 0;
			}
			return;
		}
		// Read qty out of state and kick off the mutex request.
		ItemMutexState state = AppConfig.chordState.getOrCreateMutexState(msg.getItemId());
		int qty;
		synchronized (state) {
			qty = state.myPendingQty;
		}
		AppConfig.chordState.requestBuyMutex(msg.getItemId(), qty, msg.getOwnerPort());
	}
}
