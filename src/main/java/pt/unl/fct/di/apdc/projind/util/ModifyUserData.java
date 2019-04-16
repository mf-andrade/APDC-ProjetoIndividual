package pt.unl.fct.di.apdc.projind.util;

public class ModifyUserData {

	public AuthToken token;
	public String password = "";
	public String email = "";
	public String profileStatus = "";
	public String phoneNumber = "";
	public String mobilePhoneNumber = "";
	public String address = "";

	public ModifyUserData() { }

	public ModifyUserData(AuthToken token, String email, String password, String profileStatus,
			String phoneNumber, String mobilePhoneNumber, String address) {
		this.token = token;
		this.password = password;
		this.email = email;
		this.profileStatus = profileStatus;
		this.phoneNumber = phoneNumber;
		this.mobilePhoneNumber = mobilePhoneNumber;
		this.address = address;
	}
}
