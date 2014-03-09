/*
 * $Id$
 * $HeadURL$
 */
package shouter.api.handlers;

import shouter.api.beans.ApiError;
import shouter.api.dao.AwsDao;

import javax.servlet.http.HttpServletRequest;
import java.util.Set;
import java.util.TreeSet;

/**
 * Base API Handler for which all endpoints are based off. Houses all common objects
 * and functionality.
 *
 * @author Charles Effinger (charles.effinger@cbsinteractive.com)
 * @version $Revision$, $LastChangedDate$
 */
public class BaseApiHandler {

    protected final HttpServletRequest request;
    protected final AwsDao awsDao; // data access object for the AWS DynamoDB
    protected Set<ApiError> errors; // validation/processing errors
    protected Set<Object> responseObjects; // the objects to be returned in json
    protected String responseString = "";  // the map key for the json objects

    public BaseApiHandler(HttpServletRequest request) {
        this.request = request;
        this.errors = new TreeSet<ApiError>();
        this.awsDao = new AwsDao();
        this.responseObjects = new TreeSet<Object>();
    }

    /**
     * Handles the base handling logic for all endpoints
     */
    public final void handleRequest() {
        if (validateParameters()) {
            performRequest();
        } else {
            responseString = "errors";
            responseObjects.clear();
            responseObjects.addAll(errors);
        }
    }

    /**
     * Base validation class for validating the parameters of the call.
     *
     * @return if any errors were discovered in validation
     */
    protected boolean validateParameters() {
        return errors.isEmpty();
    }

    /**
     * Performs the request for all API endpoints, including data access logic
     */
    protected void performRequest() {
    }


    public Set<Object> getResponseObjects() {
        return responseObjects;
    }

    public String getResponseString() {
        return responseString;
    }

}
