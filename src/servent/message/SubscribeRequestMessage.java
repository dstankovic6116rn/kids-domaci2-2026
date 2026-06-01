package servent.message;

/**
 * Sent by a subscriber node directly to the publisher node it wants to follow.
 *
 * The subscriber runs: subscribe localhost:PORT
 * This message is sent from the subscriber to PORT.
 *
 * No extra payload needed — senderPort already identifies the subscriber.
 * The receiving node (SubscribeRequestHandler) extracts getSenderPort() and
 * stores a new ServentInfo for it in its local subscribers set.
 */
public class SubscribeRequestMessage extends BasicMessage {

	private static final long serialVersionUID = 1L;

	public SubscribeRequestMessage(int senderPort, int receiverPort) {
		super(MessageType.SUBSCRIBE_REQUEST, senderPort, receiverPort);
	}
}
