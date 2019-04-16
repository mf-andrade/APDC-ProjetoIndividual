package pt.unl.fct.di.apdc.projind.util;

public class GBOModifyUserData {

	public AuthToken token;
	public ModifyUserData mud;
	public String username;

	public GBOModifyUserData() {}

	public GBOModifyUserData(AuthToken token, ModifyUserData mud, String username) {
		this.token = token;
		this.mud = mud;
		this.username = username;
	}

	public boolean isValid() {
		if (mud == null || token == null || username.isEmpty()) return false; else return true;
	}
}
