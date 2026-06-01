package servent.message;

import app.Ad;

/**
 * Sent by the owner of an ad toward the node that is responsible for
 * hash(itemId) in the Chord ring.  That node stores the Ad as a backup copy.
 *
 * Routing: the message is forwarded hop-by-hop (see ListItemBackupHandler)
 * until it reaches the node whose key range includes hash(itemId).
 *
 * The Ad payload is carried as a typed Serializable field rather than encoded
 * in messageText, so product names with special characters never break parsing.
 */
public class ListItemBackupMessage extends BasicMessage {

	private static final long serialVersionUID = 1L;

	// The full ad to be stored as a backup on the destination node.
	private final Ad ad;

	public ListItemBackupMessage(int senderPort, int receiverPort, Ad ad) {
		super(MessageType.LIST_ITEM_BACKUP, senderPort, receiverPort);
		this.ad = ad;
	}

	public Ad getAd() { return ad; }
}
