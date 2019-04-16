package pt.unl.fct.di.apdc.projind.util;

public class GBORegisterData {

	public RegisterData rd;
	public AuthToken token;

	public GBORegisterData() {}

	public GBORegisterData(AuthToken token, RegisterData rd) {
		this.token = token;
		this.rd = rd;
	}

	public boolean isValid() {
		if (rd.validRegistration() || token != null) return true; else return false;
	}
}
