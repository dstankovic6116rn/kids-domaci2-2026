package servent.message;

import app.Ad;

/**
 * Sent by the owner of an ad toward the node that is responsible for
 * hash(itemId) in the Chord ring.
 * Forwarded hop-by-hop.
 */
public class ListItemBackupMessage extends BasicMessage {

	private static final long serialVersionUID = 1L;

	// The full ad to be stored as a backup on the destination node.
	private final Ad ad;

	public ListItemBackupMessage(int senderPort, int receiverPort, Ad ad) {
		super(MessageType.LIST_ITEM_BACKUP, senderPort, receiverPort);
		this.ad = ad;
	}

	public Ad getAd() {
		return ad;
	}
}
