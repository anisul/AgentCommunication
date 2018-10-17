import jade.gui.GuiEvent;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;

public class AgentGUI extends JFrame {
    private static int TEXT_FIELD_HEIGHT = 30;
    private static int LABEL_HEIGHT = 30;
    private static int LABEL_WIDTH = 400;
    private CommunicationAgent CommunicationAgent;
    String messageType = "";

    JTextField messageContent;
    JTextArea messageViewerConversation, messageSentViewer, messageRecvdViewer;
    JComboBox messageTypes, receivers;

    JFrame mainFrame;
    JLabel headerLabel, conversationLabel, statusLabel, msglabel;
    JPanel controlPanel;
    JButton sendMessageBtn;
    Font font = new Font("Arial", Font.PLAIN, 14);
    JLabel messageContentLabel, sentMessagesLabel, receivedMessagesLabel, typeLabel, receiverLabel;

    ArrayList<String> msgTypesList;
    ArrayList<String> rcvrList;

    public AgentGUI(CommunicationAgent a) {
        super(a.getLocalName());

        CommunicationAgent = a;

        msgTypesList = new ArrayList();
        rcvrList = new ArrayList();

        msgTypesList.add("Request");
        msgTypesList.add("Query");
        msgTypesList.add("Propose");
        msgTypesList.add("Inform");
        msgTypesList.add("Confirm");
        msgTypesList.add("Agree");

        messageContent = new JTextField();
        messageContent.setPreferredSize(new Dimension(400, 30));

        messageViewerConversation = new JTextArea(15, 45);
        messageViewerConversation.setEditable(false);
        messageViewerConversation.setFont(font);
        JScrollPane scrollPaneConversation = new JScrollPane(messageViewerConversation);

        messageTypes = new JComboBox(msgTypesList.toArray());
        messageTypes.setPreferredSize(new Dimension(400,TEXT_FIELD_HEIGHT));

        typeLabel = new JLabel("Message Type: ");
        typeLabel.setPreferredSize(new Dimension(LABEL_WIDTH , LABEL_HEIGHT));

        receiverLabel = new JLabel("Receivers: ");
        receiverLabel.setPreferredSize(new Dimension(LABEL_WIDTH, LABEL_HEIGHT));

        messageContentLabel = new JLabel("Content: ");
        messageContentLabel.setPreferredSize(new Dimension(LABEL_WIDTH, LABEL_HEIGHT));

        conversationLabel = new JLabel("Conversation: ");
        conversationLabel.setPreferredSize(new Dimension(LABEL_WIDTH, LABEL_HEIGHT));

        sendMessageBtn = new JButton("Send");
        sendMessageBtn.setPreferredSize(new Dimension(100, 30));

        headerLabel = new JLabel("",JLabel.CENTER );
        statusLabel = new JLabel("",JLabel.CENTER);

        updateRcvrDropDown();
        receivers = new JComboBox(rcvrList.toArray());
        receivers.setPreferredSize(new Dimension(400, 40));

        sentMessagesLabel = new JLabel("Sent Messages: ");
        sentMessagesLabel.setPreferredSize(new Dimension(LABEL_WIDTH, LABEL_HEIGHT));

        receivedMessagesLabel = new JLabel("Received Messages: ");
        receivedMessagesLabel.setPreferredSize(new Dimension(LABEL_WIDTH, LABEL_HEIGHT));

        messageSentViewer = new JTextArea(5, 45);
        messageSentViewer.setEditable(false);
        messageSentViewer.setFont(font);
        JScrollPane scrollPaneSent = new JScrollPane(messageSentViewer);

        messageRecvdViewer = new JTextArea(5, 45);
        messageRecvdViewer.setEditable(false);
        messageRecvdViewer.setFont(font);
        JScrollPane scrollPaneRecvd = new JScrollPane(messageRecvdViewer);


        controlPanel = new JPanel();
        controlPanel.add(typeLabel);
        controlPanel.add(messageTypes);
        controlPanel.add(messageContent);
        controlPanel.add(receiverLabel);
        controlPanel.add(receivers);
        controlPanel.add(messageContentLabel);
        controlPanel.add(messageContent);
        //controlPanel.add(conversationLabel);
        //controlPanel.add(scrollPaneConversation);
        controlPanel.add(sentMessagesLabel);
        controlPanel.add(scrollPaneSent);
        controlPanel.add(receivedMessagesLabel);
        controlPanel.add(scrollPaneRecvd);
        controlPanel.add(sendMessageBtn);

        Container contentPane = getContentPane();
        contentPane.setPreferredSize(new Dimension(500, 600));
        getContentPane().add(controlPanel, BorderLayout.CENTER);

        /*JPanel sentMsgs = new JPanel();
        sentMsgs.add(sentMessagesLabel);
        sentMsgs.add(scrollPaneSent);

        JPanel receivedMsgs = new JPanel();
        receivedMsgs.add(receivedMessagesLabel);
        receivedMsgs.add(scrollPaneRecvd);*/

        JTabbedPane jtp = new JTabbedPane();
        getContentPane().add(jtp);
        jtp.addTab("Conversation", controlPanel);
        //jtp.addTab("Sent", sentMsgs);
        //jtp.addTab("Received", receivedMsgs);

        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                CommunicationAgent.doDelete();
            }
        } );

        sendMessageBtn.addActionListener( new ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent ae) {
                try {
                    String content = messageContent.getText().trim();
                    messageType = messageTypes.getSelectedItem().toString();
                    CommunicationAgent.messageData(messageType, receivers.getSelectedItem().toString(), content);
                    messageContent.setText("");
                    GuiEvent guiEvent = new GuiEvent(this, 1);
                    CommunicationAgent.postGuiEvent(guiEvent);
                }
                catch (Exception e) {
                    JOptionPane.showMessageDialog(AgentGUI.this, "Invalid values. "+e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        } );
    }

    public void drawGUI() {
        pack();
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int centerX = (int)screenSize.getWidth() / 2;
        int centerY = (int)screenSize.getHeight() / 2;
        setLocation(centerX - getWidth() / 2, centerY - getHeight() / 2);
        super.setVisible(true);
    }

    public void updateRcvrDropDown(){
        System.out.println("Updating receiver list " + CommunicationAgent.agentList.toString());
        for(String agentName : CommunicationAgent.agentList){
            if(!CommunicationAgent.getLocalName().equals(agentName)){
                System.out.println(agentName);
                receivers.addItem(agentName);
            }
        }
    }
}
