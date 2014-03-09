/*
 * $Id$
 * $HeadURL$
 */
package shouter.api.beans;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;

/**
 * The User object bean. Mapped from the Dynamo DB table "Users"
 *
 * @author Charles Effinger (charles.effinger@cbsinteractive.com)
 * @version $Revision$, $LastChangedDate$
 */

@DynamoDBTable(tableName = "Users")
public class User {

    private String phoneId;

    private String firstName;

    private String lastName;

    private String registrationId;

    public User() {
        this(null);
    }

    public User(String phoneId) {
        this(phoneId, null, null);
    }

    public User(String phoneId, String firstName, String lastName) {
        this(phoneId, firstName, lastName, null);
    }

    public User(String phoneId, String firstName, String lastName, String registrationId) {
        this.phoneId = phoneId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.registrationId = registrationId;
    }

    @DynamoDBHashKey
    public String getPhoneId() {
        return phoneId;
    }

    public void setPhoneId(String phoneId) {
        this.phoneId = phoneId;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getRegistrationId() {
        return registrationId;
    }

    public void setRegistrationId(String registrationId) {
        this.registrationId = registrationId;
    }

    @Override
    public String toString() {
        return "User [phoneId=" + phoneId + ", firstName=" + firstName + ", lastName=" + lastName +
                ", registrationId=" + registrationId + "]";
    }

}
