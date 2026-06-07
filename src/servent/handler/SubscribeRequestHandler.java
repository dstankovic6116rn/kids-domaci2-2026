package servent.handler;

import app.AppConfig;
import app.ServentInfo;
import servent.message.Message;
import servent.message.MessageType;

/**
 * Handles a SUBSCRIBE_REQUEST message on the publisher node.
 *
 * The subscriberss identity is taken from getSenderPort().
 */
public class SubscribeRequestHandler implements MessageHandler {

	private final Message clientMessage;

	public SubscribeRequestHandler(Message clientMessage) {
		this.clientMessage = clientMessage;
	}

	@Override
	public void run() {
		if (clientMessage.getMessageType() != MessageType.SUBSCRIBE_REQUEST) {
			AppConfig.timestampedErrorPrint("SubscribeRequestHandler got wrong type");
			return;
		}
		// Register the sender as a subscriber of this node.
		ServentInfo subscriber = new ServentInfo("localhost", clientMessage.getSenderPort());
		AppConfig.chordState.addSubscriber(subscriber);
		AppConfig.timestampedStandardPrint("Node " + subscriber.getChordId()
				+ " (port " + subscriber.getListenerPort() + ") subscribed to us.");
	}
}
