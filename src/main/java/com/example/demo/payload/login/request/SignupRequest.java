package com.example.demo.payload.login.request;

public class SignupRequest {

    private String username;

    private String email;

    private String role;

//    private String oldPassword;

    private String password;

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
    private boolean editUser;
    private boolean allAuth;

    private boolean manufacturingManager;

    private boolean storeManager;

    private boolean purchasingManager;

    private boolean user;

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

//    public String getOldPassword() {
//        return oldPassword;
//    }
//
//    public void setOldPassword(String oldPassword) {
//        this.oldPassword = oldPassword;
//    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRole() {
        return this.role;
    }

    public void setRole(String role) {
        this.role = role;
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

    public void setEditProject(boolean editProject) {
        this.editProject = editProject;
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

    public boolean isEditUser() {
        return editUser;
    }

    public void setEditUser(boolean editUser) {
        this.editUser = editUser;
    }

    public boolean isManufacturingManager() {
        return manufacturingManager;
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

    public boolean isUser() {
        return user;
    }

    public void setUser(boolean user) {
        this.user = user;
    }
}
