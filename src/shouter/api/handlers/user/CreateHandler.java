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
public class CreateHandler extends BaseApiHandler {

    private final String phoneId;

    private final String firstName;

    private final String lastName;

    private final String registrationId;

    public CreateHandler(HttpServletRequest request) {
        super(request);

        this.responseString = "user";

        this.phoneId = DataUtil.formatParameter(request, ApiConstants.PARAM_PHONE_ID);
        this.firstName = DataUtil.formatParameter(request, ApiConstants.PARAM_FIRST_NAME);
        this.lastName = DataUtil.formatParameter(request, ApiConstants.PARAM_LAST_NAME);
        this.registrationId = DataUtil.formatParameter(request, ApiConstants.PARAM_REGISTRATION_ID);

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
        User user = new User(phoneId, firstName, lastName, registrationId);
        awsDao.createUser(user);
        responseObjects.add(user);
    }
}
