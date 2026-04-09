package com.hunts.bail.domain;

import java.time.LocalDate;
import java.util.*;

/**
 * Immutable value object representing the individual charged with an offence.
 *
 */
public final class AccusedPerson {

    private final String       personId;
    private final String       fullName;
    private final LocalDate    dateOfBirth;
    private final String       nationalId;
    private final String       address;
    private final String       contactNumber;
    private final String       biometricRef;
    private final List<String> offenses;
    private final String       photoBase64;   // nullable — base64 PNG/JPG

    private AccusedPerson(Builder builder) {
        this.personId      = Objects.requireNonNull(builder.personId,      "personId must not be null");
        this.fullName      = Objects.requireNonNull(builder.fullName,      "fullName must not be null");
        this.dateOfBirth   = Objects.requireNonNull(builder.dateOfBirth,   "dateOfBirth must not be null");
        this.nationalId    = Objects.requireNonNull(builder.nationalId,    "nationalId must not be null");
        this.address       = Objects.requireNonNull(builder.address,       "address must not be null");
        this.contactNumber = Objects.requireNonNull(builder.contactNumber, "contactNumber must not be null");
        this.biometricRef  = builder.biometricRef  != null ? builder.biometricRef  : "";
        this.photoBase64   = builder.photoBase64   != null ? builder.photoBase64   : "";
        this.offenses      = Collections.unmodifiableList(new ArrayList<>(builder.offenses));
    }

    public String       getPersonId()       { return personId; }
    public String       getFullName()       { return fullName; }
    public LocalDate    getDateOfBirth()    { return dateOfBirth; }
    public String       getNationalId()     { return nationalId; }
    public String       getAddress()        { return address; }
    public String       getContactNumber()  { return contactNumber; }
    public String       getBiometricRef()   { return biometricRef; }
    public List<String> getOffenses()       { return offenses; }
    /** Raw base64 string of the photo, or empty string if not set. */
    public String       getPhotoBase64()    { return photoBase64; }
    public boolean      hasPhoto()          { return !photoBase64.isEmpty(); }

    public String getSummary() {
        return String.format("AccusedPerson[id=%s, name=%s, nationalId=%s, offenses=%s]",
                personId, fullName, nationalId, offenses);
    }
    @Override public String toString() { return getSummary(); }

    public static Builder builder(String personId) { return new Builder(personId); }

    public static final class Builder {
        private final String personId;
        private String fullName, nationalId, address, contactNumber, biometricRef, photoBase64;
        private LocalDate dateOfBirth;
        private final List<String> offenses = new ArrayList<>();

        private Builder(String personId) { this.personId = personId; }

        public Builder fullName(String v)       { this.fullName = v;       return this; }
        public Builder dateOfBirth(LocalDate v) { this.dateOfBirth = v;    return this; }
        public Builder nationalId(String v)     { this.nationalId = v;     return this; }
        public Builder address(String v)        { this.address = v;        return this; }
        public Builder contactNumber(String v)  { this.contactNumber = v;  return this; }
        public Builder biometricRef(String v)   { this.biometricRef = v;   return this; }
        public Builder photoBase64(String v)    { this.photoBase64 = v;    return this; }
        public Builder addOffense(String o)     { this.offenses.add(o);    return this; }
        public Builder offenses(List<String> l) { this.offenses.addAll(l); return this; }
        public AccusedPerson build()            { return new AccusedPerson(this); }
    }
}
