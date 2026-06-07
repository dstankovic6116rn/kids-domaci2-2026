package servent.message;

/**
 * Sent by a publisher node to each of its subscribers.
 * No Chord routing, subscriber ports are known from the subscribers set.
 */
public class MarketNotificationMessage extends BasicMessage {

	private static final long serialVersionUID = 1L;

	private final int publisherChordId;

	private final int itemId;

	public MarketNotificationMessage(int senderPort, int receiverPort, int publisherChordId, int itemId) {
		super(MessageType.MARKET_NOTIFICATION, senderPort, receiverPort);
		this.publisherChordId = publisherChordId;
		this.itemId = itemId;
	}

	public int getPublisherChordId() {
		return publisherChordId;
	}

	public int getItemId() {
		return itemId;
	}
}
