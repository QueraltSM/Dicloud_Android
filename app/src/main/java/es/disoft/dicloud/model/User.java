package es.disoft.dicloud.model;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

@Entity(tableName = "users")
public class User {

    public static User currentUser;

    @PrimaryKey
    @NonNull
    private String id;
    @NonNull
    private int user_id;
    @NonNull
    private String userAlias;
    @NonNull
    private String fullName;
    @NonNull
    private String dbAlias;
    @NonNull
    private int loggedIn;
    @NonNull
    private String token;
    @NonNull
    private String companyID;
    @NonNull
    private Boolean listin;
    @NonNull
    private Boolean desarrollo;


    public User(@NonNull int user_id,
                @NonNull String userAlias,
                @NonNull String fullName,
                @NonNull String dbAlias,
                @NonNull String token,
                @NonNull String companyID,
                @NonNull Boolean listin,
                @NonNull Boolean desarrollo) {
        id = userAlias + dbAlias;
        this.user_id   = user_id;
        this.userAlias = userAlias;
        this.fullName  = fullName;
        this.dbAlias   = dbAlias;
        this.loggedIn  = 1;
        this.token     = token;
        this.companyID = companyID;
        this.listin = listin;
        this.desarrollo = desarrollo;
    }

    @NonNull
    public String getId() {
        return id;
    }

    public void setId(@NonNull String id) {
        this.id = id;
    }

    @NonNull
    public int getUser_id() {
        return user_id;
    }

    public void setUser_id(@NonNull int user_id) {
        this.user_id = user_id;
    }

    @NonNull
    public String getUserAlias() {
        return userAlias;
    }

    public void setUserAlias(@NonNull String userAlias) {
        this.userAlias = userAlias;
    }

    @NonNull
    public String getFullName() {
        return fullName;
    }

    public void setFullName(@NonNull String fullName) {
        this.fullName = fullName;
    }

    @NonNull
    public String getDbAlias() {
        return dbAlias;
    }

    public void setDbAlias(@NonNull String dbAlias) {
        this.dbAlias = dbAlias;
    }

    @NonNull
    public int getLoggedIn() {
        return loggedIn;
    }

    public void setLoggedIn(@NonNull int loggedIn) {
        this.loggedIn = loggedIn;
    }

    @NonNull
    public String getToken() {
        return token;
    }

    public void setToken(@NonNull String token) {
        this.token = token;
    }

    @NonNull
    public String getCompanyID() {
        return companyID;
    }

    public void setCompanyID(@NonNull String companyID) {
        this.companyID = companyID;
    }

    @NonNull
    public Boolean getListin() {
        return listin;
    }

    public void setListin(@NonNull Boolean listin) {
        this.listin = listin;
    }

    @NonNull
    public Boolean getDesarrollo() {
        return desarrollo;
    }

    public void setDesarrollo(@NonNull Boolean desarrollo) {
        this.desarrollo = desarrollo;
    }

    public static class DbAlias {
        public String dbAlias;
    }

    public static class UserAlias {
        public String userAlias;
    }
}



