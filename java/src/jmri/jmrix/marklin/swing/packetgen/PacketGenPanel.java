package jmri.jmrix.marklin.swing.packetgen;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.util.Objects;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import jmri.jmrix.marklin.MarklinListener;
import jmri.jmrix.marklin.MarklinMessage;
import jmri.jmrix.marklin.MarklinReply;
import jmri.jmrix.marklin.MarklinSystemConnectionMemo;
import jmri.util.swing.JmriJOptionPane;

/**
 * Frame for user input of Marklin messages
 *
 * @author Bob Jacobsen Copyright (C) 2001, 2008
 * @author Dan Boudreau Copyright (C) 2007
 */
public class PacketGenPanel extends jmri.jmrix.marklin.swing.MarklinPanel implements MarklinListener {

    // member declarations
    private final JLabel entryLabel = new JLabel();
    private final JLabel replyLabel = new JLabel();
    private final JButton sendButton = new JButton();
    private final JTextField packetTextField = new JTextField(20);
    private final JTextField packetReplyField = new JTextField(20);

    public PacketGenPanel() {
        super();
    }

    /** 
     * {@inheritDoc}
     */
    @Override
    public void initComponents() {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        // the following code sets the frame's initial state

        JPanel entrybox = new JPanel();
        entryLabel.setText(Bundle.getMessage("CommandLabel"));
        entryLabel.setVisible(true);
        entryLabel.setLabelFor(packetTextField);

        replyLabel.setText(Bundle.getMessage("ReplyLabel"));
        replyLabel.setVisible(true);
        replyLabel.setLabelFor(packetReplyField);

        sendButton.setText(Bundle.getMessage("ButtonSend"));
        sendButton.setVisible(true);
        sendButton.setToolTipText(Bundle.getMessage("SendToolTip"));

        packetTextField.setText("");
        packetTextField.setToolTipText(Bundle.getMessage("EnterHexToolTip"));
        packetTextField.setMaximumSize(new Dimension(packetTextField
                .getMaximumSize().width, packetTextField.getPreferredSize().height));

        entrybox.setLayout(new GridLayout(2, 2));
        entrybox.add(entryLabel);
        entrybox.add(packetTextField);
        entrybox.add(replyLabel);

        JPanel buttonbox = new JPanel();
        FlowLayout buttonLayout = new FlowLayout(FlowLayout.TRAILING);
        buttonbox.setLayout(buttonLayout);
        buttonbox.add(sendButton);
        entrybox.add(buttonbox);
        //packetReplyField.setEditable(false); // keep field editable to allow user to select and copy the reply
        add(entrybox);
        add(packetReplyField);
        add(Box.createVerticalGlue());

        sendButton.addActionListener(this::sendButtonActionPerformed);
    }

    /** 
     * {@inheritDoc}
     */
    @Override
    public String getHelpTarget() {
        return "package.jmri.jmrix.marklin.swing.packetgen.PacketGenFrame";
    }

    /** 
     * {@inheritDoc}
     */
    @Override
    public String getTitle() {
        return Bundle.getMessage("SendCommandTitle");
    }

    /** 
     * {@inheritDoc}
     */
    @Override
    public void initComponents(MarklinSystemConnectionMemo memo) {
        super.initComponents(memo);
        memo.getTrafficController().addMarklinListener(this);
    }

    public void sendButtonActionPerformed(java.awt.event.ActionEvent e) {
        String text = packetTextField.getText();
        // TODO check input + feedback on error. Too easy to cause NPE
        if (text != null && !Objects.equals(text, "")) {
            if (text.length() == 0) {
                return; // no work
            }
            log.info("Entry[{}]", text);
            if (text.startsWith("0x")) { //We want to send a hex message

                text = text.replaceAll("\\s", "");
                text = text.substring(2);
                String[] arr = text.split(",");
                byte[] msgArray = new byte[arr.length];
                int pos = 0;
                for (String s : arr) {
                    msgArray[pos++] = (byte) (Integer.parseInt(s, 16) & 0xFF);
                }

                MarklinMessage m = new MarklinMessage(msgArray);
                if ( memo != null ) {
                    memo.getTrafficController().sendMarklinMessage(m, this);
                }
            } else {
                log.error("Only hex commands are supported");
                JmriJOptionPane.showMessageDialog(this, Bundle.getMessage("HexOnlyDialog"),
                        Bundle.getMessage("WarningTitle"), JmriJOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /** 
     * {@inheritDoc}
     * Ignore messages
     */
    @Override
    public void message(MarklinMessage m) {
    }

    /** 
     * {@inheritDoc}
     */
    @Override
    public void reply(MarklinReply r) {
        packetReplyField.setText(r.toString());
    }
    
    @Override
    public void dispose() {
        if ( memo != null ) {
            memo.getTrafficController().removeMarklinListener(this);
        }
        super.dispose();
    }

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(PacketGenPanel.class);
}
