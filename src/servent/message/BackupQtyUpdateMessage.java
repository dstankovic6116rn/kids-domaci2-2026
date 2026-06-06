package servent.message;

/**
 * Sent by the owner after a successful buy, routed via Chord to the node
 * responsible for hash(itemId), to keep the backup copy's quantity in
 * sync with the master.
 *
 * If a fault-tolerance promotion ever happens (owner dies, backup takes
 * over), the backup is now up to date.  Forwarded hop-by-hop like
 * LIST_ITEM_BACKUP.
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

	public int getItemId() { return itemId; }
	public int getNewQty() { return newQty; }
}
