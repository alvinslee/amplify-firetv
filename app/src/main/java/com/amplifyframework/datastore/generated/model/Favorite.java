package com.amplifyframework.datastore.generated.model;

import com.amplifyframework.core.model.temporal.Temporal;
import com.amplifyframework.core.model.ModelIdentifier;

import java.util.List;
import java.util.UUID;
import java.util.Objects;

import androidx.core.util.ObjectsCompat;

import com.amplifyframework.core.model.AuthStrategy;
import com.amplifyframework.core.model.Model;
import com.amplifyframework.core.model.ModelOperation;
import com.amplifyframework.core.model.annotations.AuthRule;
import com.amplifyframework.core.model.annotations.Index;
import com.amplifyframework.core.model.annotations.ModelConfig;
import com.amplifyframework.core.model.annotations.ModelField;
import com.amplifyframework.core.model.query.predicate.QueryField;

import static com.amplifyframework.core.model.query.predicate.QueryField.field;

/** This is an auto generated class representing the Favorite type in your schema. */
@SuppressWarnings("all")
@ModelConfig(pluralName = "Favorites", type = Model.Type.USER, version = 1, authRules = {
  @AuthRule(allow = AuthStrategy.PRIVATE, operations = { ModelOperation.CREATE, ModelOperation.UPDATE, ModelOperation.DELETE, ModelOperation.READ })
}, hasLazySupport = true)
public final class Favorite implements Model {
  public static final FavoritePath rootPath = new FavoritePath("root", false, null);
  public static final QueryField ID = field("Favorite", "id");
  public static final QueryField USER_ID = field("Favorite", "userId");
  public static final QueryField VIDEO_ID = field("Favorite", "videoId");
  private final @ModelField(targetType="ID", isRequired = true) String id;
  private final @ModelField(targetType="String", isRequired = true) String userId;
  private final @ModelField(targetType="String", isRequired = true) String videoId;
  private @ModelField(targetType="AWSDateTime", isReadOnly = true) Temporal.DateTime createdAt;
  private @ModelField(targetType="AWSDateTime", isReadOnly = true) Temporal.DateTime updatedAt;
  /** @deprecated This API is internal to Amplify and should not be used. */
  @Deprecated
   public String resolveIdentifier() {
    return id;
  }
  
  public String getId() {
      return id;
  }
  
  public String getUserId() {
      return userId;
  }
  
  public String getVideoId() {
      return videoId;
  }
  
  public Temporal.DateTime getCreatedAt() {
      return createdAt;
  }
  
  public Temporal.DateTime getUpdatedAt() {
      return updatedAt;
  }
  
  private Favorite(String id, String userId, String videoId) {
    this.id = id;
    this.userId = userId;
    this.videoId = videoId;
  }
  
  @Override
   public boolean equals(Object obj) {
      if (this == obj) {
        return true;
      } else if(obj == null || getClass() != obj.getClass()) {
        return false;
      } else {
      Favorite favorite = (Favorite) obj;
      return ObjectsCompat.equals(getId(), favorite.getId()) &&
              ObjectsCompat.equals(getUserId(), favorite.getUserId()) &&
              ObjectsCompat.equals(getVideoId(), favorite.getVideoId()) &&
              ObjectsCompat.equals(getCreatedAt(), favorite.getCreatedAt()) &&
              ObjectsCompat.equals(getUpdatedAt(), favorite.getUpdatedAt());
      }
  }
  
  @Override
   public int hashCode() {
    return new StringBuilder()
      .append(getId())
      .append(getUserId())
      .append(getVideoId())
      .append(getCreatedAt())
      .append(getUpdatedAt())
      .toString()
      .hashCode();
  }
  
  @Override
   public String toString() {
    return new StringBuilder()
      .append("Favorite {")
      .append("id=" + String.valueOf(getId()) + ", ")
      .append("userId=" + String.valueOf(getUserId()) + ", ")
      .append("videoId=" + String.valueOf(getVideoId()) + ", ")
      .append("createdAt=" + String.valueOf(getCreatedAt()) + ", ")
      .append("updatedAt=" + String.valueOf(getUpdatedAt()))
      .append("}")
      .toString();
  }
  
  public static UserIdStep builder() {
      return new Builder();
  }
  
  /**
   * WARNING: This method should not be used to build an instance of this object for a CREATE mutation.
   * This is a convenience method to return an instance of the object with only its ID populated
   * to be used in the context of a parameter in a delete mutation or referencing a foreign key
   * in a relationship.
   * @param id the id of the existing item this instance will represent
   * @return an instance of this model with only ID populated
   */
  public static Favorite justId(String id) {
    return new Favorite(
      id,
      null,
      null
    );
  }
  
  public CopyOfBuilder copyOfBuilder() {
    return new CopyOfBuilder(id,
      userId,
      videoId);
  }
  public interface UserIdStep {
    VideoIdStep userId(String userId);
  }
  

  public interface VideoIdStep {
    BuildStep videoId(String videoId);
  }
  

  public interface BuildStep {
    Favorite build();
    BuildStep id(String id);
  }
  

  public static class Builder implements UserIdStep, VideoIdStep, BuildStep {
    private String id;
    private String userId;
    private String videoId;
    public Builder() {
      
    }
    
    private Builder(String id, String userId, String videoId) {
      this.id = id;
      this.userId = userId;
      this.videoId = videoId;
    }
    
    @Override
     public Favorite build() {
        String id = this.id != null ? this.id : UUID.randomUUID().toString();
        
        return new Favorite(
          id,
          userId,
          videoId);
    }
    
    @Override
     public VideoIdStep userId(String userId) {
        Objects.requireNonNull(userId);
        this.userId = userId;
        return this;
    }
    
    @Override
     public BuildStep videoId(String videoId) {
        Objects.requireNonNull(videoId);
        this.videoId = videoId;
        return this;
    }
    
    /**
     * @param id id
     * @return Current Builder instance, for fluent method chaining
     */
    public BuildStep id(String id) {
        this.id = id;
        return this;
    }
  }
  

  public final class CopyOfBuilder extends Builder {
    private CopyOfBuilder(String id, String userId, String videoId) {
      super(id, userId, videoId);
      Objects.requireNonNull(userId);
      Objects.requireNonNull(videoId);
    }
    
    @Override
     public CopyOfBuilder userId(String userId) {
      return (CopyOfBuilder) super.userId(userId);
    }
    
    @Override
     public CopyOfBuilder videoId(String videoId) {
      return (CopyOfBuilder) super.videoId(videoId);
    }
  }
  

  public static class FavoriteIdentifier extends ModelIdentifier<Favorite> {
    private static final long serialVersionUID = 1L;
    public FavoriteIdentifier(String id) {
      super(id);
    }
  }
  
}
