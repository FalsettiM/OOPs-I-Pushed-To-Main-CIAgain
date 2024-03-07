package com.oopsipushedtomain;

public class Profile {
    private String userId;
    private String name;
    private String nickname;
    private String birthday; // type to be declared timestamp??
    private String homepage;
    private String address;
    private String phone;
    private String email;

    public Profile() {}

    // Constructor
    public Profile(String userId, String name, String nickname, String birthday, String homepage, String address, String phoneNumber, String email) {
        this.userId = userId;
        this.name = name;
        this.nickname = nickname;
        this.birthday = birthday;
        this.homepage = homepage;
        this.address = address;
        this.phone = phoneNumber;
        this.email = email;
    }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getNickname() { return nickname; }
    public void setNickname(String nickname) { this.nickname = nickname; }
    public String getBirthday() { return birthday; }
    public void setBirthday(String birthday) { this.birthday = birthday; }
    public String getHomepage() { return homepage; }
    public void setHomepage(String homepage) { this.homepage = homepage; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
}
