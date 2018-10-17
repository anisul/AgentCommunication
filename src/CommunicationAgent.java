import jade.core.AID;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.domain.AMSService;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.AMSAgentDescription;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.SearchConstraints;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.gui.GuiAgent;
import jade.gui.GuiEvent;
import jade.lang.acl.ACLMessage;

import java.util.ArrayList;
import java.util.Date;

public class CommunicationAgent extends GuiAgent {
    private AgentGUI gui;
    private String receiverName = "";
    private String msgContent = "";
    private String messagePerformative="";
    private String fullConversationText = ""; // all the conversations will be appended here
    public ArrayList<String> agentList;
    public static int agentCounterInitial = 0;
    public static int agentCounterFinal = 0;

    protected void setup() {
        System.out.println("Messenger agent "+getAID().getName()+" is ready.");

        agentList	=	new ArrayList();

        Behaviour loop;
        loop = new TickerBehaviour( this, 5000 ){
            protected void onTick() {
                refreshActiveAgents();
            }
        };

        addBehaviour( loop );

        gui = new AgentGUI(this);
        gui.drawGUI();

        Behaviour loop2;
        loop2 = new TickerBehaviour( this, 5000 ){
            protected void onTick() {
                gui.receivers.removeAllItems();
                gui.updateRcvrDropDown();
            }
        };

        addBehaviour( loop2 );

        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());
        ServiceDescription sd = new ServiceDescription();
        sd.setType("messenger-agent");
        sd.setName(getLocalName()+"-Messenger agent");
        dfd.addServices(sd);
        try {
            DFService.register(this, dfd);
        }
        catch (FIPAException fe) {
            fe.printStackTrace();
        }
        addBehaviour(new ReceiveMessage());
    }


    //Agent clean-up
    protected void takeDown() {
        if (gui != null) {
            gui.dispose();
        }
        System.out.println("Agent "+getAID().getName()+" is terminating.");
        try {
            DFService.deregister(this);
            System.out.println("Agent "+getAID().getName()+" has been signed off.");
        }
        catch (FIPAException fe) {
            fe.printStackTrace();
        }
    }

    public class SendMsg extends OneShotBehaviour {
        public void action() {
            ACLMessage msg;
            if(messagePerformative.equalsIgnoreCase("PROPOSE")){
                msg = new ACLMessage(ACLMessage.PROPOSE);
            }else if(messagePerformative.equalsIgnoreCase("REQUEST")){
                msg = new ACLMessage(ACLMessage.REQUEST);
            }else if(messagePerformative.equalsIgnoreCase("INFORM")){
                msg = new ACLMessage(ACLMessage.INFORM);
            }else if(messagePerformative.equalsIgnoreCase("CONFIRM")){
                msg = new ACLMessage(ACLMessage.CONFIRM);
            }else if(messagePerformative.equalsIgnoreCase("QUERY")){
                msg = new ACLMessage(ACLMessage.QUERY_REF);
            }else{
                msg = new ACLMessage(ACLMessage.AGREE);
            }

            msg.addReceiver(new AID(receiverName, AID.ISLOCALNAME));
            msg.setLanguage("English");
            msg.setContent(msgContent);
            send(msg);
            Date date = new Date();
            String currDate = date.toString();
            fullConversationText = "\n" + messagePerformative.toUpperCase() + " to " + receiverName + " : " + msg.getContent();
            gui.messageViewerConversation.append(fullConversationText + " \n" + currDate);
            gui.messageSentViewer.append(fullConversationText+ " \n" + currDate);
        }
    }

    public class ReceiveMessage extends CyclicBehaviour {
        private String messageContent;
        private String SenderName;
        public void action() {
            ACLMessage msg = receive();
            if(msg != null) {
                messagePerformative = msg.getPerformative(msg.getPerformative());
                messageContent = msg.getContent();
                SenderName = msg.getSender().getLocalName();
                Date date = new Date();
                String currDate = date.toString();
                fullConversationText = "\n" + messagePerformative + " from " +SenderName+" : "+messageContent;
                gui.messageViewerConversation.append(fullConversationText + " \n" + currDate);
                gui.messageRecvdViewer.append(fullConversationText + " \n" + currDate);
            }
        }
    }

    public void messageData(final String messageType, final String to, final String msgInfo) {
        addBehaviour(new OneShotBehaviour() {
            public void action() {
                messagePerformative = messageType;
                receiverName = to;
                msgContent = msgInfo;
            }
        } );
    }



    public void refreshActiveAgents(){
        AMSAgentDescription[] agents = null;
        agentList.clear();
        try {
            SearchConstraints searchConstraints = new SearchConstraints();
            searchConstraints.setMaxResults ( new Long(-1) );
            agents = AMSService.search( this, new AMSAgentDescription (), searchConstraints );
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        for (int i=0; i<agents.length;i++){
            AID agentID = agents[i].getName();
            if(!agentID.getLocalName().equals("ams") && !agentID.getLocalName().equals("rma") && !agentID.getLocalName().equals("df"))
                agentList.add(agentID.getLocalName());
        }
    }

    @Override
    protected void onGuiEvent(GuiEvent arg0) {
        // TODO Auto-generated method stub
        addBehaviour(new SendMsg());
    }
}
