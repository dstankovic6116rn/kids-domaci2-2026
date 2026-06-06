package servent.handler;

import app.AppConfig;
import servent.message.BuyOwnerLookupMessage;
import servent.message.Message;
import servent.message.MessageType;

/**
 * Routes a buyer's owner-lookup query toward the Chord-responsible backup
 * node.  Delegates the routing+reply decision to ChordState.
 */
public class BuyOwnerLookupHandler implements MessageHandler {

	private final Message clientMessage;

	public BuyOwnerLookupHandler(Message clientMessage) {
		this.clientMessage = clientMessage;
	}

	@Override
	public void run() {
		if (clientMessage.getMessageType() != MessageType.BUY_OWNER_LOOKUP) {
			AppConfig.timestampedErrorPrint("BuyOwnerLookupHandler got wrong type");
			return;
		}
		BuyOwnerLookupMessage msg = (BuyOwnerLookupMessage) clientMessage;
		AppConfig.chordState.handleBuyOwnerLookup(msg.getItemId(), msg.getOriginPort());
	}
}
