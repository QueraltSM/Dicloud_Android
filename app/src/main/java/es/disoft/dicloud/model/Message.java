package es.disoft.dicloud.model;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;
import android.arch.persistence.room.Index;
import android.support.annotation.NonNull;

@Entity(tableName   = "messages",
        indices     = @Index("user_id"),
        primaryKeys = {"user_id", "from_id"},
        foreignKeys = @ForeignKey(entity        = User.class,
                                  parentColumns = "id",
                                  childColumns  = "user_id",
                                  onDelete      = ForeignKey.CASCADE))
public class Message {

    @NonNull
    @ColumnInfo(name = "user_id")
    private String user_id;
    @NonNull
    private int from_id;
    @NonNull
    private String from;
    @NonNull
    private String last_message_timestamp;
    @NonNull
    private int messages_count;

    public Message(@NonNull int from_id,
                   @NonNull String from,
                   @NonNull String last_message_timestamp,
                   @NonNull int messages_count) {
        user_id                     = User.currentUser.getId();
        this.from_id                = from_id;
        this.from                   = from;
        this.last_message_timestamp = last_message_timestamp;
        this.messages_count         = messages_count;
    }

    @NonNull
    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(@NonNull String user_id) {
        this.user_id = user_id;
    }

    @NonNull
    public int getFrom_id() {
        return from_id;
    }

    public void setFrom_id(@NonNull int from_id) {
        this.from_id = from_id;
    }

    @NonNull
    public String getFrom() {
        return from;
    }

    public void setFrom(@NonNull String from) {
        this.from = from;
    }

    @NonNull
    public String getLast_message_timestamp() {
        return last_message_timestamp;
    }

    public void setLast_message_timestamp(@NonNull String last_message_timestamp) {
        this.last_message_timestamp = last_message_timestamp;
    }

    @NonNull
    public int getMessages_count() {
        return messages_count;
    }

    public void setMessages_count(@NonNull int messages_count) {
        this.messages_count = messages_count;
    }

    @Override
    public String toString() {
        return "\n(" + getMessages_count() + ") " + getFrom();
    }

    public static class Fetch extends Message {

        private String status;

        public Fetch(@NonNull int from_id,
                     @NonNull String from,
                     @NonNull String last_message_timestamp,
                     @NonNull int messages_count) {
            super(from_id, from, last_message_timestamp, messages_count);
            status = "";
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        @Override
        public String toString() {
           return String.format("%-20s  %s", super.toString(), status);
        }
    }

    public static class EssentialInfo {
        public int from_id;
        public String from;
        public int messages_count;

        public int getFrom_id() {
            return from_id;
        }

        public String getFrom() {
            return from;
        }

        public int getMessages_count() {
            return messages_count;
        }
    }
}
