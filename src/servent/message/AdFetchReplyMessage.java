package servent.message;

import app.Ad;

/**
 * Reply to an AdFetchMessage. Sent directly from the owner node back to the
 * searching node (originPort from the request).
 *
 * ad can be null if the owner no longer has the item.
 */
public class AdFetchReplyMessage extends BasicMessage {

	private static final long serialVersionUID = 1L;

	// Live ad
	private final Ad ad;

	// The itemId that was originally requested
	private final int requestedItemId;

	public AdFetchReplyMessage(int senderPort, int receiverPort, Ad ad, int requestedItemId) {
		super(MessageType.AD_FETCH_REPLY, senderPort, receiverPort);
		this.ad = ad;
		this.requestedItemId = requestedItemId;
	}

	public Ad getAd() {
		return ad;
	}

	public int getRequestedItemId() {
		return requestedItemId;
	}
}
