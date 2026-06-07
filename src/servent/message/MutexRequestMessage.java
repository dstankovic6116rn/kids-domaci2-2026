package servent.message;

/**
 * Suzuki-Kasami REQUEST itemId, requesterPort, seq.
 *
 * Broadcast by a buyer to every other node when it wants to enter the CS.
 * Whoever currently holds the token, after the update, may forward the
 * token to the requester.
 */
public class MutexRequestMessage extends BasicMessage {

	private static final long serialVersionUID = 1L;

	private final int itemId;
	private final int requesterPort;
	private final int seq;

	public MutexRequestMessage(int senderPort, int receiverPort, int itemId, int requesterPort, int seq) {
		super(MessageType.MUTEX_REQUEST, senderPort, receiverPort);
		this.itemId = itemId;
		this.requesterPort = requesterPort;
		this.seq = seq;
	}

	public int getItemId() {
		return itemId;
	}

	public int getRequesterPort() {
		return requesterPort;
	}

	public int getSeq() {
		return seq;
	}
}
