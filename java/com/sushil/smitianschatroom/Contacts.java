package com.sushil.smitianschatroom;

public class Contacts {
    public String name,status,image; //This value must be same with database key value(name,status,image)
    //It need empty constructor(constructor with no parameters) or default constructor
     public Contacts(){

     }

    public Contacts(String name, String status, String image) {
        this.name = name;
        this.status = status;
        this.image = image;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }
}
