package com.oopsipushedtomain.Announcements;

import androidx.annotation.NonNull;

import java.io.Serializable;

/**
 * This class represents Announcement objects in the app, with String fields for the announcements
 * title, body image UID, and event UID.
 * @author  Aidan Gironella
 * @see     AnnouncementListActivity
 * @see     AnnouncementListAdapter
 * @see     SendAnnouncementActivity
 */

public class Announcement implements Serializable {
    private String title;
    private String body;
    private String imageId;
    private String eventId;

    public Announcement(String title, String body, String imageId, String eventId) {
        this.title = title;
        this.body = body;
        this.imageId = imageId;
        this.eventId = eventId;
    }

    /**
     * No-argument constructor so that Announcement can be deserialized
     */
    public Announcement() {
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getImageId() {
        return imageId;
    }

    public void setImageId(String imageId) {
        this.imageId = imageId;
    }

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    /**
     * Prints an announcement details. Currently only used for debugging
     * @return  A string containing all of an announcement's details
     */
    @NonNull
    @Override
    public String toString() {
        return String.format("Title: %s\nBody: %s\neventId: %s\nimageId: %s\n",
                this.getTitle(),
                this.getBody(),
                this.getEventId(),
                this.getImageId());
    }
}
