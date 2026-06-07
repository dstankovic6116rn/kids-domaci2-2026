package servent.handler;

import java.util.ArrayList;
import java.util.List;

import app.AppConfig;
import app.NameIndexEntry;
import app.ServentInfo;
import servent.message.Message;
import servent.message.MessageType;
import servent.message.SearchLookupMessage;
import servent.message.SearchLookupReplyMessage;
import servent.message.util.MessageUtil;

/**
 * Handles a SEARCH_LOOKUP message.
 *
 * Find the node that owns hash(productName) and it should return the
 * list of known sellers for the product.
 *
 * If THIS node owns nameKey -> look up the local nameIndex,
 * filter by product name and send a SearchLookupReplyMessage directly back to
 * the original searching node (originPort).
 * The reply goes in one direct hop, not along the ring.
 * 
 * Otherwise -> forward the message one hop closer.
 */
public class SearchLookupHandler implements MessageHandler {

	private final Message clientMessage;

	public SearchLookupHandler(Message clientMessage) {
		this.clientMessage = clientMessage;
	}

	@Override
	public void run() {
		if (clientMessage.getMessageType() != MessageType.SEARCH_LOOKUP) {
			AppConfig.timestampedErrorPrint("SearchLookupHandler got wrong type");
			return;
		}
		SearchLookupMessage msg = (SearchLookupMessage) clientMessage;
		int nameKey = msg.getNameKey();

		if (AppConfig.chordState.isKeyMine(nameKey)) {
			// We own the name index for this key —> collect matching entries.
			List<NameIndexEntry> all = AppConfig.chordState.getNameIndex(nameKey);
			List<NameIndexEntry> matches = new ArrayList<>();
			for (NameIndexEntry e : all) {
				// Filter by exact name.
				if (e.getName().equals(msg.getName())) {
					matches.add(e);
				}
			}
			// Send the list directly to the original searcher.
			SearchLookupReplyMessage reply = new SearchLookupReplyMessage(
					AppConfig.myServentInfo.getListenerPort(), msg.getOriginPort(),
					msg.getName(), matches);
			MessageUtil.sendMessage(reply);
		} else {
			// Not our key —> forward one hop closer on the ring.
			ServentInfo next = AppConfig.chordState.getNextNodeForKey(nameKey);
			SearchLookupMessage fwd = new SearchLookupMessage(
					AppConfig.myServentInfo.getListenerPort(), next.getListenerPort(),
					msg.getName(), nameKey, msg.getOriginPort());
			MessageUtil.sendMessage(fwd);
		}
	}
}
