package se.su.dsv.viking_prep_pvt_15_group9.model;

/**
 * Created by miaha_000 on 5/25/2015.
 */
public class UserTipObject {

    private String userName;
    private String description;
    private String firstImage;
    private String secondImage;
    private String obstacleName;
    private int userID;
    private int tipID;



    public UserTipObject(String userName, int userID, int tipID, String obstacleName, String description, String firstImage, String secondImage){
        super();
        this.userName = userName;
        this.userID = userID;
        this.tipID = tipID;
        this.obstacleName = obstacleName;
        this.description = description;
        this.firstImage = firstImage;
        this.secondImage = secondImage;

    }

    public void setTipID(int tipId) {tipID = tipId; }

    public int getUserTip() {return tipID; }

    public void setUserID(int userId){ userID = userId; }

    public int getUserID() { return userID; }

    public void setUserName(String name){
        userName = name;
    }

    public String getUserName(){
        return userName;
    }

    public void setDescription(String desc){
        description = desc;
    }

    public String getDescription(){
        return description;
    }

    public void setFirstImage(String firstImageName){
        this.firstImage = firstImageName;
    }

    public String getFirstImage(){
        return firstImage;
    }

    public void setSecondImage(String secondImageName){
        this.secondImage = secondImageName;
    }

    public String getSecondImage(){
        return secondImage;
    }

    public void setObstacleName(String obstacleIdName){
        obstacleName = obstacleIdName;
    }

    public String getObstacleName(){
        return obstacleName;
    }


}
