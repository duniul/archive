package se.su.dsv.viking_prep_pvt_15_group9.model;

/**
 * Created by Daniel on 2015-06-01.
 */
public class Conversation {

    private int userID1;
    private int userID2;
    private Person person;
    private Message lastMessage;

    public Conversation(int userID1, int userID2, Person person, Message lastMessage) {
        this.userID1 = userID1;
        this.userID2 = userID2;
        this.person = person;
        this.lastMessage = lastMessage;
    }

    public int getUserID1() {
        return userID1;
    }

    public void setUserID1(int userID1) {
        this.userID1 = userID1;
    }

    public int getUserID2() {
        return userID2;
    }

    public void setUserID2(int userID2) {
        this.userID2 = userID2;
    }

    public Person getPerson() {
        return person;
    }

    public void setPerson(Person person) {
        this.person = person;
    }

    public Message getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(Message lastMessage) {
        this.lastMessage = lastMessage;
    }

    public String getLastMessageSenderTitle() {
        return lastMessage.getSenderTitle();
    }

    public String getLastMessageText() {
        return lastMessage.getMessageText();
    }

    public String getLastMessageDate() {
        return lastMessage.getDateSent();
    }

    public String getLastMessageTime() {
        return lastMessage.getTimeSent();
    }
}
