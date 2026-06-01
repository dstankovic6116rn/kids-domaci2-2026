package servent.handler;

import app.AppConfig;
import app.ServentInfo;
import servent.message.Message;
import servent.message.MessageType;

/**
 * Handles a SUBSCRIBE_REQUEST message on the publisher node.
 *
 * When node B wants to follow node A, B sends this message to A.
 * A registers B's ServentInfo in its local subscribers set so that
 * future list_item calls on A will notify B.
 *
 * The subscriber's identity is taken from getSenderPort() — no extra
 * payload needed since all nodes are on localhost.
 *
 * Duplicate subscriptions are silently ignored (Set semantics in ChordState).
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
