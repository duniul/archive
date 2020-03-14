package se.su.dsv.viking_prep_pvt_15_group9.model;

/**
 * Created by Daniel on 2015-05-31.
 */
public class Message {

    private String senderTitle;
    private int senderUserID;
    private int recieverUserID;
    private String messageText;
    private String dateSent;
    private String timeSent;

    // Konstruktor 1, utan datum och tid
    public Message(String senderTitle, int senderUserID, int recieverUserID, String messageText) {
        this.senderTitle = senderTitle;
        this.senderUserID = senderUserID;
        this.recieverUserID = recieverUserID;
        this.messageText = messageText;
        dateSent = "";
        timeSent ="";
    }

    // Konstruktor 2, med datum och tid
    public Message(String senderTitle, int senderUserID, int recieverUserID, String messageText, String dateSent, String timeSent) {
        this.senderTitle = senderTitle;
        this.senderUserID = senderUserID;
        this.recieverUserID = recieverUserID;
        this.messageText = messageText;
        this.dateSent = dateSent;
        this.timeSent = timeSent;
    }
    public String getSenderTitle() {
        return senderTitle;
    }

    public void setSenderTitle(String senderTitle) {
        this.senderTitle = senderTitle;
    }

    public int getSenderUserID() {
        return senderUserID;
    }

    public void setSenderUserID(int senderUserID) {
        this.senderUserID = senderUserID;
    }

    public int getRecieverUserID() {
        return recieverUserID;
    }

    public void setRecieverUserID(int recieverUserID) {
        this.recieverUserID = recieverUserID;
    }

    public String getDateSent() {
        return dateSent;
    }

    public void setDateSent(String dateSent) {
        this.dateSent = dateSent;
    }

    public String getTimeSent() {
        return timeSent;
    }

    public void setTimeSent(String timeSent) {
        this.timeSent = timeSent;
    }

    public String getMessageText() {
        return messageText;
    }

    public void setMessageText(String messageText) {
        this.messageText = messageText;
    }
}
