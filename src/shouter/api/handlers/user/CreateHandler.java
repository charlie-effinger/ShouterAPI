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
public class CreateHandler extends BaseApiHandler {

    private final String userName;

    private final String password;

    private final String passwordConfirm;

    private final String iosId;

    private final String androidId;

    public CreateHandler(HttpServletRequest request) {
        super(request);

        this.responseString = "user";

        this.userName = DataUtil.formatParameter(request, ApiConstants.PARAM_USER_NAME);
        this.password = (DataUtil.formatParameter(request, ApiConstants.PARAM_PASSWORD));
        this.passwordConfirm = DataUtil.formatParameter(request, ApiConstants.PARAM_PASSWORD_CONFIRM);
        this.iosId = DataUtil.formatParameter(request, ApiConstants.PARAM_IOS_ID);
        this.androidId = DataUtil.formatParameter(request, ApiConstants.PARAM_ANDROID_ID);
    }

    @Override
    protected boolean validateParameters() {

        // validate the userName
        if (DataUtil.isEmpty(userName)) {
            errors.add(new ApiError(null, null, null));
        } else {
            if (awsDao.checkUserName(userName)) {
                errors.add(new ApiError(null, null, null));
            }
        }

        if (!password.equals(passwordConfirm)) {
            errors.add(new ApiError(null, null, null));
        }

        return super.validateParameters();
    }

    @Override
    protected void performRequest() {

        String sDigest = "";
        String sSalt = "";
        try {
            // Uses a secure Random not a simple Random
            SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
            // Salt generation 64 bits long
            byte[] bSalt = new byte[8];
            random.nextBytes(bSalt);
            // Digest computation
            byte[] bDigest = SecurityUtil.getHash(password, bSalt);
            sDigest = SecurityUtil.byteToBase64(bDigest);
            sSalt = SecurityUtil.byteToBase64(bSalt);

        } catch (NoSuchAlgorithmException ignore) { }

        try {
            // set up and save the user
            User user = new User(userName, sDigest, sSalt, iosId, androidId);
            awsDao.saveUser(user);
            responseObjects.add(user);
        } catch (Exception e) {
            this.responseString = "errors";
            responseObjects.add(new ApiError(null, null, null));
        }
    }
}
