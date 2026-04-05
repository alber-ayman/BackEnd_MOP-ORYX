package com.example.demo.models;

import java.math.BigInteger;
import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Entity
@Data
@Table(name = "USERS")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Size(max = 50)
    @Column(name = "EMAIL")
    private String email;
    @Size(max = 120)
    @Column(name = "PASSWORD")
    private String password;
    @Size(max = 20)
    @Column(name = "USERNAME")
    private String username;

    private String role;
    @Column(name = "USERROLE")
    private BigInteger userrole;
    @Column(name = "ISACTIVE")
    private BigInteger isactive;
    @Column(name = "MERCHANT_ID")
    private BigInteger merchantId;
    @Column(name = "IS_FIRST_TIME")
    private BigInteger isFirstTime;

    private boolean viewProject;
    private boolean addProject;
    private boolean editProject;
    private boolean deleteProject;

    private boolean viewPand;
    private boolean addPand;
    private boolean editPand;

    private boolean deletePand;

    private boolean viewJobOrder;
    private boolean addJobOrder;
    private boolean editJobOrder;

    private boolean deleteJobOrder;

    private boolean viewExitJobOrder;
    private boolean addExitJobOrder;
    private boolean editExitJobOrder;

    private boolean deleteExitJobOrder;
    private boolean showReports;
    private boolean recordDelivered;

    private boolean viewOnly;
    private boolean allAuth;

    private boolean editUser;

    private boolean manufacturingManager;

    private boolean storeManager;

    private boolean purchasingManager;

    private boolean user;
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id"))
    private Set<Role> roles = new HashSet<>();

    public User() {
    }

    public User(String username, String email, String password, String role,
                boolean viewProject, boolean editProject, boolean addProject, boolean deleteProject
            , boolean viewPand, boolean addPand, boolean editPand, boolean deletePand
            , boolean viewJobOrder, boolean addJobOrder, boolean editJobOrder, boolean deleteJobOrder
            , boolean viewExitJobOrder, boolean addExitJobOrder, boolean editExitJobOrder, boolean deleteExitJobOrder,
                boolean showReports, boolean recordDelivered, boolean allAuth,boolean viewOnly ,boolean editUser,boolean manufacturingManager,boolean storeManager,boolean purchasingManager, boolean user) {
        this.username = username;
        this.email = email;
        this.password = password;
        this.role = role;

        this.viewProject = viewProject;
        this.addProject = addProject;
        this.editProject = editProject;
        this.deleteProject = deleteProject;

        this.viewPand = viewPand;
        this.addPand = addPand;
        this.editPand = editPand;
        this.deletePand = deletePand;

        this.viewJobOrder = viewJobOrder;
        this.addJobOrder = addJobOrder;
        this.editJobOrder = editJobOrder;
        this.deleteJobOrder = deleteJobOrder;

        this.viewExitJobOrder = viewExitJobOrder;
        this.addExitJobOrder = addExitJobOrder;
        this.editExitJobOrder = editExitJobOrder;
        this.deleteExitJobOrder = deleteExitJobOrder;

        this.showReports = showReports;
        this.recordDelivered = recordDelivered;
        this.allAuth = allAuth;
        this.viewOnly = viewOnly;
        this.editUser = editUser;

        this.manufacturingManager = manufacturingManager;
        this.storeManager = storeManager;
        this.purchasingManager = purchasingManager;
        this.user = user;
    }


    public boolean isEditUser() {
        return editUser;
    }

    public void setEditUser(boolean editUser) {
        this.editUser = editUser;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Set<Role> getRoles() {
        return roles;
    }

    public void setRoles(Set<Role> roles) {
        this.roles = roles;
    }

    @Override
    public String toString() {
        return "eg.com.khales.paymentgateway.models.User[ id=" + id + " ]";
    }


    public BigInteger getUserrole() {
        return userrole;
    }

    public void setUserrole(BigInteger userrole) {
        this.userrole = userrole;
    }

    public BigInteger getIsactive() {
        return isactive;
    }

    public void setIsactive(BigInteger isactive) {
        this.isactive = isactive;
    }

    public BigInteger getMerchantId() {
        return merchantId;
    }

    public void setMerchantId(BigInteger merchantId) {
        this.merchantId = merchantId;
    }

    public BigInteger getIsFirstTime() {
        return isFirstTime;
    }

    public void setIsFirstTime(BigInteger isFirstTime) {
        this.isFirstTime = isFirstTime;
    }

    public boolean isAddProject() {
        return addProject;
    }

    public void setAddProject(boolean addProject) {
        this.addProject = addProject;
    }

    public boolean isAddPand() {
        return addPand;
    }

    public void setAddPand(boolean addPand) {
        this.addPand = addPand;
    }

    public boolean isEditPand() {
        return editPand;
    }

    public void setEditPand(boolean editPand) {
        this.editPand = editPand;
    }

    public boolean isAddJobOrder() {
        return addJobOrder;
    }

    public void setAddJobOrder(boolean addJobOrder) {
        this.addJobOrder = addJobOrder;
    }

    public boolean isEditJobOrder() {
        return editJobOrder;
    }

    public void setEditJobOrder(boolean editJobOrder) {
        this.editJobOrder = editJobOrder;
    }

    public boolean isAddExitJobOrder() {
        return addExitJobOrder;
    }

    public void setAddExitJobOrder(boolean addExitJobOrder) {
        this.addExitJobOrder = addExitJobOrder;
    }

    public boolean isEditExitJobOrder() {
        return editExitJobOrder;
    }

    public void setEditExitJobOrder(boolean editExitJobOrder) {
        this.editExitJobOrder = editExitJobOrder;
    }

    public boolean isShowReports() {
        return showReports;
    }

    public void setShowReports(boolean showReports) {
        this.showReports = showReports;
    }

    public boolean isRecordDelivered() {
        return recordDelivered;
    }

    public void setRecordDelivered(boolean recordDelivered) {
        this.recordDelivered = recordDelivered;
    }

    public boolean isAllAuth() {
        return allAuth;
    }

    public void setAllAuth(boolean allAuth) {
        this.allAuth = allAuth;
    }

    public boolean isViewProject() {
        return viewProject;
    }

    public void setViewProject(boolean viewProject) {
        this.viewProject = viewProject;
    }

    public boolean isEditProject() {
        return editProject;
    }

    public void setEditProject(boolean updateProject) {
        this.editProject = updateProject;
    }

    public boolean isDeleteProject() {
        return deleteProject;
    }

    public void setDeleteProject(boolean deleteProject) {
        this.deleteProject = deleteProject;
    }

    public boolean isViewPand() {
        return viewPand;
    }

    public void setViewPand(boolean viewPand) {
        this.viewPand = viewPand;
    }

    public boolean isDeletePand() {
        return deletePand;
    }

    public void setDeletePand(boolean deletePand) {
        this.deletePand = deletePand;
    }

    public boolean isViewJobOrder() {
        return viewJobOrder;
    }

    public void setViewJobOrder(boolean viewJobOrder) {
        this.viewJobOrder = viewJobOrder;
    }

    public boolean isDeleteJobOrder() {
        return deleteJobOrder;
    }

    public void setDeleteJobOrder(boolean deleteJobOrder) {
        this.deleteJobOrder = deleteJobOrder;
    }

    public boolean isViewExitJobOrder() {
        return viewExitJobOrder;
    }

    public void setViewExitJobOrder(boolean viewExitJobOrder) {
        this.viewExitJobOrder = viewExitJobOrder;
    }

    public boolean isDeleteExitJobOrder() {
        return deleteExitJobOrder;
    }

    public void setDeleteExitJobOrder(boolean deleteExitJobOrder) {
        this.deleteExitJobOrder = deleteExitJobOrder;
    }

    public boolean isViewOnly() {
        return viewOnly;
    }

    public void setViewOnly(boolean viewOnly) {
        this.viewOnly = viewOnly;
    }

    public boolean isManufacturingManager() {
        return manufacturingManager;
    }

    public boolean isUser() {
        return user;
    }

    public void setUser(boolean user) {
        this.user = user;
    }

    public void setManufacturingManager(boolean manufacturingManager) {
        this.manufacturingManager = manufacturingManager;
    }

    public boolean isStoreManager() {
        return storeManager;
    }

    public void setStoreManager(boolean storeManager) {
        this.storeManager = storeManager;
    }

    public boolean isPurchasingManager() {
        return purchasingManager;
    }

    public void setPurchasingManager(boolean purchasingManager) {
        this.purchasingManager = purchasingManager;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (id != null ? id.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof User)) {
            return false;
        }
        User other = (User) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }
}
