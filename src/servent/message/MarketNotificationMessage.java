package servent.message;

/**
 * Sent by a publisher node to each of its subscribers whenever it creates a
 * new ad (list_item command).
 *
 * Carries only the publisher's Chord ID and the new item's ID — the minimal
 * information needed to print [MARKET-NOTIFICATION] node:X posted item_id:Y.
 *
 * Sent in one direct hop per subscriber (no Chord routing — subscriber ports
 * are known from the subscribers set).
 */
public class MarketNotificationMessage extends BasicMessage {

	private static final long serialVersionUID = 1L;

	// Chord ID of the node that listed the new item (printed as "node:X").
	private final int publisherChordId;

	// ID of the newly listed item (printed as "item_id:Y").
	private final int itemId;

	public MarketNotificationMessage(int senderPort, int receiverPort, int publisherChordId, int itemId) {
		super(MessageType.MARKET_NOTIFICATION, senderPort, receiverPort);
		this.publisherChordId = publisherChordId;
		this.itemId = itemId;
	}

	public int getPublisherChordId() { return publisherChordId; }
	public int getItemId() { return itemId; }
}
