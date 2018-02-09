package com.belikeprogrammer.freeloads;

/**
 * Created by gowty-vicky on 28/9/17.
 */

public class Post {

    private String uid, title, desc, address, contactNo, image0, image1, image2, image3, category, flag;
    private boolean verified;

    public Post(String uid, String title, String desc, String address, String contactNo, String image0, String image1, String image2, String image3, String category, String flag) {
        this.uid = uid;
        this.title = title;
        this.desc = desc;
        this.address = address;
        this.contactNo = contactNo;
        this.image0 = image0;
        this.image1 = image1;
        this.image2 = image2;
        this.image3 = image3;
        this.category = category;
        this.flag = flag;
    }

    public Post() {
    }

    public boolean isVerified() {
        return verified;
    }

    public String getUid() {
        return uid;
    }

    public String getTitle() {
        return title;
    }

    public String getDesc() {
        return desc;
    }

    public String getAddress() {
        return address;
    }

    public String getContactNo() {
        return contactNo;
    }

    public String getImage0() {
        return image0;
    }

    public String getImage1() {
        return image1;
    }

    public String getImage2() {
        return image2;
    }

    public String getImage3() {
        return image3;
    }

    public String getCategory() {
        return category;
    }

    public String getFlag() {
        return flag;
    }
}
