package servent.message;

/**
 * Sent by the owner after a successful buy, routed via Chord to the node
 * responsible for hash(itemId)
 */
public class BackupQtyUpdateMessage extends BasicMessage {

	private static final long serialVersionUID = 1L;

	private final int itemId;
	private final int newQty;

	public BackupQtyUpdateMessage(int senderPort, int receiverPort, int itemId, int newQty) {
		super(MessageType.BACKUP_QTY_UPDATE, senderPort, receiverPort);
		this.itemId = itemId;
		this.newQty = newQty;
	}

	public int getItemId() {
		return itemId;
	}

	public int getNewQty() {
		return newQty;
	}
}
