package servent;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import app.AppConfig;
import app.Cancellable;
import servent.handler.AdFetchHandler;
import servent.handler.AdFetchReplyHandler;
import servent.handler.AskGetHandler;
import servent.handler.ListItemBackupHandler;
import servent.handler.ListItemIndexHandler;
import servent.handler.MarketNotificationHandler;
import servent.handler.MessageHandler;
import servent.handler.NewNodeHandler;
import servent.handler.NullHandler;
import servent.handler.PutHandler;
import servent.handler.SearchLookupHandler;
import servent.handler.SearchLookupReplyHandler;
import servent.handler.SorryHandler;
import servent.handler.SubscribeRequestHandler;
import servent.handler.TellGetHandler;
import servent.handler.UpdateHandler;
import servent.handler.WelcomeHandler;
import servent.message.Message;
import servent.message.util.MessageUtil;

public class SimpleServentListener implements Runnable, Cancellable {

	private volatile boolean working = true;
	
	public SimpleServentListener() {
		
	}

	/*
	 * Thread pool for executing the handlers. Each client will get it's own handler thread.
	 */
	private final ExecutorService threadPool = Executors.newWorkStealingPool();
	
	@Override
	public void run() {
		ServerSocket listenerSocket = null;
		try {
			listenerSocket = new ServerSocket(AppConfig.myServentInfo.getListenerPort(), 100);
			/*
			 * If there is no connection after 1s, wake up and see if we should terminate.
			 */
			listenerSocket.setSoTimeout(1000);
		} catch (IOException e) {
			AppConfig.timestampedErrorPrint("Couldn't open listener socket on: " + AppConfig.myServentInfo.getListenerPort());
			System.exit(0);
		}
		
		
		while (working) {
			try {
				Message clientMessage;
				
				Socket clientSocket = listenerSocket.accept();
				
				//GOT A MESSAGE! <3
				clientMessage = MessageUtil.readMessage(clientSocket);
				
				MessageHandler messageHandler = new NullHandler(clientMessage);
				
				/*
				 * Each message type has it's own handler.
				 * If we can get away with stateless handlers, we will,
				 * because that way is much simpler and less error prone.
				 */
				switch (clientMessage.getMessageType()) {
				case NEW_NODE:
					messageHandler = new NewNodeHandler(clientMessage);
					break;
				case WELCOME:
					messageHandler = new WelcomeHandler(clientMessage);
					break;
				case SORRY:
					messageHandler = new SorryHandler(clientMessage);
					break;
				case UPDATE:
					messageHandler = new UpdateHandler(clientMessage);
					break;
				case PUT:
					messageHandler = new PutHandler(clientMessage);
					break;
				case ASK_GET:
					messageHandler = new AskGetHandler(clientMessage);
					break;
				case TELL_GET:
					messageHandler = new TellGetHandler(clientMessage);
					break;
				case LIST_ITEM_BACKUP:
					messageHandler = new ListItemBackupHandler(clientMessage);
					break;
				case LIST_ITEM_INDEX:
					messageHandler = new ListItemIndexHandler(clientMessage);
					break;
				case SEARCH_LOOKUP:
					messageHandler = new SearchLookupHandler(clientMessage);
					break;
				case SEARCH_LOOKUP_REPLY:
					messageHandler = new SearchLookupReplyHandler(clientMessage);
					break;
				case AD_FETCH:
					messageHandler = new AdFetchHandler(clientMessage);
					break;
				case AD_FETCH_REPLY:
					messageHandler = new AdFetchReplyHandler(clientMessage);
					break;
				case SUBSCRIBE_REQUEST:
					messageHandler = new SubscribeRequestHandler(clientMessage);
					break;
				case MARKET_NOTIFICATION:
					messageHandler = new MarketNotificationHandler(clientMessage);
					break;
				case POISON:
					break;
				}
				
				threadPool.submit(messageHandler);
			} catch (SocketTimeoutException timeoutEx) {
				//Uncomment the next line to see that we are waking up every second.
//				AppConfig.timedStandardPrint("Waiting...");
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void stop() {
		this.working = false;
	}

}
