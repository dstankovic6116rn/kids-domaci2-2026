package app;

import java.io.Serializable;

/**
 * Represents a product advertisement posted by a seller node.
 *
 * A node creates an Ad when the user runs the "list_item" command.
 * The Ad is stored in two places:
 *   - the OWNER node keeps the master copy in its myAds map
 *   - a BACKUP copy is sent (via Chord routing) to the node responsible
 *     for the key hash(itemId), for fault tolerance
 *
 * Serializable because Ad objects are sent over the network inside messages
 * (Java ObjectOutputStream serializes the entire message including its fields).
 */
public class Ad implements Serializable {

	private static final long serialVersionUID = 1L;

	private final int itemId;    // user-supplied unique ID for this product listing
	private final String name;   // product name (used as search key)
	private final int qty;       // quantity available for sale
	private final int ownerId;   // Chord ID of the node that created this ad
	private final int ownerPort; // TCP port of the owner node (used to contact it directly)

	public Ad(int itemId, String name, int qty, int ownerId, int ownerPort) {
		this.itemId = itemId;
		this.name = name;
		this.qty = qty;
		this.ownerId = ownerId;
		this.ownerPort = ownerPort;
	}

	public int getItemId() { return itemId; }
	public String getName() { return name; }
	public int getQty() { return qty; }
	public int getOwnerId() { return ownerId; }
	public int getOwnerPort() { return ownerPort; }
}
