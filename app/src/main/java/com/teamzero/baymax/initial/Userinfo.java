package com.teamzero.baymax.initial;

public class Userinfo {

    private static Userinfo mInstance=null;
    private static String email="",name="";
    private static boolean active=false;
    private static boolean user,admin,pharmacy,institute,driver,doctor;

    public static Userinfo getInstance()
    {
        if(mInstance==null) mInstance=new Userinfo();
        return mInstance;
    }
    private Userinfo()
    {
        active=false;
    }

    public void refresh()
    {
        email="";
        name="";
        user=admin=pharmacy=institute=driver=doctor=active=false;
    }

    public static boolean isActive() {
        return active;
    }

    public void setActive(String email,String name, boolean active, boolean user, boolean admin, boolean doctor, boolean pharmacy, boolean institute, boolean driver) {
        this.email=email;
        this.name=name;
        this.active=active;
        this.user=user;
        this.admin=admin;
        this.doctor=doctor;
        this.pharmacy=pharmacy;
        this.institute=institute;
        this.driver=driver;
    }

    public static boolean isAdmin() {
        return admin;
    }

    public static boolean isDoctor() {
        return doctor;
    }

    public static boolean isDriver() {
        return driver;
    }

    public static boolean hasInstitute() {
        return institute;
    }

    public static boolean hasPharmacy() {
        return pharmacy;
    }

    public static String getEmail() {
        return email;
    }

    public static String getName() { return name; }
}
