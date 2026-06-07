package servent.handler;

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
 * Searching node has received the list of (itemId, ownerPort)
 * pairs that match the searched product name. Now it needs the live
 * details from each owner.
 * 
 * Send an AdFetchMessage directly to the owners port.
 * The owner will reply with an AdFetchReplyMessage.
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
			// Always ask the owner with AD_FETCH even when the owner is
			// self.
			AdFetchMessage fetch = new AdFetchMessage(myPort, entry.getOwnerPort(),
					entry.getItemId(), myPort);
			MessageUtil.sendMessage(fetch);
		}
	}
}
