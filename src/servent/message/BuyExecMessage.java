package servent.message;

/**
 * Sent by the buyer in its critical section directly to the item
 * owner to decrement stock.
 */
public class BuyExecMessage extends BasicMessage {

	private static final long serialVersionUID = 1L;

	private final int itemId;
	private final int qty;
	private final int originPort;

	public BuyExecMessage(int senderPort, int receiverPort, int itemId, int qty, int originPort) {
		super(MessageType.BUY_EXEC, senderPort, receiverPort);
		this.itemId = itemId;
		this.qty = qty;
		this.originPort = originPort;
	}

	public int getItemId() {
		return itemId;
	}

	public int getQty() {
		return qty;
	}

	public int getOriginPort() {
		return originPort;
	}
}
