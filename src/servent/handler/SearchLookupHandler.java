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
 * Goal: find the node that owns hash(productName) and have it return the
 * list of known sellers for that product.
 *
 * Decision logic:
 *   - If THIS node owns nameKey -> look up the local nameIndex, filter entries
 *     by exact product name (to handle hash collisions in a 64-slot ring),
 *     and send a SearchLookupReplyMessage directly back to the original
 *     searching node (originPort).  The reply goes in one direct hop, not
 *     back along the ring.
 *   - Otherwise -> forward the message one hop closer to the owner.
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
			// We own the name index for this key — collect matching entries.
			List<NameIndexEntry> all = AppConfig.chordState.getNameIndex(nameKey);
			List<NameIndexEntry> matches = new ArrayList<>();
			for (NameIndexEntry e : all) {
				// Filter by exact name: different strings can collide to the same nameKey.
				if (e.getName().equals(msg.getName())) {
					matches.add(e);
				}
			}
			// Send the list directly to the original searcher (may be empty).
			SearchLookupReplyMessage reply = new SearchLookupReplyMessage(
					AppConfig.myServentInfo.getListenerPort(), msg.getOriginPort(),
					msg.getName(), matches);
			MessageUtil.sendMessage(reply);
		} else {
			// Not our key — forward one hop closer along the ring.
			ServentInfo next = AppConfig.chordState.getNextNodeForKey(nameKey);
			SearchLookupMessage fwd = new SearchLookupMessage(
					AppConfig.myServentInfo.getListenerPort(), next.getListenerPort(),
					msg.getName(), nameKey, msg.getOriginPort());
			MessageUtil.sendMessage(fwd);
		}
	}
}
