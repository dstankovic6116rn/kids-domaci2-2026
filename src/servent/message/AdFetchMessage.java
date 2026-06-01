package servent.message;

/**
 * Sent directly from the searching node to the known owner of a specific ad.
 * The owner replies with an AdFetchReplyMessage containing the live Ad data.
 *
 * Unlike Lookup messages, this does NOT use Chord routing — the owner's port
 * is already known from the NameIndexEntry, so we go straight to it.
 * originPort tells the owner where to send the reply.
 */
public class AdFetchMessage extends BasicMessage {

	private static final long serialVersionUID = 1L;

	// ID of the product whose live details are being requested.
	private final int itemId;

	// Port of the node that should receive the AdFetchReplyMessage.
	private final int originPort;

	public AdFetchMessage(int senderPort, int receiverPort, int itemId, int originPort) {
		super(MessageType.AD_FETCH, senderPort, receiverPort);
		this.itemId = itemId;
		this.originPort = originPort;
	}

	public int getItemId() { return itemId; }
	public int getOriginPort() { return originPort; }
}
