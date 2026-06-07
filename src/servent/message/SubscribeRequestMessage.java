package servent.message;

public class SubscribeRequestMessage extends BasicMessage {

	private static final long serialVersionUID = 1L;

	public SubscribeRequestMessage(int senderPort, int receiverPort) {
		super(MessageType.SUBSCRIBE_REQUEST, senderPort, receiverPort);
	}
}
