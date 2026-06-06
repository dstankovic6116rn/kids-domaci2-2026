package servent.message;

/**
 * Sent by a buyer toward the Chord-responsible backup node for an item, to
 * discover which node is the master owner.  Routed hop-by-hop until it
 * reaches the node that holds the backup copy of the ad.
 *
 * originPort is the buyer (carried through forwards so the reply can come
 * back in one direct hop).
 */
public class BuyOwnerLookupMessage extends BasicMessage {

	private static final long serialVersionUID = 1L;

	private final int itemId;
	private final int originPort;

	public BuyOwnerLookupMessage(int senderPort, int receiverPort, int itemId, int originPort) {
		super(MessageType.BUY_OWNER_LOOKUP, senderPort, receiverPort);
		this.itemId = itemId;
		this.originPort = originPort;
	}

	public int getItemId() { return itemId; }
	public int getOriginPort() { return originPort; }
}
