package pt.unl.fct.di.apdc.projind.util;

public class AdminFLogInData {
    public String username;
    public long failedAttempts;

    public AdminFLogInData() {}

    public AdminFLogInData(String username, int failedAttempts) {
    	this.username = username;
    	this.failedAttempts = failedAttempts;
    }
}
