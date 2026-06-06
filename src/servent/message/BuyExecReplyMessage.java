package servent.message;

/**
 * Owner's response to BuyExecMessage.
 *
 *   success == true  -> qty was sufficient, owner has decremented the master
 *                       copy.  remainingQty is the new stock value.
 *   success == false -> qty was insufficient; nothing changed.  The buyer
 *                       prints [MARKET-BUY-FAIL] item_id:X reason:OUT_OF_STOCK.
 */
public class BuyExecReplyMessage extends BasicMessage {

	private static final long serialVersionUID = 1L;

	private final int itemId;
	private final int qtyBought;
	private final int remainingQty;
	private final boolean success;

	public BuyExecReplyMessage(int senderPort, int receiverPort, int itemId,
			int qtyBought, int remainingQty, boolean success) {
		super(MessageType.BUY_EXEC_REPLY, senderPort, receiverPort);
		this.itemId = itemId;
		this.qtyBought = qtyBought;
		this.remainingQty = remainingQty;
		this.success = success;
	}

	public int getItemId() { return itemId; }
	public int getQtyBought() { return qtyBought; }
	public int getRemainingQty() { return remainingQty; }
	public boolean isSuccess() { return success; }
}
