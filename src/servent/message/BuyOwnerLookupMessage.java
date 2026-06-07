package servent.message;

/**
 * Sent by a buyer to the responsible backup node for an item, to
 * discover which node is the master owner. Routed hop-by-hop.
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

	public int getItemId() {
		return itemId;
	}

	public int getOriginPort() {
		return originPort;
	}
}
