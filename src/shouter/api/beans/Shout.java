/*
 * $Id$
 * $HeadURL$
 */
package shouter.api.beans;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAutoGeneratedKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;

import java.util.Collection;

/**
 * The Shout object bean. Mapped from the Dynamo DB.
 *
 * @author Charles Effinger (charles.effinger@cbsinteractive.com)
 * @version $Revision$, $LastChangedDate$
 */

@DynamoDBTable(tableName = "Shouts")
public class Shout implements Comparable {

    private String id;

    @DynamoDBHashKey
    @DynamoDBAutoGeneratedKey
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    private Long timestamp;

    private String message;

    private String phoneId;

    private String parentId; // id of the shout's parent, if it is a comment

    private Double latitude;

    private Double longitude;

    private Collection<Shout> comments;

    public Shout() {

    }

    public Shout(Double longitude, Double latitude, String phoneId, String message) {
        this(null, message, phoneId, null, latitude, longitude);
    }

    public Shout(String shoutId, String message, String phoneId, String parentId, Double latitude, Double longitude) {
        this.message = message;
        this.phoneId = phoneId;
        this.parentId = parentId;
        this.latitude = latitude;
        this.longitude = longitude;
    }




    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getPhoneId() {
        return phoneId;
    }

    public void setPhoneId(String phoneId) {
        this.phoneId = phoneId;
    }

    public String getParentId() {
        return parentId;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public Collection<Shout> getComments() {
        return comments;
    }

    public void setComments(Collection<Shout> comments) {
        this.comments = comments;
    }

    @Override
    public String toString() {
        return "Shout [id=" + id + ", message=" + message + ", timestamp=" + timestamp + ", latitude=" + latitude +
                ", longitude=" + longitude + ", phoneId=" + phoneId + ", parentId=" + parentId + "]";
    }

    @Override
    public int compareTo(Object o) {
        return this.getTimestamp().compareTo(((Shout) o).getTimestamp());
    }
}