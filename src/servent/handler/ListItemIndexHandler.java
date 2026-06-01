package servent.handler;

import app.AppConfig;
import app.ServentInfo;
import servent.message.ListItemIndexMessage;
import servent.message.Message;
import servent.message.MessageType;
import servent.message.util.MessageUtil;

/**
 * Handles a LIST_ITEM_INDEX message.
 *
 * Goal: append a NameIndexEntry to the name index on the node whose Chord key
 * range contains hash(productName).  This makes the product findable by
 * "search name" — the search command routes to the same key.
 *
 * Decision logic:
 *   - If THIS node owns nameKey -> append the entry to the local nameIndex map.
 *   - Otherwise -> forward one hop closer using getNextNodeForKey.
 *
 * Same Chord forwarding pattern as ListItemBackupHandler, just for a different
 * key (hash of the product name instead of hash of the itemId).
 */
public class ListItemIndexHandler implements MessageHandler {

	private final Message clientMessage;

	public ListItemIndexHandler(Message clientMessage) {
		this.clientMessage = clientMessage;
	}

	@Override
	public void run() {
		if (clientMessage.getMessageType() != MessageType.LIST_ITEM_INDEX) {
			AppConfig.timestampedErrorPrint("ListItemIndexHandler got wrong type");
			return;
		}
		ListItemIndexMessage msg = (ListItemIndexMessage) clientMessage;
		int nameKey = msg.getNameKey();

		if (AppConfig.chordState.isKeyMine(nameKey)) {
			// This node owns nameKey — add the entry to the local name index.
			AppConfig.chordState.appendNameIndex(nameKey, msg.getEntry());
		} else {
			// Not our key — forward one hop closer.
			ServentInfo next = AppConfig.chordState.getNextNodeForKey(nameKey);
			ListItemIndexMessage fwd = new ListItemIndexMessage(
					AppConfig.myServentInfo.getListenerPort(), next.getListenerPort(),
					msg.getEntry(), nameKey);
			MessageUtil.sendMessage(fwd);
		}
	}
}
