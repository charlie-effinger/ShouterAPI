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
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

/**
 * Handles authenticating a user's phone ID. If the phone hasn't been registered yet,
 * it will be automatically registered.
 *
 * @author Charles Effinger (charles.effinger@cbsinteractive.com)
 * @version $Revision$, $LastChangedDate$
 */
public class UpdateHandler extends BaseApiHandler {

    private final String userName;

    private final String iosId;

    private final String androidId;

    public UpdateHandler(HttpServletRequest request) {
        super(request);

        this.responseString = "user";

        this.userName = DataUtil.formatParameter(request, ApiConstants.PARAM_USER_NAME);
        this.iosId = DataUtil.formatParameter(request, ApiConstants.PARAM_IOS_ID);
        this.androidId = DataUtil.formatParameter(request, ApiConstants.PARAM_ANDROID_ID);
    }

    @Override
    protected boolean validateParameters() {

        // validate the userName
        if (DataUtil.isEmpty(userName)) {
            errors.add(new ApiError(null, null, null));
        }

        return super.validateParameters();
    }

    @Override
    protected void performRequest() {

        // set up and save the user
        User user = awsDao.getUser(userName);
        if (!DataUtil.isEmpty(iosId)) {
            user.setIosId(iosId);
        }
        if (!DataUtil.isEmpty(androidId)) {
            user.setAndroidId(androidId);
        }

        awsDao.saveUser(user);
        responseObjects.add(user);
    }
}
