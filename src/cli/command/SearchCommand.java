package cli.command;

import app.AppConfig;
import app.ChordState;
import app.ServentInfo;
import servent.message.SearchLookupMessage;
import servent.message.util.MessageUtil;

/**
 * search [name]
 *
 * Results arrive non-FIFO so [MARKET-SEARCH-RESULT] may get tangled in other
 * output
 */
public class SearchCommand implements CLICommand {

	@Override
	public String commandName() {
		return "search";
	}

	@Override
	public void execute(String args) {
		if (args == null || args.isEmpty()) {
			AppConfig.timestampedErrorPrint("Usage: search [name]");
			return;
		}
		String name = args.trim();

		// Compute the Chord key for the product name same as list_item.
		int nameKey = ChordState.keyHash(name.hashCode());
		int myPort = AppConfig.myServentInfo.getListenerPort();

		// route SEARCH_LOOKUP through the network
		ServentInfo next = AppConfig.chordState.getNextNodeForKey(nameKey);
		MessageUtil.sendMessage(new SearchLookupMessage(myPort, next.getListenerPort(),
				name, nameKey, myPort));
	}
}
