package pt.unl.fct.di.apdc.projind.util;

public class RegisterData {

	public static final String PROFILE_PUBLIC = "public";
	public static final String PROFILE_PRIVATE = "private";
	
	public String username;
	public String password;
	public String email;
	public String name;
	public String profileStatus;
	public String phoneNumber;
	public String mobilePhoneNumber;
	public String address;
	
	public RegisterData() { }
	
	public RegisterData(String username, String password, String email, String name, String profileStatus,
			String phoneNumber, String mobilePhoneNumber, String address) {
		this.username = username;
		this.password = password;
		this.email = email;
		this.name = name;
		this.profileStatus = profileStatus;
		this.phoneNumber = phoneNumber;
		this.mobilePhoneNumber = mobilePhoneNumber;
		this.address = address;
	}
	
	public boolean validRegistration() {
		if ( !(profileStatus.equals(PROFILE_PRIVATE) || profileStatus.equals(PROFILE_PUBLIC)) ) return false;
		if (username.isEmpty() || password.isEmpty() || email.isEmpty() ||
			name.isEmpty() || phoneNumber.isEmpty() || mobilePhoneNumber.isEmpty() ||
			address.isEmpty() || !email.contains("@")) return false; else return true;
	}
}