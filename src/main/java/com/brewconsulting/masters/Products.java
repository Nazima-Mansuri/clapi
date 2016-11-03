package com.brewconsulting.masters;

import java.io.IOException;
import java.io.InputStream;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.NotAuthorizedException;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.postgresql.util.PSQLException;

import com.brewconsulting.DB.masters.Division;
import com.brewconsulting.DB.masters.LoggedInUser;
import com.brewconsulting.DB.masters.Product;
import com.brewconsulting.exceptions.NoDataFound;
import com.brewconsulting.login.Secured;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Path("products")
public class Products {
    ObjectMapper mapper = new ObjectMapper();

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
    public Response getAllproducts(@PathParam("divid") Integer divid ,@Context ContainerRequestContext crc) {
        Response resp = null;

        try {
            resp = Response.ok(
                    mapper.writeValueAsString(Product
                            .getAllProducts(divid,(LoggedInUser) crc
                                    .getProperty("userObject")))).build();
        } catch (NotAuthorizedException na) {
            resp = Response.status(Response.Status.UNAUTHORIZED)
                    .header("content-type", MediaType.TEXT_PLAIN)
                    .entity("You are not authorized to get products").build();
        } catch (Exception e) {
            resp = Response.serverError().entity("{\"Message\":" + "\"" + e.getMessage()  +"\"}").build();
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
            resp = Response.status(Response.Status.UNAUTHORIZED)
                    .header("content-type", MediaType.TEXT_PLAIN)
                    .entity("You are not authorized to get product").build();
        } catch (Exception e) {
            resp = Response.serverError().entity("{\"Message\":" + "\"" + e.getMessage()  +"\"}").build();
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
        String uploadFilePath = null;
        ObjectMapper mapper = new ObjectMapper();

        try {

            if (fileFormDataContentDisposition != null) {
                fileName = System.currentTimeMillis() + "_"
                        + fileFormDataContentDisposition.getFileName();
                // This method is used to store image in AWS bucket.
                uploadFilePath = Product.writeToFile(fileInputStream, fileName);
            } else {
                uploadFilePath = null;
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
            resp = Response.status(Response.Status.UNAUTHORIZED)
                    .header("content-type", MediaType.TEXT_PLAIN)
                    .entity("You are not authorized to create product").build();
        } catch (IOException e) {
            if (resp == null) {
                resp = Response.serverError().entity("{\"Message\":" + "\"" + e.getMessage()  +"\"}").build();
                e.printStackTrace();
            }
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return resp;
    }

    /***
     * Updates a Product. Id of product is passedin input.
     *
     * @param fileInputStream
     * @param fileFormDataContentDisposition
     * @param name
     * @param description
     * @param division
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
            @FormDataParam("division") int division,
            @FormDataParam("isActive") boolean isActive,
            @FormDataParam("isUpdated") boolean isUpdated,
            @FormDataParam("url") String url,
            @FormDataParam("id") int id, @Context ContainerRequestContext crc) {

        Response resp = null;
        String fileName = null;
        String uploadFilePath = null;
        ObjectMapper mapper = new ObjectMapper();
        try {

            if(isUpdated)
            {
                if (fileFormDataContentDisposition != null) {
                    fileName = System.currentTimeMillis() + "_"
                            + fileFormDataContentDisposition.getFileName();
                    // This method is used to store image in AWS bucket.
                    uploadFilePath = Product.writeToFile(fileInputStream, fileName);
                } else {
                    uploadFilePath = null;
                }

            }
            else
            {
                uploadFilePath = url;
            }

            int affectedRow = Product.updateProduct(name, uploadFilePath,
                    description, division, isActive, id,
                    (LoggedInUser) crc.getProperty("userObject"));
            if (affectedRow > 0)
                resp = Response.ok().build();
            else
                resp = Response.status(204).build();
        } catch (NotAuthorizedException na) {
            resp = Response.status(Response.Status.UNAUTHORIZED)
                    .header("content-type", MediaType.TEXT_PLAIN)
                    .entity("You are not authorized to update product").build();
        } catch (IOException e) {
            if (resp == null)
                resp = Response.serverError().entity("{\"Message\":" + "\"" + e.getMessage()  +"\"}").build();
            e.printStackTrace();
        } catch (Exception e) {
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
            int affectedRow = Product.deleteProduct(id,
                    (LoggedInUser) crc.getProperty("userObject"));
            if (affectedRow > 0)
                resp = Response.ok().build();
            else
                // If no rows affected in database. It gives server status
                // 204(NO_CONTENT).
                resp = Response.status(204).build();

        } catch (NotAuthorizedException na) {
            resp = Response.status(Response.Status.UNAUTHORIZED)
                    .header("content-type", MediaType.TEXT_PLAIN)
                    .entity("You are not authorized to delete product").build();
        } catch (PSQLException ex) {
            resp = Response
                    .status(Response.Status.CONFLICT)
                    .entity("{\"Message\":" + "\"This id is already Use in another table as foreign key\"}")
                    .type(MediaType.APPLICATION_JSON).build();
            ex.printStackTrace();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return resp;
    }
}