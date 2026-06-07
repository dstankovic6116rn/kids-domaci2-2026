package servent.message;

import app.NameIndexEntry;

/**
 * Sent by the owner of a new ad to the node responsible for
 * hash(productName) in the Chord ring. That node appends the NameIndexEntry
 * to its local name index, making the product discoverable for search.
 *
 * Forwarded hop-by-hop.
 */
public class NameIndexStoreMessage extends BasicMessage {

	private static final long serialVersionUID = 1L;

	// The index record to append (contains itemId, ownerId, ownerPort, name).
	private final NameIndexEntry entry;

	// Pre-computed Chord key for the product name: Math.floorMod(name.hashCode(),
	// CHORD_SIZE).
	private final int nameKey;

	public NameIndexStoreMessage(int senderPort, int receiverPort, NameIndexEntry entry, int nameKey) {
		super(MessageType.NAME_INDEX_STORE, senderPort, receiverPort);
		this.entry = entry;
		this.nameKey = nameKey;
	}

	public NameIndexEntry getEntry() {
		return entry;
	}

	public int getNameKey() {
		return nameKey;
	}
}
