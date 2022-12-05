package matchpackage.access;

import javax.swing.SwingUtilities;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.lang.acl.ACLMessage;
import matchpackage.application.EnhancedAgent;
import matchpackage.application.ProviderGUI;
import matchpackage.contract.ProviderChatGUI;
import matchpackage.contract.ProviderFeedbackGUI;
import matchpackage.contract.ProviderProjectGUI;
import matchpackage.database.Provider;

public class ProviderAgent extends EnhancedAgent {

	//Declaring provider project and chat GUI
	private ProviderProjectGUI providerProjectGUI;
	private ProviderChatGUI providerChatGUI;
	
	ProviderGUI providerGUI;
	Provider provider;
	ProviderFeedbackGUI providerFeedbackGUI;
	
	//Declaring named string values
	String trackValues = "PENDING";
	String endProject = "PENDING";
	String changeRequest = "PENDING";
	String contractDecision = "PENDING";
	String customerName = "";
	String bidValue;
	String decision = "PENDING";
	
	int caseValue = 0;
	
	//Creating the agent

	protected void setup() {
		createAgent("Bidding", "matchpackage.contract.BiddingAgent");
		providerGUI = new ProviderGUI(this);
		System.out.printf("Hello! My name is %s%n", getLocalName());
		addBehaviour(new ShowGUIProvider(this, 2000));
	}
	
	//setting the visibility of feedback window as false so that it is closed.

	public void closeFeedbackWindow()

	{
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				providerFeedbackGUI.setVisible(false);

			}
		});

	}
	
	//Updating the tracker
	public void updateTracker(ProviderAgent a) {
		trackValues = "DONE";
	}

	//Change Request
	public void afterChangeRequest(String text) {
		this.changeRequest = text;
	}

	//End of Project
	public void endProject() {

		endProject = "END";
		providerFeedbackGUI = new ProviderFeedbackGUI(this);
	}


	//Show the GUI of provider
	public void showGUI() {

		this.providerGUI.showGUI();
	}

	//After clicking on bid the decision is made and displayed
	public void afterBidClick(String text) {
		decision = text;
	}

	//After clicking on contract, contractDecision is displayed
	public void afterContractClick(String text) {
		contractDecision = text;
	}

	//Feedback GUI opened
	public void openFeedbackGUI() {

	}

	
	private class ShowGUIProvider extends TickerBehaviour {

		ProviderAgent providerAgent;

		ShowGUIProvider(Agent a, long period) {

			super(a, period);
			providerAgent = (ProviderAgent) a;
		}

		@Override
		protected void onTick() {

			switch (caseValue) {

			case 0:

				ACLMessage msg = myAgent.blockingReceive();

				System.out.println("I am inside Provider agent");
				if (msg.getContent().contentEquals("Open GUI")) {
					System.out.println("Am i reaching here");
					SwingUtilities.invokeLater(new Runnable() {
						@Override
						public void run() {
							providerGUI.showGUI();

						}
					});
				}

				caseValue = 1;
				break;

			case 1:

				ACLMessage msg1 = myAgent.blockingReceive();

				if (msg1.getPerformative() == ACLMessage.PROPOSE) {

					customerName = msg1.getSender().getLocalName();

					System.out.println("Proposal has been received by me");

					bidValue = msg1.getContent();
					double price = Double.parseDouble(bidValue);
					String text = customerName + bidValue;
					providerGUI.setBidText(text);

					System.out.println("I am giving the bid value");
					System.out.println("--------------------------------------------------------------------------------------");

				}

				caseValue = 2;
				break;

			case 2:

				if (!(decision.contentEquals("PENDING"))) {

					System.out.println("Action of ProviderAgent");
					if (decision.contentEquals("Accept")) {
						ACLMessage msgAccept = new ACLMessage(ACLMessage.ACCEPT_PROPOSAL);
						msgAccept.addReceiver(new AID("Bidding", AID.ISLOCALNAME));
						msgAccept.setContent(customerName);
						send(msgAccept);
						caseValue = 3;
					}

					if (decision.contentEquals("Reject")) {
						System.out.println("Action of Provider Agent");
						ACLMessage bidMsgReply = new ACLMessage(ACLMessage.REJECT_PROPOSAL);
						bidMsgReply.addReceiver(new AID(customerName, AID.ISLOCALNAME));
						bidMsgReply.setContent("Bid is rejected");
						send(bidMsgReply);
						caseValue = 1;
						decision = "PENDING";
					}

				}
				break;

			case 3:

				ACLMessage msgContract = myAgent.blockingReceive();
				if (msgContract != null) {
					if (msgContract.getPerformative() == ACLMessage.PROPOSE) {
						providerGUI.setContract(msgContract.getContent());
					}
				}

				caseValue = 4;

				break;

			case 4:

				if (!(contractDecision.contentEquals("PENDING"))) {

					System.out.println("ProviderAgent: Contract Action");
					if (decision.contentEquals("Accept")) {
						System.out.println("Pending Decision");
						ACLMessage msgAcceptContract = new ACLMessage(ACLMessage.INFORM);
						msgAcceptContract.addReceiver(new AID("Bidding", AID.ISLOCALNAME));
						msgAcceptContract.setContent("ACCEPT");
						send(msgAcceptContract);
						contractDecision = "PENDING";
						caseValue = 1;
						decision = "PENDING";
					}

					if (decision.contentEquals("Reject")) {
						System.out.println("I am in here as well");
						ACLMessage msgRejectContract = new ACLMessage(ACLMessage.INFORM);
						msgRejectContract.addReceiver(new AID("Bidding", AID.ISLOCALNAME));
						msgRejectContract.setContent("REJECT");
						send(msgRejectContract);
						caseValue = 5;
						decision = "PENDING";
						contractDecision = "PENDING";
					}

					caseValue = 5;

				}

				break;

			case 5:

				System.out.println("Project is on");
				ACLMessage startMsg = blockingReceive();
				if (startMsg.getPerformative() == ACLMessage.REQUEST_WHENEVER) {

					providerProjectGUI = new ProviderProjectGUI(providerAgent);
					providerChatGUI = new ProviderChatGUI();
					caseValue = 6;
				}

				break;

			case 6:

				if (trackValues.contentEquals("DONE")) {

					ACLMessage msgTracker = new ACLMessage(ACLMessage.INFORM);
					String content = providerProjectGUI.getDeadlineArea() + "*" + providerProjectGUI.getProgressArea()
							+ "*" + providerProjectGUI.getTimeArea();
					msgTracker.addReceiver(new AID(customerName, AID.ISLOCALNAME));
					msgTracker.setContent(content);
					send(msgTracker);
					caseValue = 8;
				}

				break;


			case 8:

				if (endProject.contentEquals("END")) {
					ACLMessage msgTracker = new ACLMessage(ACLMessage.CANCEL);
					String contentPr = "END PROJECT";
					msgTracker.addReceiver(new AID(customerName, AID.ISLOCALNAME));
					msgTracker.setContent(contentPr);
					send(msgTracker);
					caseValue =9;

				}

				break;

			case 9:

				ACLMessage msgPayment = new ACLMessage(ACLMessage.PROPAGATE);
				msgPayment.addReceiver(new AID("BIDDING", AID.ISLOCALNAME));
				msgPayment.setContent("ASK FOR PAYMENT");
				send(msgPayment);

				caseValue = 10;
				break;

			case 10:

				ACLMessage getPayment = blockingReceive();
				String paymentText = getPayment.getContent();
				providerFeedbackGUI.setPaymentArea(paymentText);

				caseValue = 1;
				
				break;

			}
		}
	}
}
