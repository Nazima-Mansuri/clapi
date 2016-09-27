package com.brewconsulting.DB;

public class Permissions {
	public static final int USER_PROFILE = 0x01; //access to others profile.
	public static final int DIVISION = 0x02; //access to others profile.
	public static final int TERRITORY = 0x03; //access to others profile.
	public static final int PRODUCT = 0x04; //access to others profile.
	
	public static final int NONE = 0x01;
	public static final int READ_ONLY = 0x3;
	public static final int READ_WRITE = 0x7;
	
	//ensure that these values are teh same as that in the DB. 
	//Later we will get this data from DB directly.
	public static final int ROLE_ROOT = 0;
	public static final int ROLE_MR = 1;
	public static final int ROLE_MKT = 2;
	
	public static final int[][][] Permissions = {
			{{ROLE_ROOT, USER_PROFILE, READ_WRITE}},
			{{ROLE_MR, USER_PROFILE, NONE}},
			{{ROLE_MKT, USER_PROFILE, READ_ONLY}}
	};
	
	public static boolean isAuthorised(int userRole, int entity, int accesslevel){
		boolean retval = false;
		System.out.println(userRole+""+entity+""+accesslevel);
		for(int i =0;i<Permissions[userRole].length;i++){
			if((Permissions[userRole][i][1] ^ entity) == 0){
				 retval = (Permissions[userRole][i][2] & accesslevel) == accesslevel;
				 System.out.println(Permissions[userRole][i][2] & accesslevel);
				 break;
			}
		}
		
		return retval;
	}
	
}
