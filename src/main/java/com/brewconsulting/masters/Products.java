package com.brewconsulting.masters;

import com.amazonaws.services.apigateway.model.ConflictException;
import com.brewconsulting.DB.masters.LoggedInUser;
import com.brewconsulting.DB.masters.Product;
import com.brewconsulting.exceptions.NoDataFound;
import com.brewconsulting.login.Secured;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.postgresql.util.PSQLException;

import javax.ws.rs.*;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

@Path("products")
public class Products {
    ObjectMapper mapper = new ObjectMapper();
    static final Logger logger = Logger.getLogger(Products.class);
    Properties properties = new Properties();
    InputStream inp = getClass().getClassLoader().getResourceAsStream("log4j.properties");

    /***
     * Produces a list of all products
     *
     * @param crc
     * @return
     */

    @GET
    @Produces("application/json")
    @Secured
    @Path("/productsbydiv/{divid}")
    public Response getAllproducts(@PathParam("divid") Integer divid, @Context ContainerRequestContext crc) {
        Response resp = null;

        try {
            properties.load(inp);
            PropertyConfigurator.configure(properties);

            resp = Response.ok(
                    mapper.writeValueAsString(Product
                            .getAllProducts(divid, (LoggedInUser) crc
                                    .getProperty("userObject")))).build();
        } catch (NotAuthorizedException na) {
            logger.error("NotAuthorizedException ", na);
            resp = Response.status(Response.Status.FORBIDDEN)
                    .entity("{\"Message\":" + "\"You are not authorized to get products \"}")
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        } catch (Exception e) {
            logger.error("Exception ", e);
            resp = Response.serverError().entity("{\"Message\":" + "\"" + e.getMessage() + "\"}").build();
            e.printStackTrace();
        }

        return resp;
    }

    /***
     * get a particular product
     *
     * @param id
     * @param crc
     * @return
     */
    @GET
    @Produces("application/json")
    @Secured
    @Path("{id}")
    public Response products(@PathParam("id") Integer id,
                             @Context ContainerRequestContext crc) {
        Response resp = null;
        try {
            properties.load(inp);
            PropertyConfigurator.configure(properties);

            Product product = Product.getProductById(id,
                    (LoggedInUser) crc.getProperty("userObject"));
            if (product == null) {
                resp = Response
                        .noContent()
                        .entity(new NoDataFound("This product does not exist")
                                .getJsonString()).build();
            } else
                resp = Response.ok(mapper.writeValueAsString(product)).build();

        } catch (NotAuthorizedException na) {
            logger.error("NotAuthorizedException ", na);
            resp = Response.status(Response.Status.FORBIDDEN)
                    .entity("{\"Message\":" + "\"You are not authorized to get product\"}")
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        } catch (Exception e) {
            logger.error("Exception ", e);
            resp = Response.serverError().entity("{\"Message\":" + "\"" + e.getMessage() + "\"}").build();
            e.printStackTrace();
        }
        return resp;
    }

    /***
     * Add new Product
     *
     * @param fileInputStream
     * @param fileFormDataContentDisposition
     * @param name
     * @param description
     * @param division
     * @param isActive
     * @param crc
     * @return
     */
    @POST
    @Secured
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response createPro(
            @FormDataParam("uploadFile") InputStream fileInputStream,
            @FormDataParam("uploadFile") FormDataContentDisposition fileFormDataContentDisposition,
            @FormDataParam("name") String name,
            @FormDataParam("description") String description,
            @FormDataParam("division") int division,
            @FormDataParam("isActive") Boolean isActive,
            @Context ContainerRequestContext crc) {

        Response resp = null;
        // local variables
        String fileName = null;
        String uploadFilePath = "";
        ObjectMapper mapper = new ObjectMapper();

        try {
            properties.load(inp);
            PropertyConfigurator.configure(properties);


            if (fileFormDataContentDisposition != null) {
                if (fileFormDataContentDisposition.getFileName() != null) {
                    fileName = System.currentTimeMillis() + "_"
                            + fileFormDataContentDisposition.getFileName();
                    // This method is used to store image in AWS bucket.
                    uploadFilePath = Product.writeToFile(fileInputStream, fileName);
                } else {
                    uploadFilePath = "";
                }
            } else {
                uploadFilePath = "";
            }

            int productId = Product.addProduct(name, uploadFilePath,
                    description, division, isActive,
                    (LoggedInUser) crc.getProperty("userObject"));

            if (productId != 0)
                resp = Response.ok("{\"id\":" + productId + "}").build();
            else
                resp = Response
                        .noContent()
                        .entity(new NoDataFound("Unable to Insert Product")
                                .getJsonString()).build();

        } catch (NotAuthorizedException na) {
            logger.error("NotAuthorizedException ", na);
            resp = Response.status(Response.Status.FORBIDDEN)
                    .entity("{\"Message\":" + "\"You are not authorized to add product\"}")
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        } catch (IOException e) {
            logger.error("IOException ", e);
            if (resp == null) {
                resp = Response.serverError().entity("{\"Message\":" + "\"" + e.getMessage() + "\"}").build();
                e.printStackTrace();
            }
        } catch (Exception e) {
            logger.error("Exception ", e);
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return resp;
    }

    /***
     * Updates a Product.
     *
     * @param fileInputStream
     * @param fileFormDataContentDisposition
     * @param name
     * @param description
     * @param isActive
     * @param id
     * @param crc
     * @return
     */

    @PUT
    @Secured
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response updatePro(
            @FormDataParam("uploadFile") InputStream fileInputStream,
            @FormDataParam("uploadFile") FormDataContentDisposition fileFormDataContentDisposition,
            @FormDataParam("name") String name,
            @FormDataParam("description") String description,
            @FormDataParam("isActive") boolean isActive,
            @FormDataParam("isUpdated") boolean isUpdated,
            @FormDataParam("url") String url,
            @FormDataParam("id") int id, @Context ContainerRequestContext crc) {

        Response resp = null;
        String fileName = null;
        String uploadFilePath = "";
        ObjectMapper mapper = new ObjectMapper();
        try {
            properties.load(inp);
            PropertyConfigurator.configure(properties);

            if (isUpdated) {
                if (fileFormDataContentDisposition != null) {
                    if (fileFormDataContentDisposition.getFileName() != null) {
                        fileName = System.currentTimeMillis() + "_"
                                + fileFormDataContentDisposition.getFileName();
                        // This method is used to store image in AWS bucket.
                        uploadFilePath = Product.writeToFile(fileInputStream, fileName);
                    } else {
//                        uploadFilePath = "https://s3.amazonaws.com/com.brewconsulting.client1/Product/1475134095978_no_image.png";
                        uploadFilePath = "";
                    }
                } else {
                    uploadFilePath = "";
                }
            } else {
                uploadFilePath = url;
            }

            int affectedRow = Product.updateProduct(name, uploadFilePath,
                    description, isActive, id,
                    (LoggedInUser) crc.getProperty("userObject"));
            if (affectedRow > 0)
                resp = Response.ok().build();
            else
                resp = Response.status(204).build();
        } catch (NotAuthorizedException na) {
            logger.error("NotAuthorizedException ", na);
            resp = Response.status(Response.Status.FORBIDDEN)
                    .entity("{\"Message\":" + "\"You are not authorized to update product\"}")
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        } catch (IOException e) {
            logger.error("IOException ", e);
            if (resp == null)
                resp = Response.serverError().entity("{\"Message\":" + "\"" + e.getMessage() + "\"}").build();
            e.printStackTrace();
        } catch (Exception e) {
            logger.error(" Exception ", e);
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return resp;
    }

    /***
     * Delete a Product
     *
     * @param id
     * @param crc
     * @return
     */
    @DELETE
    @Produces("application/json")
    @Secured
    @Path("{id}")
    public Response deletePro(@PathParam("id") Integer id,
                              @Context ContainerRequestContext crc) {
        Response resp = null;
        try {
            properties.load(inp);
            PropertyConfigurator.configure(properties);

            int affectedRow = Product.deleteProduct(id,
                    (LoggedInUser) crc.getProperty("userObject"));
            if (affectedRow > 0)
                resp = Response.ok().build();
            else
                // If no rows affected in database. It gives server status
                // 204(NO_CONTENT).
                resp = Response.status(204).build();

        } catch (NotAuthorizedException na) {
            logger.error("NotAuthorizedException ", na);
            resp = Response.status(Response.Status.FORBIDDEN)
                    .entity("{\"Message\":" + "\"You are not authorized to delete product\"}")
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        } catch (PSQLException ex) {
            logger.error(" PSQLException ", ex);
            resp = Response
                    .status(Response.Status.CONFLICT)
                    .entity("{\"Message\":" + "\"This id is already Use in another table as foreign key\"}")
                    .type(MediaType.APPLICATION_JSON).build();
            ex.printStackTrace();
        } catch (ConflictException ex) {
            logger.error(" ConflictException ", ex);
            resp = Response
                    .status(Response.Status.CONFLICT)
                    .entity("{\"Message\":" + "\"This id is already Use in another table as foreign key\"}")
                    .type(MediaType.APPLICATION_JSON).build();
            ex.printStackTrace();
        } catch (Exception e) {
            logger.error(" Exception ", e);
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return resp;
    }
}