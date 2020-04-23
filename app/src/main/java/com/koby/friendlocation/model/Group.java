package com.koby.friendlocation.model;

import java.io.Serializable;
import java.util.ArrayList;

public class Group implements Serializable {

    private String name;
    private String inviteCode;
    private String uid;
    private String image;

    public Group() {
    }

    public Group(String groupName,String groupUid, String groupInviteCode ) {
        this.name = groupName;
        this.inviteCode = groupInviteCode;
        this.uid = groupUid;
    }

    public Group(String groupName, String groupInviteCode) {
        this.name = groupName;
        this.inviteCode = groupInviteCode;
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getInviteCode() {
        return inviteCode;
    }

    public void setInviteCode(String inviteCode) {
        this.inviteCode = inviteCode;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }
}
