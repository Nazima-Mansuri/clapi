package com.brewconsulting.DB.masters;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.ws.rs.NotAuthorizedException;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import org.glassfish.jersey.media.multipart.FormDataParam;

import com.amazonaws.auth.PropertiesCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.PutObjectResult;
import com.brewconsulting.DB.Permissions;
import com.brewconsulting.DB.common.DBConnectionProvider;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;

public class Product {

    @JsonProperty("id")
    public int id;

    @JsonProperty("name")
    public String name;

    @JsonProperty("description")
    public String description;

    @JsonProperty("image")
    public String image;

    @JsonProperty("isActive")
    public Boolean isActive;

    @JsonProperty("division")
    public int division;

    @JsonProperty("divName")
    public String divName;

    @JsonProperty("createDate")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy'T'hh:mm:ss.Z")
    public Date createDate;

    @JsonProperty("createBy")
    public int createBy;

    @JsonProperty("updateDate")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy'T'hh:mm:ss.Z")
    public Date updateDate;

    @JsonProperty("updateBy")
    public int updateBy;

    @JsonProperty("username")
    public String username;

    @JsonProperty("Firstname")
    public String firstname;

    @JsonProperty("Lastname")
    public String lastname;

    @JsonProperty("addLine1")
    public String addLine1;

    @JsonProperty("addLine2")
    public String addLine2;

    @JsonProperty("addLine3")
    public String addLine3;

    @JsonProperty("city")
    public String city;

    @JsonProperty("state")
    public String state;

    @JsonProperty("phones")
    public String[] phones;

    public Product() {

    }

    /***
     * Method allows user to get All Details of Products.
     *
     * @param divid
     * @param loggedInUser
     * @return
     * @throws Exception
     */
    public static List<Product> getAllProducts(int divid, LoggedInUser loggedInUser)
            throws Exception {
        // TODO: check authorization of the user to see this data

        int userRole = loggedInUser.roles.get(0).roleId;

        if (Permissions.isAuthorised(userRole, Permissions.PRODUCT,
                Permissions.getAccessLevel(userRole))) {

            String schemaName = loggedInUser.schemaName;
            Connection con = DBConnectionProvider.getConn();
            ArrayList<Product> products = new ArrayList<Product>();
            PreparedStatement stmt = null;
            ResultSet result = null;

            try {
                if (con != null) {

                    if (divid != -1) {
                        stmt = con
                                .prepareStatement("select p.id, p.name,p.image, p.description,p.division,p.isActive, p.createDate,"
                                        + "p.createBy, p.updateDate,p.updateBy,(address).addLine1 addLine1," +
                                        "(address).addLine2 addLine2,(address).addLine3 addLine3,(address).city city," +
                                        "(address).state state,(address).phone phones ,d.name from "
                                        + schemaName
                                        + ".products p left join "
                                        + schemaName
                                        + ".userprofile u on p.updateby = u.userid" +
                                        " left join " + schemaName + ".divisions d on  p.division = d.id where p.division=? ORDER BY p.createDate DESC ");
                        stmt.setInt(1, divid);
                        result = stmt.executeQuery();
                        while (result.next()) {
                            Product product = new Product();
                            product.id = result.getInt(1);
                            product.name = result.getString(2);
                            if (!result.getString(3).contains("null")) {
                                product.image = result.getString(3);
                            } else {
                                product.image = "https://s3.amazonaws.com/com.brewconsulting.client1/Product/1475134095978_no_image.png";
                            }
                            product.description = result.getString(4);
                            product.division = result.getInt(5);
                            product.isActive = result.getBoolean(6);
                            product.createDate = new java.sql.Date(result.getTimestamp(7).getTime());
                            product.createBy = result.getInt(8);
                            product.updateDate = new java.sql.Date(result.getTimestamp(9).getTime());
                            product.updateBy = result.getInt(10);
                            product.addLine1 = result.getString(11);
                            product.addLine2 = result.getString(12);
                            product.addLine3 = result.getString(13);
                            product.city = result.getString(14);
                            product.state = result.getString(15);
                            if (result.getArray(16) != null)
                                product.phones = (String[]) result.getArray(16)
                                        .getArray();
                            product.divName = result.getString(17);
                            product.firstname = loggedInUser.firstName;
                            product.lastname = loggedInUser.lastName;
                            product.username = loggedInUser.username;
                            products.add(product);
                        }
                    } else {
                        stmt = con
                                .prepareStatement("select p.id, p.name,p.image, p.description,p.division,p.isActive, p.createDate,"
                                        + "p.createBy, p.updateDate,p.updateBy,(address).addLine1 addLine1," +
                                        "(address).addLine2 addLine2,(address).addLine3 addLine3,(address).city city," +
                                        "(address).state state,(address).phone phones ,d.name from "
                                        + schemaName
                                        + ".products p left join "
                                        + schemaName
                                        + ".userprofile u on p.updateby = u.userid" +
                                        " left join " + schemaName + ".divisions d on  p.division = d.id  ORDER BY p.createDate DESC ");

                        result = stmt.executeQuery();
                        while (result.next()) {
                            Product product = new Product();
                            product.id = result.getInt(1);
                            product.name = result.getString(2);
                            if (!result.getString(3).contains("null")) {
                                product.image = result.getString(3);
                            } else {
                                product.image = "https://s3.amazonaws.com/com.brewconsulting.client1/Product/1475134095978_no_image.png";
                            }
                            product.description = result.getString(4);
                            product.division = result.getInt(5);
                            product.isActive = result.getBoolean(6);
                            product.createDate = new java.sql.Date(result.getTimestamp(7).getTime());
                            product.createBy = result.getInt(8);
                            product.updateDate = new java.sql.Date(result.getTimestamp(9).getTime());
                            product.updateBy = result.getInt(10);
                            product.addLine1 = result.getString(11);
                            product.addLine2 = result.getString(12);
                            product.addLine3 = result.getString(13);
                            product.city = result.getString(14);
                            product.state = result.getString(15);
                            if (result.getArray(16) != null)
                                product.phones = (String[]) result.getArray(16)
                                        .getArray();
                            product.divName = result.getString(17);
                            product.firstname = loggedInUser.firstName;
                            product.lastname = loggedInUser.lastName;
                            product.username = loggedInUser.username;
                            products.add(product);
                        }
                    }
                }
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

            return products;
        } else {

            throw new NotAuthorizedException("");

        }
    }

    /***
     * Method allows user to get Details of Particular Product.
     *
     * @param loggedInUser
     * @param id
     * @return
     * @throws Exception
     */
    public static Product getProductById(int id, LoggedInUser loggedInUser)
            throws Exception {

        int userRole = loggedInUser.roles.get(0).roleId;

        if (Permissions.isAuthorised(userRole, Permissions.PRODUCT,
                Permissions.getAccessLevel(userRole))) {

            Product product = null;
            // TODO check authorization
            String schemaName = loggedInUser.schemaName;
            Connection con = DBConnectionProvider.getConn();
            PreparedStatement stmt = null;
            ResultSet result = null;

            try {
                if (con != null) {
                    stmt = con
                            .prepareStatement("select p.id, p.name,p.image, p.description,p.division,p.isActive, p.createDate,"
                                    + "p.createBy, p.updateDate,p.updateBy,(address).addLine1 addLine1,(address).addLine2 addLine2,(address).addLine3 addLine3,(address).city city,(address).state state,(address).phone phones from "
                                    + schemaName
                                    + ".products p left join "
                                    + schemaName
                                    + ".userprofile u on p.updateby = u.userid"
                                    + " where id = ?");
                    stmt.setInt(1, id);
                    result = stmt.executeQuery();
                    if (result.next()) {
                        product = new Product();
                        product.id = result.getInt(1);
                        product.name = result.getString(2);
                        if (!result.getString(3).contains("null")) {
                            product.image = result.getString(3);
                        } else {
                            product.image = "https://s3.amazonaws.com/com.brewconsulting.client1/Product/1475134095978_no_image.png";
                        }
                        product.description = result.getString(4);
                        product.division = result.getInt(5);
                        product.isActive = result.getBoolean(6);
                        product.createDate = new java.sql.Date(result.getTimestamp(7).getTime());
                        product.createBy = result.getInt(8);
                        product.updateDate = new java.sql.Date(result.getTimestamp(9).getTime());
                        product.updateBy = result.getInt(10);
                        product.addLine1 = result.getString(11);
                        product.addLine2 = result.getString(12);
                        product.addLine3 = result.getString(13);
                        product.city = result.getString(14);
                        product.state = result.getString(15);
                        if (result.getArray(16) != null)
                            product.phones = (String[]) result.getArray(16)
                                    .getArray();
                        product.firstname = loggedInUser.firstName;
                        product.lastname = loggedInUser.lastName;
                        product.username = loggedInUser.username;
                    }
                } else
                    throw new Exception("DB connection is null");

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
            return product;
        } else {
            throw new NotAuthorizedException("");
        }
    }

    /***
     * Method allows user to insert Product in Database.
     *
     * @param name
     * @param image
     * @param description
     * @param division
     * @param isActive
     * @param loggedInUser
     * @return
     * @throws Exception
     */
    public static int addProduct(String name, String image, String description,
                                 int division, Boolean isActive, LoggedInUser loggedInUser)
            throws Exception {
        // TODO: check authorization of the user to Insert data

        int userRole = loggedInUser.roles.get(0).roleId;

        if (Permissions.isAuthorised(userRole, Permissions.PRODUCT,
                Permissions.getAccessLevel(userRole))) {

            String schemaName = loggedInUser.schemaName;
            Connection con = DBConnectionProvider.getConn();
            PreparedStatement stmt = null;
            int result = 0;

            try {
                con.setAutoCommit(false);

                stmt = con
                        .prepareStatement(
                                "INSERT INTO "
                                        + schemaName
                                        + ".products(name,image,description,division,isActive,createDate,"
                                        + "createBy,updateDate,updateBy) values (?,?,?,?,?,?,?,?,?)",
                                Statement.RETURN_GENERATED_KEYS);

                stmt.setString(1, name);

                stmt.setString(2, image);

                // It checks that description is empty or not
                if (description != null)
                    stmt.setString(3, description);
                else
                    stmt.setString(3, null);

                stmt.setInt(4, division);

                // Checks isActive empty or not
                if (isActive != null)
                    stmt.setBoolean(5, isActive);
                else
                    // If isActive empty it set default TRUE
                    stmt.setBoolean(5, true);

                stmt.setTimestamp(6, new Timestamp((new Date()).getTime()));
                stmt.setInt(7, loggedInUser.id);
                stmt.setTimestamp(8, new Timestamp((new Date()).getTime()));
                stmt.setInt(9, loggedInUser.id);
                result = stmt.executeUpdate();

                if (result == 0)
                    throw new SQLException("Add Product Failed.");

                ResultSet generatedKeys = stmt.getGeneratedKeys();
                int productId;
                if (generatedKeys.next())
                    // It gives last inserted Id in divisionId
                    productId = generatedKeys.getInt(1);
                else
                    throw new SQLException("No ID obtained");

                con.commit();
                return productId;
            } catch (Exception ex) {
                if (con != null)
                    con.rollback();
                throw ex;
            } finally {
                con.setAutoCommit(false);
                if (con != null)
                    con.close();
            }
        } else {
            throw new NotAuthorizedException("");
        }

    }

    /***
     * Method allows user to Update Product in Database.
     *
     * @param name
     * @param image
     * @param description
     * @param division
     * @param isActive
     * @param id
     * @param loggedInUser
     * @return
     * @throws Exception
     */
    public static int updateProduct(String name, String image,
                                    String description, int division, Boolean isActive, int id,
                                    LoggedInUser loggedInUser) throws Exception {
        // TODO: check authorization of the user to Update data

        int userRole = loggedInUser.roles.get(0).roleId;

        if (Permissions.isAuthorised(userRole, Permissions.PRODUCT,
                Permissions.getAccessLevel(userRole))) {

            String schemaName = loggedInUser.schemaName;
            Connection con = DBConnectionProvider.getConn();
            PreparedStatement stmt = null;
            int result = 0;
            try {
                if (con != null) {
                    stmt = con
                            .prepareStatement("UPDATE "
                                    + schemaName
                                    + ".products SET name = ?,image = ?,description = ?,division = ?,isActive = ?"
                                    + ",updateDate = ?, updateBy = ? WHERE id = ?");
                    stmt.setString(1, name);

                    stmt.setString(2, image);

                    // It checks that description is empty or not
                    if (description != null)
                        stmt.setString(3, description);
                    else
                        stmt.setString(3, null);

                    stmt.setInt(4, division);
                    // Checks isActive empty or not
                    if (isActive != null)
                        stmt.setBoolean(5, isActive);
                    else
                        // If isActive empty it set default TRUE
                        stmt.setBoolean(5, true);
                    stmt.setTimestamp(6, new Timestamp((new Date()).getTime()));
                    stmt.setInt(7, loggedInUser.id);
                    stmt.setInt(8, id);

                    result = stmt.executeUpdate();
                } else
                    throw new Exception("DB connection is null");

            } finally {
                if (stmt != null)
                    if (!stmt.isClosed())
                        stmt.close();
                if (con != null)
                    if (!con.isClosed())
                        con.close();
            }
            return result;
        } else {
            throw new NotAuthorizedException("");
        }
    }

    /***
     * Method allows user to Delete Product from Database.
     *
     * @param loggedInUser
     * @param id
     * @throws Exception
     * @Return
     */

    public static int deleteProduct(int id, LoggedInUser loggedInUser)
            throws Exception {
        // TODO: check authorization of the user to Delete data

        int userRole = loggedInUser.roles.get(0).roleId;

        if (Permissions.isAuthorised(userRole, Permissions.PRODUCT,
                Permissions.getAccessLevel(userRole))) {

            String schemaName = loggedInUser.schemaName;
            Connection con = DBConnectionProvider.getConn();
            PreparedStatement stmt = null;
            int result = 0;

            try {
                if (con != null) {
                    stmt = con.prepareStatement("DELETE FROM " + schemaName
                            + ".products WHERE id = ?");

                    stmt.setInt(1, id);
                    result = stmt.executeUpdate();
                } else
                    throw new Exception("DB connection is null");
            } finally {

                if (stmt != null)
                    if (!stmt.isClosed())
                        stmt.close();
                if (con != null)
                    if (!con.isClosed())
                        con.close();
            }
            return result;
        } else {
            throw new NotAuthorizedException("");
        }
    }

    /**
     * Method allows to store image in AWS bucket.
     *
     * @param inputStream
     * @param fileName
     * @return
     * @throws IOException
     */

    // save uploaded file to new location
    public static String writeToFile(InputStream inputStream, String fileName)
            throws IOException {

        String existingBucketName = "com.brewconsulting.client1";
        String finalUrl = null;
        String amazonFileUploadLocationOriginal = existingBucketName
                + "/Product";

        try {

            AWSCredentials awsCredentials = new BasicAWSCredentials("AKIAJZZRFGQGNZIDUFTQ", "12uUP7pQrvR3Kf0GpyeJr328RQ/a1m8TI+/8w2X8");
            AmazonS3 s3Client = new AmazonS3Client(awsCredentials);

            ObjectMetadata objectMetadata = new ObjectMetadata();
            PutObjectRequest putObjectRequest = new PutObjectRequest(
                    amazonFileUploadLocationOriginal, fileName, inputStream,
                    objectMetadata);
            PutObjectResult result = s3Client.putObject(putObjectRequest);
            System.out.println("Etag:" + result.getETag() + "-->" + result);

            finalUrl = "https://s3.amazonaws.com/"
                    + amazonFileUploadLocationOriginal + "/" + fileName;
            System.out.println(finalUrl);

        } catch (Exception ioe) {
            ioe.printStackTrace();
        }

        return finalUrl;
    }

}