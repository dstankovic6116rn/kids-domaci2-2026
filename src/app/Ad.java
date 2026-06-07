package app;

import java.io.Serializable;

/**
 * Represents a product advertisement posted by a seller node.
 * The Ad is stored in the owner and backup is sent via chord
 */
public class Ad implements Serializable {

	private static final long serialVersionUID = 1L;

	private final int itemId;
	private final String name; // used as search key
	private final int qty;
	private final int ownerId;
	private final int ownerPort;

	public Ad(int itemId, String name, int qty, int ownerId, int ownerPort) {
		this.itemId = itemId;
		this.name = name;
		this.qty = qty;
		this.ownerId = ownerId;
		this.ownerPort = ownerPort;
	}

	public int getItemId() {
		return itemId;
	}

	public String getName() {
		return name;
	}

	public int getQty() {
		return qty;
	}

	public int getOwnerId() {
		return ownerId;
	}

	public int getOwnerPort() {
		return ownerPort;
	}
}
