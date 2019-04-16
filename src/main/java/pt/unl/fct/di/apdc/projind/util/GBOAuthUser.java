package pt.unl.fct.di.apdc.projind.util;

public class GBOAuthUser {

	public AuthToken token;
	public String username;
	
	public GBOAuthUser() {}
	
	public GBOAuthUser(AuthToken token, String username) {
		this.token = token;
		this.username = username;
	}
	
	public boolean isValid() {
		if (username.isEmpty() || token == null) return false; else return true;
	}
	
}
