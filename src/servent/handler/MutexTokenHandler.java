package servent.handler;

import app.AppConfig;
import servent.message.Message;
import servent.message.MessageType;
import servent.message.MutexTokenMessage;

/**
 * Token arrival — stash it and, if a buy is pending, enter the CS.
 */
public class MutexTokenHandler implements MessageHandler {

	private final Message clientMessage;

	public MutexTokenHandler(Message clientMessage) {
		this.clientMessage = clientMessage;
	}

	@Override
	public void run() {
		if (clientMessage.getMessageType() != MessageType.MUTEX_TOKEN) {
			AppConfig.timestampedErrorPrint("MutexTokenHandler got wrong type");
			return;
		}
		MutexTokenMessage msg = (MutexTokenMessage) clientMessage;
		AppConfig.chordState.handleTokenReceived(msg.getToken().getItemId(), msg.getToken());
	}
}
