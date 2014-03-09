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

import javax.servlet.http.HttpServletRequest;

/**
 * Handles authenticating a user's phone ID. If the phone hasn't been registered yet,
 * it will be automatically registered.
 *
 * @author Charles Effinger (charles.effinger@cbsinteractive.com)
 * @version $Revision$, $LastChangedDate$
 */
public class AuthenticateHandler extends BaseApiHandler {

    private final String phoneId;

    public AuthenticateHandler(HttpServletRequest request) {
        super(request);

        this.responseString = "user";

        this.phoneId = DataUtil.formatParameter(request, ApiConstants.PARAM_PHONE_ID);

    }

    @Override
    protected boolean validateParameters() {

        // validate the phone ID
        if (DataUtil.isEmpty(phoneId)) {
            errors.add(new ApiError(null, null, null));
        }
        return super.validateParameters();
    }

    @Override
    protected void performRequest() {
        // set up and save the user
        User user = awsDao.authenticateUser(phoneId);

        if (user == null) {
            errors.add(new ApiError(null, null, null));
            responseObjects.add(errors);
        } else {
            responseObjects.add(user);
        }
    }
}
