package servent.handler;

import app.Ad;
import app.AppConfig;
import servent.message.AdFetchReplyMessage;
import servent.message.Message;
import servent.message.MessageType;

/**
 * Handles an AD_FETCH_REPLY message on the searching node.
 *
 * This is the last step of a search: the owner has responded with the live
 * ad data.  We simply print the [MARKET-SEARCH-RESULT] line to stdout.
 *
 * If ad is null the owner no longer holds that item, so we print an error
 * instead (item may have been sold or the node restarted between index
 * lookup and this fetch — acceptable in Phase 1).
 */
public class AdFetchReplyHandler implements MessageHandler {

	private final Message clientMessage;

	public AdFetchReplyHandler(Message clientMessage) {
		this.clientMessage = clientMessage;
	}

	@Override
	public void run() {
		if (clientMessage.getMessageType() != MessageType.AD_FETCH_REPLY) {
			AppConfig.timestampedErrorPrint("AdFetchReplyHandler got wrong type");
			return;
		}
		AdFetchReplyMessage msg = (AdFetchReplyMessage) clientMessage;
		Ad ad = msg.getAd();

		if (ad == null) {
			// Owner no longer holds this item — stale index entry.
			AppConfig.timestampedErrorPrint("Owner no longer has item " + msg.getRequestedItemId());
			return;
		}

		// Print the search result in the required format.
		AppConfig.timestampedStandardPrint("[MARKET-SEARCH-RESULT] item_id:"
				+ ad.getItemId() + " name:\"" + ad.getName() + "\" qty:"
				+ ad.getQty() + " owner_id:" + ad.getOwnerId());
	}
}
