package matchpackage.access;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.OneShotBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import matchpackage.application.AppGUI;
import matchpackage.application.EnhancedAgent;
import matchpackage.application.GuestGUI;
import matchpackage.database.ProviderList;

public class AccessAgent extends Agent {
	
	private ProviderList providerList;
	
	protected void setup() {
		providerList = new ProviderList();
		System.out.println("I am an Access Agent");
		addBehaviour(new CallForProvidersList());
	}
	
	public class CallForProvidersList extends OneShotBehaviour {
		
		public void action() {
			
			ACLMessage message = myAgent.blockingReceive();
			System.out.println(message);
			ACLMessage reply = message.createReply();
			reply.setContent(providerList.getStringProvidersGuest());
			send(reply);
		}
	}
}