package servent.handler;

import app.AppConfig;
import servent.message.MarketNotificationMessage;
import servent.message.Message;
import servent.message.MessageType;

/**
 * Handles a MARKET_NOTIFICATION message on a subscriber node.
 *
 * Received when a publisher node (one we subscribed to) creates a new ad.
 * Prints the required [MARKET-NOTIFICATION] line to stdout.
 */
public class MarketNotificationHandler implements MessageHandler {

	private final Message clientMessage;

	public MarketNotificationHandler(Message clientMessage) {
		this.clientMessage = clientMessage;
	}

	@Override
	public void run() {
		if (clientMessage.getMessageType() != MessageType.MARKET_NOTIFICATION) {
			AppConfig.timestampedErrorPrint("MarketNotificationHandler got wrong type");
			return;
		}
		MarketNotificationMessage msg = (MarketNotificationMessage) clientMessage;
		AppConfig.timestampedStandardPrint("[MARKET-NOTIFICATION] node:"
				+ msg.getPublisherChordId() + " posted item_id:" + msg.getItemId());
	}
}
