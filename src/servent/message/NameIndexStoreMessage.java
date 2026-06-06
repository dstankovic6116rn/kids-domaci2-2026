package servent.message;

import app.NameIndexEntry;

/**
 * Sent by the owner of a new ad toward the node responsible for
 * hash(productName) in the Chord ring.  That node appends the NameIndexEntry
 * to its local name index, making the product discoverable via "search name".
 *
 * Routing: forwarded hop-by-hop (see NameIndexStoreHandler) until the message
 * reaches the node whose key range includes nameKey.
 *
 * nameKey is pre-computed by the sender as Math.floorMod(name.hashCode(), CHORD_SIZE)
 * and carried in the message so intermediate forwarding nodes don't have to
 * recompute it.
 */
public class NameIndexStoreMessage extends BasicMessage {

	private static final long serialVersionUID = 1L;

	// The index record to append (contains itemId, ownerId, ownerPort, name).
	private final NameIndexEntry entry;

	// Pre-computed Chord key for the product name: Math.floorMod(name.hashCode(), CHORD_SIZE).
	private final int nameKey;

	public NameIndexStoreMessage(int senderPort, int receiverPort, NameIndexEntry entry, int nameKey) {
		super(MessageType.NAME_INDEX_STORE, senderPort, receiverPort);
		this.entry = entry;
		this.nameKey = nameKey;
	}

	public NameIndexEntry getEntry() { return entry; }
	public int getNameKey() { return nameKey; }
}
