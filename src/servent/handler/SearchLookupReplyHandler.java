package servent.handler;

import app.Ad;
import app.AppConfig;
import app.NameIndexEntry;
import servent.message.AdFetchMessage;
import servent.message.Message;
import servent.message.MessageType;
import servent.message.SearchLookupReplyMessage;
import servent.message.util.MessageUtil;

/**
 * Handles a SEARCH_LOOKUP_REPLY message on the searching node.
 *
 * At this point the searching node has received the list of (itemId, ownerPort)
 * pairs that match the searched product name.  Now it needs the live ad
 * details (current quantity, etc.) from each owner.
 *
 * For each entry in the reply:
 *   - If the owner is THIS node -> look up the ad locally and print immediately.
 *     (Avoids a network round-trip for ads we ourselves listed.)
 *   - Otherwise -> send an AdFetchMessage directly to the owner's port.
 *     The owner will reply with an AdFetchReplyMessage, which
 *     AdFetchReplyHandler will pick up and print.
 *
 * Messages arrive out of order (non-FIFO) so the printed results may appear
 * interleaved with other output lines — that is expected.
 */
public class SearchLookupReplyHandler implements MessageHandler {

	private final Message clientMessage;

	public SearchLookupReplyHandler(Message clientMessage) {
		this.clientMessage = clientMessage;
	}

	@Override
	public void run() {
		if (clientMessage.getMessageType() != MessageType.SEARCH_LOOKUP_REPLY) {
			AppConfig.timestampedErrorPrint("SearchLookupReplyHandler got wrong type");
			return;
		}
		SearchLookupReplyMessage msg = (SearchLookupReplyMessage) clientMessage;
		int myPort = AppConfig.myServentInfo.getListenerPort();

		for (NameIndexEntry entry : msg.getEntries()) {
			if (entry.getOwnerPort() == myPort) {
				// Owner is us — no network hop needed, print directly.
				Ad ad = AppConfig.chordState.getMyAd(entry.getItemId());
				if (ad != null) {
					AppConfig.timestampedStandardPrint("[MARKET-SEARCH-RESULT] item_id:"
							+ ad.getItemId() + " name:\"" + ad.getName() + "\" qty:"
							+ ad.getQty() + " owner_id:" + ad.getOwnerId());
				}
			} else {
				// Owner is a remote node — ask it for the live ad.
				AdFetchMessage fetch = new AdFetchMessage(myPort, entry.getOwnerPort(),
						entry.getItemId(), myPort);
				MessageUtil.sendMessage(fetch);
			}
		}
	}
}
