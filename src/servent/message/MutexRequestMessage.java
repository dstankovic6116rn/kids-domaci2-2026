package servent.message;

/**
 * Suzuki-Kasami REQUEST(itemId, requesterPort, seq).
 *
 * Broadcast by a buyer to every other node when it wants to enter the CS
 * for `itemId`.  Each receiver updates its local RN[requesterPort] = max(...).
 * Whoever currently holds the token, after the update, may forward the
 * token to the requester if the requester's seq matches LN[requester]+1
 * (Suzuki-Kasami forwarding condition).
 *
 * requesterPort is carried explicitly even though it usually equals
 * senderPort — this way the message stays self-contained and immune to
 * intermediate-rewrite bugs.
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

	public int getItemId() { return itemId; }
	public int getRequesterPort() { return requesterPort; }
	public int getSeq() { return seq; }
}
