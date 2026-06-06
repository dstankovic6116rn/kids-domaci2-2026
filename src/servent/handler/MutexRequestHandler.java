package servent.handler;

import app.AppConfig;
import servent.message.Message;
import servent.message.MessageType;
import servent.message.MutexRequestMessage;

/**
 * Suzuki-Kasami REQUEST receipt: update RN[from] and conditionally forward
 * the token if we hold it.  All logic lives in ChordState.handleMutexRequest.
 */
public class MutexRequestHandler implements MessageHandler {

	private final Message clientMessage;

	public MutexRequestHandler(Message clientMessage) {
		this.clientMessage = clientMessage;
	}

	@Override
	public void run() {
		if (clientMessage.getMessageType() != MessageType.MUTEX_REQUEST) {
			AppConfig.timestampedErrorPrint("MutexRequestHandler got wrong type");
			return;
		}
		MutexRequestMessage msg = (MutexRequestMessage) clientMessage;
		AppConfig.chordState.handleMutexRequest(msg.getItemId(), msg.getRequesterPort(), msg.getSeq());
	}
}
