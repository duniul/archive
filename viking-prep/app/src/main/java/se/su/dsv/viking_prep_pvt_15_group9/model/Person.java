package se.su.dsv.viking_prep_pvt_15_group9.model;

/**
 * Created by Daniel on 2015-05-28.
 */
import java.util.Calendar;

public class Person {
    private int userID;
    private String uniqueID;
    private String name;
    private String surname;
    private String email;
    private String area;
    private String city;
    private String dateOfBirth;
    private String sex;
    private String createdAt;
    private String updatedAt;
    private String pictureUrl;

    public Person(int userID, String uniqueID, String name, String surname, String email, String area, String city, String dateOfBirth, String sex, String createdAt, String updatedAt, String pictureUrl) {
        this.userID = userID;
        this.uniqueID = uniqueID;
        this.name = name;
        this.surname = surname;
        this.email = email;
        this.area = area;
        this.city = city;
        this.dateOfBirth = dateOfBirth;
        this.sex = sex;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.pictureUrl = pictureUrl;
    }

    public int getUserID() {
        return userID;
    }

    public String getUniqueID() {
        return uniqueID;
    }

    public String getFullName() {
        return name + " " + surname;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getLocation() {
        return area + ", " + city;
    }

    public String getArea() {
        return area;
    }

    public void setArea(String area) {
        this.area = area;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public int getAge() {
        Calendar calToday = Calendar.getInstance();
        Calendar calDateOfBirth = Calendar.getInstance();
        int age;
        int year;
        int month;
        int day;

        year = Integer.parseInt(dateOfBirth.substring(0, 4));
        month = Integer.parseInt(dateOfBirth.substring(5,7));
        day = Integer.parseInt(dateOfBirth.substring(8));
        calDateOfBirth.set(year, month, day);

        age = calToday.get(Calendar.YEAR) - calDateOfBirth.get(Calendar.YEAR);
        if (calToday.get(Calendar.DAY_OF_YEAR) >= calDateOfBirth.get(Calendar.DAY_OF_YEAR)) {
            age--;
        }

        return age;
    }

    public String getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(String dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public String getSex() {
        return sex;
    }

    public void setSex(String sex) {
        this.sex = sex;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getPictureUrl() {
        return pictureUrl;
    }

    public void setPictureUrl(String pictureUrl) {
        this.pictureUrl = pictureUrl;
    }

}