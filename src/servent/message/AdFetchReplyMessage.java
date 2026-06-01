package servent.message;

import app.Ad;

/**
 * Reply to an AdFetchMessage.  Sent directly from the owner node back to the
 * searching node (originPort from the request).
 *
 * ad can be null if the owner no longer has the item (e.g. it was sold or
 * removed between the index lookup and this fetch).  The receiver
 * (AdFetchReplyHandler) checks for null before printing the result.
 *
 * requestedItemId is kept separately so the receiver can log a meaningful
 * error even when ad is null.
 */
public class AdFetchReplyMessage extends BasicMessage {

	private static final long serialVersionUID = 1L;

	// The live ad from the owner's myAds map, or null if not found.
	private final Ad ad;

	// The itemId that was originally requested — used for error logging if ad == null.
	private final int requestedItemId;

	public AdFetchReplyMessage(int senderPort, int receiverPort, Ad ad, int requestedItemId) {
		super(MessageType.AD_FETCH_REPLY, senderPort, receiverPort);
		this.ad = ad;
		this.requestedItemId = requestedItemId;
	}

	public Ad getAd() { return ad; }
	public int getRequestedItemId() { return requestedItemId; }
}
