/*
 * $Id$
 * $HeadURL$
 */
package shouter.api.beans;

/**
 * The Api Error object bean.
 *
 * @author Charles Effinger (charles.effinger@cbsinteractive.com)
 * @version $Revision$, $LastChangedDate$
 */
public class ApiError implements Comparable {
    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public String getApplicationCode() {
        return applicationCode;
    }

    public void setApplicationCode(String applicationCode) {
        this.applicationCode = applicationCode;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    private String errorCode;

    private String applicationCode;

    private String message;

    public ApiError(String errorCode, String applicationCode, String message) {
        this.errorCode = errorCode;
        this.applicationCode = applicationCode;
        this.message = message;
    }

    @Override
    public int compareTo(Object o) {
        return 1;
    }

}
