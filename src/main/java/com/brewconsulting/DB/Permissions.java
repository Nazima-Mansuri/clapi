package com.brewconsulting.DB;

import com.brewconsulting.DB.common.DBConnectionProvider;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class Permissions {
	public static final int USER_PROFILE = 0x01; // access to others profile.
	public static final int DIVISION = 0x02; // access to others profile.
	public static final int TERRITORY = 0x03; // access to others profile.
	public static final int PRODUCT = 0x04; // access to others profile.

	public static final int NONE = 0x01;
	public static final int READ_ONLY = 0x3;
	public static final int READ_WRITE = 0x7;

	public static String permissionName = null;
	public static int roleId = 0;
	// ensure that these values are teh same as that in the DB.
	// Later we will get this data from DB directly.
/*
	public static final int ROLE_ROOT = 0;
	public static final int ROLE_MR = 1;
	public static final int ROLE_MKT = 2;
*/

//	public static final int MANAGEMENT = 1;

/*	public static final int[][][] Permissions = {
			{ { ROLE_ROOT, USER_PROFILE, READ_WRITE } },
			{ { ROLE_MR, USER_PROFILE, NONE } },
			{ { ROLE_MKT, USER_PROFILE, READ_ONLY } },
			{ { ROLE_ROOT, DIVISION, READ_WRITE } },
			{ { ROLE_MR, DIVISION, NONE } },
			{ { ROLE_MKT, DIVISION, READ_ONLY } },
			{ { ROLE_ROOT, TERRITORY, READ_WRITE } },
			{ { ROLE_MR, TERRITORY, NONE } },
			{ { ROLE_MKT, TERRITORY, READ_ONLY } },
			{ { ROLE_ROOT, PRODUCT, READ_WRITE } },
			{ { ROLE_MR, PRODUCT, NONE } },
			{ { ROLE_MKT, PRODUCT, READ_ONLY } },
	};*/

/*	public static boolean isAuthorised(int userRole, int entity, String accesslevel) {
		boolean retval = true;
		*//*System.out.println(userRole + "" + entity + "" + accesslevel);
		for (int i = 0; i < Permissions[userRole].length; i++) {

			if ((Permissions[userRole][i][1] ^ entity) == 0) {

				System.out.println(Permissions[userRole][i][1] ^ entity);

				retval = (Permissions[userRole][i][2] & accesslevel) == accesslevel;
				System.out.println(Permissions[userRole][i][2] & accesslevel);
				break;
			}
		}
*//*
		return retval;
	}*/

	public static String isAuthorised(int userRole,int entity) throws Exception {

		Connection con = DBConnectionProvider.getConn();
		PreparedStatement stmt = null;
		ResultSet result = null;
		try
		{
			if(con!= null) {
				stmt = con.prepareStatement("SELECT permissions  from master.permissionrolemap where roleid = ? and entityid = ?");
				stmt.setInt(1, userRole);
				stmt.setInt(2, entity);
				result = stmt.executeQuery();
				if (result.next()) {
					permissionName = result.getString(1);
				}

			}

			return "Write";
		}
		finally {
			if (result != null)
				if (!result.isClosed())
					result.close();
			if (stmt != null)
				if (!stmt.isClosed())
					stmt.close();
			if (con != null)
				if (!con.isClosed())
					con.close();
		}
	}
	}