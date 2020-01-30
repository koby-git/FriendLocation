package com.koby.friendlocation.classes;

import java.io.Serializable;
import java.util.ArrayList;

public class Group implements Serializable {

    private String groupName;
    private String groupInviteCode;
    private String groupUid;
    private ArrayList<String> users;

    public Group() {
    }

    public Group(String groupName,String groupUid, String groupInviteCode ) {
        this.groupName = groupName;
        this.groupInviteCode = groupInviteCode;
        this.groupUid = groupUid;
        users = new ArrayList<>();
    }

    public Group(String groupName, String groupInviteCode) {
        this.groupName = groupName;
        this.groupInviteCode = groupInviteCode;
        users = new ArrayList<>();
    }

    public void addUser(String user) {
        this.users.add(user);
    }

    public ArrayList<String> getUsers() {
        return users;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public void setGroupInviteCode(String groupInviteCode) {
        this.groupInviteCode = groupInviteCode;
    }

    public void setGroupUid(String groupUid) {
        this.groupUid = groupUid;
    }

    public String getGroupName() {
        return groupName;
    }

    public String getGroupInviteCode() {
        return groupInviteCode;
    }

    public String getGroupUid() {
        return groupUid;
    }
}
