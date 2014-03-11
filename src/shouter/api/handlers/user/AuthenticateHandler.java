/*
 * $Id$
 * $HeadURL$
 */
package shouter.api.handlers.user;

import shouter.api.ApiConstants;
import shouter.api.beans.ApiError;
import shouter.api.beans.User;
import shouter.api.handlers.BaseApiHandler;
import shouter.api.utils.DataUtil;
import shouter.api.utils.SecurityUtil;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Array;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;

/**
 * Handles authenticating a user's phone ID. If the phone hasn't been registered yet,
 * it will be automatically registered.
 *
 * @author Charles Effinger (charles.effinger@cbsinteractive.com)
 * @version $Revision$, $LastChangedDate$
 */
public class AuthenticateHandler extends BaseApiHandler {

    private final String userName;

    private final String password;

    public AuthenticateHandler(HttpServletRequest request) {
        super(request);

        this.responseString = "user";

        this.userName = DataUtil.formatParameter(request, ApiConstants.PARAM_USER_NAME);
        this.password = DataUtil.formatParameter(request, ApiConstants.PARAM_PASSWORD);

    }

    @Override
    protected boolean validateParameters() {

        // validate the phone ID
        if (DataUtil.isEmpty(userName)) {
            errors.add(new ApiError(null, null, null));
        }

        if (DataUtil.isEmpty(password)) {
            errors.add(new ApiError(null, null, null));
        }
        return super.validateParameters();
    }

    @Override
    protected void performRequest() {
        // set up and save the user
        User user = awsDao.getUser(userName);
        if (user != null) {
            try {
                byte[] bSalt = SecurityUtil.base64ToByte(user.getSalt());
                byte[] bDigest = SecurityUtil.base64ToByte(user.getPassword());
                // Compute the new DIGEST
                byte[] proposedDigest = SecurityUtil.getHash(password, bSalt);
                if (Arrays.equals(proposedDigest, bDigest)) {
                    responseObjects.add(user);
                } else {
                    errors.add(new ApiError(null, null, null));
                    responseObjects.add(errors);
                }
            } catch (Exception ignore) { }

        } else {
            errors.add(new ApiError(null, null, null));
            responseObjects.add(errors);
        }
    }
}
