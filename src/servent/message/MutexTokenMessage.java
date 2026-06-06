package servent.message;

import app.MutexToken;

/**
 * Carries a Suzuki-Kasami token from the previous holder to the next
 * grantee.  Sent in one direct hop — no Chord routing involved.
 *
 * Receipt of this message at the buyer is the trigger to enter the
 * critical section.
 */
public class MutexTokenMessage extends BasicMessage {

	private static final long serialVersionUID = 1L;

	private final MutexToken token;

	public MutexTokenMessage(int senderPort, int receiverPort, MutexToken token) {
		super(MessageType.MUTEX_TOKEN, senderPort, receiverPort);
		this.token = token;
	}

	public MutexToken getToken() { return token; }
}
