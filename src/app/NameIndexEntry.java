package app;

import java.io.Serializable;

/**
 * A single entry in the distributed name index.
 *
 * When a seller runs "list_item", the owner node sends one NameIndexEntry to
 * the node responsible for hash(productName).  That node appends the entry to
 * its nameIndex map, effectively building a DHT-backed lookup table:
 *
 *   hash(name)  -->  [ NameIndexEntry, NameIndexEntry, ... ]
 *
 * When a buyer runs "search apple", the system routes to the node that owns
 * hash("apple"), retrieves all NameIndexEntries stored there, and then
 * contacts each entry's owner directly to fetch the live Ad details.
 *
 * The name field is kept here (even though it can be inferred from context)
 * because multiple different product names can collide to the same Chord key
 * in a 64-slot ring.  Filtering by exact name during search prevents returning
 * unrelated products that happen to share the same hash slot.
 *
 * Serializable because instances travel inside ListItemIndexMessage and
 * SearchLookupReplyMessage over the network.
 */
public class NameIndexEntry implements Serializable {

	private static final long serialVersionUID = 1L;

	private final int itemId;    // ID of the listed product
	private final int ownerId;   // Chord ID of the node that owns the master Ad
	private final int ownerPort; // TCP port of the owner (used to send AdFetchMessage directly)
	private final String name;   // exact product name — needed to filter hash-slot collisions

	public NameIndexEntry(int itemId, int ownerId, int ownerPort, String name) {
		this.itemId = itemId;
		this.ownerId = ownerId;
		this.ownerPort = ownerPort;
		this.name = name;
	}

	public int getItemId() { return itemId; }
	public int getOwnerId() { return ownerId; }
	public int getOwnerPort() { return ownerPort; }
	public String getName() { return name; }
}
