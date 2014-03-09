/*
 * $Id$
 * $HeadURL$
 */
package shouter.api.handlers.shout;

import shouter.api.ApiConstants;
import shouter.api.beans.ApiError;
import shouter.api.beans.Shout;
import shouter.api.handlers.BaseApiHandler;
import shouter.api.utils.DataUtil;

import javax.servlet.http.HttpServletRequest;
import java.util.Collection;

/**
 * Handler for retrieving shouts around the given latitude and longitude.
 *
 * @author Charles Effinger (charles.effinger@cbsinteractive.com)
 * @version $Revision$, $LastChangedDate$
 */
public class SearchHandler extends BaseApiHandler {

    private Double longitude = null;
    private Double latitude = null;

    public SearchHandler(HttpServletRequest request) {
        super(request);

        this.responseString = "shouts";
        try {
            this.latitude = Double.parseDouble(DataUtil.formatParameter(request, ApiConstants.PARAM_LATITUDE));
            this.longitude = Double.parseDouble(DataUtil.formatParameter(request, ApiConstants.PARAM_LONGITUDE));
        } catch (Exception ignore) {}
    }

    @Override
    protected boolean validateParameters() {

        // validate the latitude
        if (latitude != null) {
            if (!DataUtil.isDegrees(latitude)) {
                errors.add(new ApiError("400", ApiConstants.INVALID_PREFIX + ApiConstants.PARAM_LATITUDE,
                        "Invalid latitude. Please provide a valid latitude."));
            }
        } else {
            errors.add(new ApiError("400", ApiConstants.MISSING_PREFIX + ApiConstants.PARAM_LATITUDE,
                    "Missing latitude. Please provide a latitude."));
        }

        // validate the longitude
        if (longitude != null) {
            if (!DataUtil.isDegrees(longitude)) {
                errors.add(new ApiError("400", ApiConstants.INVALID_PREFIX + ApiConstants.PARAM_LONGITUDE,
                        "Invalid longitude. Please provide a valid longitude."));
            }
        } else {
            errors.add(new ApiError("400", ApiConstants.MISSING_PREFIX + ApiConstants.PARAM_LONGITUDE,
                    "Missing longitude. Please provide a longitude"));
        }

        return super.validateParameters();
    }

    @Override
    protected void performRequest() {
        // retrieve and post the shouts
        Collection<Shout> shouts = awsDao.getShouts(latitude, longitude);
        responseObjects.addAll(shouts);
    }
}
