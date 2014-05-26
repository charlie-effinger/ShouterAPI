/*
 * $Id$
 * $HeadURL$
 */
package shouter.api.dao;

/**
 * Constants needed for interaction with the DynamoDB.
 *
 * @author chuck (charlie.effinger@gmail.com)
 * @version $Revision$ $LastChangedDate$
 */
public class AwsConstants {

    /* Tables */
    public static final String SHOUT_TABLE = "Shouts";

    public static final String USER_TABLE = "Users";

    public static final String COMMENT_TABLE = "Comments";

    public static final String LIKED_SHOUTS_TABLE = "UserLikesShout";

    public static final String BLOCKED_USER_TABLE = "BlockedUser";

    public static final String BLOCKED_SHOUT_TABLE = "BlockedShout";

    public static final String BLOCKED_COMMENT_TABLE = "BlockedComment";

    public static final String LIKED_COMMENTS_TABLE = "UserLikesComment";


    /* Columns */
    public static final String ID = "id";

    public static final String SHOUT_ID = "shoutId";

    public static final String MESSAGE = "message";

    public static final String TIMESTAMP = "timestamp";

    public static final String LATITUDE = "latitude";

    public static final String LONGITUDE = "longitude";

    public static final String NUM_LIKES = "numLikes";

    public static final String NUM_COMMENTS = "numComments";

    public static final String USER_NAME = "userName";

    public static final String PASSWORD = "password";

    public static final String SALT = "salt";

    public static final String IOS_ID = "iosId";

    public static final String ANDROID_ID = "androidId";

    public static final String EXPIRATION_TIMESTAMP = "expirationTimestamp";

    public static final String BLOCKED_USER_NAME = "blockedUserName";

    public static final String COMMENT_ID = "commentId";

    /* Default numerical values */
    public static final double DEFAULT_LOCATION_CONSTRAINT = 1.0 / 120.0;  // default location constraint is half a minute

    public static final long DEFAULT_TIME_CONSTRAINT = 3600; // 60 minutes (in seconds)
}
