package com.sismics.docs.core.model.jpa;

import java.util.Date;
import java.text.SimpleDateFormat;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import com.google.common.base.MoreObjects;

/**
 * User register request entity.
 */
@Entity
@Table(name = "T_USER_REGISTER_REQUEST")
public class UserRegisterRequest {
    /**
     * Register request ID.
     */
    @Id
    @Column(name = "URR_ID_C", length = 36)
    private String id;

    /**
     * Username.
     */
    @Column(name = "URR_USERNAME_C", nullable = false, unique = true, length = 50)
    private String username;

    /**
     * Password.
     */
    @Column(name = "URR_PASSWORD_C", nullable = false, length = 100)
    private String password;

    /**
     * Email.
     */
    @Column(name = "URR_EMAIL_C", nullable = false, length = 100)
    private String email;

    /**
     * Status (PENDING, APPROVED, REJECTED).
     */
    @Column(name = "URR_STATUS_C", nullable = false, length = 20)
    private String status;

    /**
     * Create date.
     */
    @Column(name = "URR_CREATEDATE_D", nullable = false)
    private Date createDate;

    /**
     * Decision date.
     */
    @Column(name = "URR_DECISIONDATE_D")
    private Date decisionDate;

    /**
     * Decision admin ID.
     */
    @Column(name = "URR_DECISIONADMINID_C", length = 50)
    private String decisionAdminId;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Date getCreateDate() {
        return createDate;
    }

    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }

    public Date getDecisionDate() {
        return decisionDate;
    }

    public void setDecisionDate(Date decisionDate) {
        this.decisionDate = decisionDate;
    }

    public String getDecisionAdminId() {
        return decisionAdminId;
    }

    public void setDecisionAdminId(String decisionAdminId) {
        this.decisionAdminId = decisionAdminId;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("id", id)
                .add("username", username)
                .add("email", email)
                .add("status", status)
                .toString();
    }
}
