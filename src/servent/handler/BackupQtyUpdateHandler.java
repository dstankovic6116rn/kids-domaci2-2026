package servent.handler;

import app.AppConfig;
import servent.message.BackupQtyUpdateMessage;
import servent.message.Message;
import servent.message.MessageType;

/**
 * Overwrite the backup ads quantity. Otherwise forward closer to the owner of
 * hash(itemId).
 */
public class BackupQtyUpdateHandler implements MessageHandler {

	private final Message clientMessage;

	public BackupQtyUpdateHandler(Message clientMessage) {
		this.clientMessage = clientMessage;
	}

	@Override
	public void run() {
		if (clientMessage.getMessageType() != MessageType.BACKUP_QTY_UPDATE) {
			AppConfig.timestampedErrorPrint("BackupQtyUpdateHandler got wrong type");
			return;
		}
		BackupQtyUpdateMessage msg = (BackupQtyUpdateMessage) clientMessage;
		AppConfig.chordState.handleBackupQtyUpdate(msg.getItemId(), msg.getNewQty());
	}
}
