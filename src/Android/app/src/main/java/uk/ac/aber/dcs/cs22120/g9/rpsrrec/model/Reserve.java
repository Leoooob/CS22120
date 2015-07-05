package uk.ac.aber.dcs.cs22120.g9.rpsrrec.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.ArrayList;

public class Reserve implements Parcelable {

    /** Class loader used when de-serialising from a {@link Parcel}. */
    public static final Parcelable.Creator<Reserve> CREATOR = new Parcelable.Creator<Reserve>() {
        @Override
        public Reserve createFromParcel(Parcel source) {
            return new Reserve(source);
        }

        @Override
        public Reserve[] newArray(int size) {
            return new Reserve[size];
        }
    };

    /** Reserve database ID. */
    public long id;
    /** Reserve name. */
    public String reserveName;
    /** User name. */
    public String userName;
    /** User's phone number. */
    public String phoneNumber;
    /** User's email address. */
    public String email;
    /** Reserve address. */
    public String gridReference;
    /** Recording description. */
    public String description;

    public Reserve() {
    }

    /** Create a new Reserve by de-serialising it from a {@link Parcel}. */
    public Reserve(Parcel in) {
        this.id = in.readLong();
        this.reserveName = in.readString();
        this.userName = in.readString();
        this.phoneNumber = in.readString();
        this.email = in.readString();
        this.gridReference = in.readString();
        this.description = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        // Write reserve data to the parcel.
        dest.writeLong(id);
        dest.writeString(reserveName);
        dest.writeString(userName);
        dest.writeString(phoneNumber);
        dest.writeString(email);
        dest.writeString(gridReference);
        dest.writeString(description);
    }

}
