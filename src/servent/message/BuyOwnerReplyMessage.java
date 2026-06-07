package servent.message;

/**
 * Reply to BuyOwnerLookupMessage. Sent directly from the backup node back to
 * the buyer, originPort in the request.
 */
public class BuyOwnerReplyMessage extends BasicMessage {

	private static final long serialVersionUID = 1L;

	private final int itemId;
	private final int ownerPort;

	public BuyOwnerReplyMessage(int senderPort, int receiverPort, int itemId, int ownerPort) {
		super(MessageType.BUY_OWNER_REPLY, senderPort, receiverPort);
		this.itemId = itemId;
		this.ownerPort = ownerPort;
	}

	public int getItemId() {
		return itemId;
	}

	public int getOwnerPort() {
		return ownerPort;
	}
}
