package app;

import java.io.Serializable;

/**
 * A single entry in the distributed name index.
 *
 * When a seller runs "list_item", the owner node sends one NameIndexEntry to
 * the node responsible for hash(productName). That node appends the entry to
 * its nameIndex map, effectively building a DHT-backed lookup table.
 *
 * When a buyer runs "search apple", the system routes to the node that owns
 * hash("apple"), retrieves all NameIndexEntries stored there, and then
 * contacts each entry's owner directly to fetch the live Ad details.
 */
public class NameIndexEntry implements Serializable {

	private static final long serialVersionUID = 1L;

	private final int itemId;
	private final int ownerId;
	private final int ownerPort;
	private final String name;

	public NameIndexEntry(int itemId, int ownerId, int ownerPort, String name) {
		this.itemId = itemId;
		this.ownerId = ownerId;
		this.ownerPort = ownerPort;
		this.name = name;
	}

	public int getItemId() {
		return itemId;
	}

	public int getOwnerId() {
		return ownerId;
	}

	public int getOwnerPort() {
		return ownerPort;
	}

	public String getName() {
		return name;
	}
}
