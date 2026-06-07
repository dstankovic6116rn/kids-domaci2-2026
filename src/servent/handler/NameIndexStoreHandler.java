package servent.handler;

import app.AppConfig;
import app.ServentInfo;
import servent.message.Message;
import servent.message.MessageType;
import servent.message.NameIndexStoreMessage;
import servent.message.util.MessageUtil;

/**
 * Handles a NAME_INDEX_STORE message.
 *
 * Append a NameIndexEntry to the name index on the node whose Chord key
 * range contains hash(productName). This makes the product findable by search.
 *
 * If THIS node owns nameKey -> append the entry to the local nameIndex map.
 * Otherwise -> forward one hop closer.
 */
public class NameIndexStoreHandler implements MessageHandler {

	private final Message clientMessage;

	public NameIndexStoreHandler(Message clientMessage) {
		this.clientMessage = clientMessage;
	}

	@Override
	public void run() {
		if (clientMessage.getMessageType() != MessageType.NAME_INDEX_STORE) {
			AppConfig.timestampedErrorPrint("NameIndexStoreHandler got wrong type");
			return;
		}
		NameIndexStoreMessage msg = (NameIndexStoreMessage) clientMessage;
		int nameKey = msg.getNameKey();

		if (AppConfig.chordState.isKeyMine(nameKey)) {
			// This node owns nameKey — add the entry to the local name index.
			AppConfig.chordState.appendNameIndex(nameKey, msg.getEntry());
		} else {
			// Not our key — forward one hop closer.
			ServentInfo next = AppConfig.chordState.getNextNodeForKey(nameKey);
			NameIndexStoreMessage fwd = new NameIndexStoreMessage(
					AppConfig.myServentInfo.getListenerPort(), next.getListenerPort(),
					msg.getEntry(), nameKey);
			MessageUtil.sendMessage(fwd);
		}
	}
}
