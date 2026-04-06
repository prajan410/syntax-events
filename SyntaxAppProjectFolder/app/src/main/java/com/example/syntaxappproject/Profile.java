package com.example.syntaxappproject;

/**
 * Model class representing a user profile in the SyntaxEvents application.
 * <p>
 * Stores personal information, role assignments, device identification,
 * and notification preferences for a registered user. This class is used
 * as a Firestore data model and is serialized/deserialized directly via
 * {@code DocumentSnapshot.toObject(Profile.class)}.
 * </p>
 */
public class Profile {

    private String name;
    private String email;
    private String phone;
    private boolean notificationsEnabled;
    private String deviceId;
    private boolean isEntrant;
    private boolean isOrganizer;
    private boolean isAdmin;

    private boolean organizerNotificationEnabled;
    private boolean adminNotificationEnabled;

    private boolean notificationsOptedOut;

    /**
     * Required no-argument constructor for Firestore deserialization.
     */
    public Profile() {}

    /**
     * Constructs a fully initialized Profile.
     *
     * @param name                          the full name of the user
     * @param email                         the email address of the user
     * @param phone                         the phone number of the user, or {@code null} if not provided
     * @param isEntrant                     {@code true} if the user has the entrant role
     * @param isOrganizer                   {@code true} if the user has the organizer role
     * @param isAdmin                       {@code true} if the user has admin privileges; always {@code false} on registration
     * @param organizerNotificationsEnabled {@code true} if the user has enabled notifications from organizers
     * @param adminNotificationEnabled      {@code true} if the user has enabled notifications from admins
     * @param deviceId                      the unique device identifier for the user
     */
    public Profile(String name, String email, String phone,
                   boolean isEntrant, boolean isOrganizer, boolean isAdmin,
                   boolean organizerNotificationsEnabled, boolean adminNotificationEnabled, String deviceId) {
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.isEntrant = isEntrant;
        this.isOrganizer = isOrganizer;
        this.isAdmin = isAdmin;

        this.organizerNotificationEnabled = organizerNotificationsEnabled;
        this.adminNotificationEnabled = adminNotificationEnabled;
        this.deviceId = deviceId;
    }

    /**
     * Returns whether the user has the entrant role.
     *
     * @return {@code true} if the user is an entrant
     */
    public boolean isEntrant() { return isEntrant; }

    /**
     * Sets the entrant role flag for the user.
     *
     * @param entrant {@code true} to assign the entrant role
     */
    public void setEntrant(boolean entrant) { isEntrant = entrant; }

    /**
     * Returns whether the user has the organizer role.
     *
     * @return {@code true} if the user is an organizer
     */
    public boolean isOrganizer() { return isOrganizer; }

    /**
     * Sets the organizer role flag for the user.
     *
     * @param organizer {@code true} to assign the organizer role
     */
    public void setOrganizer(boolean organizer) { isOrganizer = organizer; }

    /**
     * Returns whether the user has admin privileges.
     *
     * @return {@code true} if the user is an admin
     */
    public boolean isAdmin() { return isAdmin; }

    /**
     * Sets the admin privilege flag for the user.
     *
     * @param admin {@code true} to grant admin privileges
     */
    public void setAdmin(boolean admin) { isAdmin = admin; }

    /**
     * Returns the full name of the user.
     *
     * @return the user's name
     */
    public String getName() { return name; }

    /**
     * Sets the full name of the user.
     *
     * @param name the new name to assign
     */
    public void setName(String name) { this.name = name; }

    /**
     * Returns the email address of the user.
     *
     * @return the user's email
     */
    public String getEmail() { return email; }

    /**
     * Sets the email address of the user.
     *
     * @param email the new email to assign
     */
    public void setEmail(String email) { this.email = email; }

    /**
     * Returns the phone number of the user.
     *
     * @return the user's phone number, or {@code null} if not provided
     */
    public String getPhone() { return phone; }

    /**
     * Sets the phone number of the user.
     *
     * @param phone the new phone number to assign, or {@code null} to clear it
     */
    public void setPhone(String phone) { this.phone = phone; }

    /**
     * Returns whether the user has notifications enabled.
     *
     * @return {@code true} if notifications are enabled
     */
    public boolean isNotificationsEnabled() { return notificationsEnabled; }

    /**
     * Sets the notification preference for the user.
     *
     * @param notificationsEnabled {@code true} to enable notifications
     */
    public void setNotificationsEnabled(boolean notificationsEnabled) {
        this.notificationsEnabled = notificationsEnabled;
    }

    /**
     * Returns the unique device identifier associated with the user.
     *
     * @return the device ID string
     */
    public String getDeviceId() { return deviceId; }

    /**
     * Sets the unique device identifier for the user.
     *
     * @param deviceId the device ID to assign
     */
    public void setDeviceId(String deviceId) { this.deviceId = deviceId; }

    /**
     * Sets whether the user has opted out of notifications.
     *
     * @param notificationsOptedOut {@code true} if the user has opted out
     */
    public void setNotificationsOptedOut(boolean notificationsOptedOut) {
        this.notificationsOptedOut = notificationsOptedOut;
    }

    /**
     * Return whether a entrant allow organizer send notification
     * @return true if a entrant allow organizer send notification
     */
    public boolean isOrganizerNotificationEnabled() {
        return organizerNotificationEnabled;
    }

    /**
     * Set the enable of organizer notification
     * @param organizerNotificationEnabled true or false of organizer notification enable
     */
    public void setOrganizerNotificationEnabled(boolean organizerNotificationEnabled) {
        this.organizerNotificationEnabled = organizerNotificationEnabled;
    }
    /**
     * Return whether a entrant allow admin send notification
     * @return true if a entrant allow admin send notification
     */
    public boolean isAdminNotificationEnabled() {
        return adminNotificationEnabled;
    }

    /**
     * Set the enable of admin notification
     * @param adminNotificationEnabled true or false of admin notification enable
     */
    public void setAdminNotificationEnabled(boolean adminNotificationEnabled) {
        this.adminNotificationEnabled = adminNotificationEnabled;
    }


}
