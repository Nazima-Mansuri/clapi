package com.brewconsulting.DB;

import com.brewconsulting.DB.common.DBConnectionProvider;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class Permissions {

    public static String permissionName = null;
    public static int roleId = 0;

    /***
     *  Method is used to get Permission from database and Return it.
     *
     * @param userRole
     * @param entity
     * @return
     * @throws Exception
     */
    public static String isAuthorised(int userRole, int entity) throws Exception {

        Connection con = DBConnectionProvider.getConn();
        PreparedStatement stmt = null;
        ResultSet result = null;
        try {
            if (con != null) {
                stmt = con.prepareStatement("SELECT permissions  from master.permissionrolemap where roleid = ? and entityid = ?");
                stmt.setInt(1, userRole);
                stmt.setInt(2, entity);
                result = stmt.executeQuery();
                if (result.next()) {
                    permissionName = result.getString(1);
                }

            }

            return "Write";
        } finally {
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