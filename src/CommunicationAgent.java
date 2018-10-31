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
    private String messagePerformative= "";
    private String fullConversationText = "";
    public ArrayList<String> agentList;

    protected void setup() {
        System.out.println("Messenger agent "+ getAID().getAddressesArray()[0] + "--" + getAID().getName()+" is ready.");

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

            AID aid = new AID();
            //xx set <receiver agentName>@<private IP of receiver agent>:1099/JADE
            aid.setName("TWO@172.31.21.127:1099/JADE");
            //xx put private DNS of receiver
            aid.addAddresses("http://ip-172-31-21-127.eu-west-1.compute.internal:7778/acc");
            msg.addReceiver(aid);

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
        //clearing list in GUI
        agentList.clear();

        DFAgentDescription template = new DFAgentDescription();

        AID otherPlatform = new AID();
        //xx df@<private IP of receiver>:1099/JADE
        otherPlatform.setName("df@172.31.21.127:1099/JADE");
        //xx http://<private IP of receiver:7778/acc>
        otherPlatform.addAddresses("http://ip-172-31-21-127.eu-west-1.compute.internal:7778/acc");

        ServiceDescription sd = new ServiceDescription();
        sd.setType("messenger-agent");

        template.addServices(sd);
        //template.setName(otherPlatform);

        try {
            DFAgentDescription[] result = DFService.search(this, otherPlatform, template);
            for (int i = 0; i < result.length; i++) {
                AID agentID = result[i].getName();
                agentList.add(agentID.getLocalName());
            }
        } catch (FIPAException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onGuiEvent(GuiEvent arg0) {
        addBehaviour(new SendMsg());
    }
}
