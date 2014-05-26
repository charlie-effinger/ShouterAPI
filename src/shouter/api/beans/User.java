/*
 * $Id$
 * $HeadURL$
 */
package shouter.api.beans;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;
import com.fasterxml.jackson.annotation.JsonIgnore;
import shouter.api.dao.AwsConstants;
import shouter.api.utils.DataUtil;
import sun.org.mozilla.javascript.internal.ast.Block;

import java.util.Collection;

/**
 * The User object bean. Mapped from the Dynamo DB table "Users"
 *
 * @author Charles Effinger (charles.effinger@cbsinteractive.com)
 * @version $Revision$, $LastChangedDate$
 */

@DynamoDBTable(tableName = AwsConstants.USER_TABLE)
public class User implements Comparable<User>{

    private String userName;

    private String password;

    private String salt;

    private String iosId;

    private String androidId;

    private Collection<BlockedUser> blockedUsers;

    private Collection<BlockedUser> blockedByUsers;

    private Collection<BlockedShout> blockedShouts;

    private boolean displayUserName;

    public User() { }

    public User(String userName, String password) {
        this.userName = userName;
        this.password = password;
    }

    public User(String userName, String iosId, String androidId) {
        this.userName = userName;
        if (!DataUtil.isEmpty(iosId)) {
            this.iosId = iosId;
        }
        if (!DataUtil.isEmpty(androidId)) {
            this.androidId = androidId;
        }
    }

    public User(String userName, String password, String salt, String iosId, String androidId) {
        this.userName = userName;
        this.password = password;
        this.salt = salt;
        this.androidId = androidId;
        this.iosId = iosId;
    }

    @DynamoDBHashKey(attributeName = AwsConstants.USER_NAME)
    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    @JsonIgnore
    @DynamoDBAttribute(attributeName = AwsConstants.PASSWORD)
    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @DynamoDBAttribute(attributeName = AwsConstants.IOS_ID)
    public String getIosId() {
        return iosId;
    }

    public void setIosId(String iosId) {
        this.iosId = iosId;
    }

    @DynamoDBAttribute(attributeName = AwsConstants.ANDROID_ID)
    public String getAndroidId() {
        return androidId;
    }

    public void setAndroidId(String androidId) {
        this.androidId = androidId;
    }

    @JsonIgnore
    @DynamoDBAttribute(attributeName = AwsConstants.SALT)
    public String getSalt() {
        return salt;
    }

    public void setSalt(String salt) {
        this.salt = salt;
    }

    @Override
    public String toString() {
        return "User [userName=" + userName + ", iosId=" + iosId + ", androidId=" + androidId + "]";
    }

    public int compareTo(User o) {
        return userName.compareTo(o.userName);
    }

    public Collection<BlockedShout> getBlockedShouts() {
        return blockedShouts;
    }

    public void setBlockedShouts(Collection<BlockedShout> blockedShouts) {
        this.blockedShouts = blockedShouts;
    }

    public Collection<BlockedUser> getBlockedUsers() {
        return blockedUsers;
    }

    public void setBlockedUsers(Collection<BlockedUser> blockedUsers) {
        this.blockedUsers = blockedUsers;
    }

    public Collection<BlockedUser> getBlockedByUsers() {
        return blockedByUsers;
    }

    public void setBlockedByUsers(Collection<BlockedUser> blockedByUsers) {
        this.blockedByUsers = blockedByUsers;
    }

}
