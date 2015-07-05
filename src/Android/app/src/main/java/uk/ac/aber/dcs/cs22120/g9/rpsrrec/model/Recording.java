package uk.ac.aber.dcs.cs22120.g9.rpsrrec.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.text.SimpleDateFormat;
import java.util.Date;

/** Data structure containing an individual recording of a {@link Species} within a {@link Reserve}. */
public class Recording implements Parcelable {
    /** Date format used to format dates of recordings. */
    public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("HH:mm dd/MM/yyyy");

    /** Class loader used when de-serialising from a {@link android.os.Parcel}. */
    public static final Parcelable.Creator<Recording> CREATOR = new Parcelable.Creator<Recording>() {

        @Override
        public Recording createFromParcel(Parcel source) {
            return new Recording(source);
        }

        @Override
        public Recording[] newArray(int size) {
            return new Recording[size];
        }
    };

    /** Recording database ID. */
    public long id;
    /** Reserve the recording was taken in. */
    public Reserve reserve;
    /** Recording species. */
    public Species species;
    /** GPS location latitude. */
    public double locationLat;
    /** GPS location longitude. */
    public double locationLon;
    /** Species abundance. */
    public Abundance abundance;
    /** Recording date. */
    public Date date;
    /** Comment. */
    public String comment;
    /** General photo URL. */
    public String generalPhotoUrl;
    /** Specimen photo URL. */
    public String specimenPhotoUrl;

    /** Default constructor. */
    public Recording() {
    }

    /** Create a new Recording by de-serializing it from a Parcel. */
    public Recording(Parcel in) {
        this.id = in.readLong();
        this.reserve = in.readParcelable(Reserve.class.getClassLoader());
        this.species = in.readParcelable(Species.class.getClassLoader());
        this.locationLat = in.readDouble();
        this.locationLon = in.readDouble();
        this.abundance = Abundance.fromChar(in.readString());
        this.date = new Date(in.readLong());

        if (in.readByte() == 0x01) {
            this.comment = in.readString();
        }
        if (in.readByte() == 0x01) {
            this.generalPhotoUrl = in.readString();
        }
        if (in.readByte() == 0x01) {
            this.specimenPhotoUrl = in.readString();
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(this.id);
        dest.writeParcelable(this.reserve, 0);
        dest.writeParcelable(this.species, 0);
        dest.writeDouble(this.locationLat);
        dest.writeDouble(this.locationLon);
        dest.writeString(this.abundance.toChar());
        dest.writeLong(this.date.getTime());

        if (this.comment != null) {
            dest.writeByte((byte) 0x01);
            dest.writeString(this.comment);
        } else {
            dest.writeByte((byte) 0x00);
        }

        if (this.generalPhotoUrl != null) {
            dest.writeByte((byte) 0x01);
            dest.writeString(this.generalPhotoUrl);
        } else {
            dest.writeByte((byte) 0x00);
        }

        if (this.specimenPhotoUrl != null) {
            dest.writeByte((byte) 0x01);
            dest.writeString(this.specimenPhotoUrl);
        } else {
            dest.writeByte((byte) 0x00);
        }
    }

    /** Species abundance. */
    public enum Abundance {
        DOMINANT,
        ABUNDANT,
        FREQUENT,
        OCCASIONAL,
        RARE;

        /**
         * Get Abundance from its character representation.
         */
        public static Abundance fromChar(String c) {
            if (c.equals("D")) {
                return DOMINANT;
            } else if (c.equals("A")) {
                return ABUNDANT;
            } else if (c.equals("F")) {
                return FREQUENT;
            } else if (c.equals("O")) {
                return OCCASIONAL;
            } else if (c.equals("R")) {
                return RARE;
            }
            throw new IllegalArgumentException();
        }

        /**
         * Convert Abundance to its database character representation.
         */
        public String toChar() {
            switch (this) {
                case DOMINANT:
                    return "D";
                case ABUNDANT:
                    return "A";
                case FREQUENT:
                    return "F";
                case OCCASIONAL:
                    return "O";
                case RARE:
                    return "R";
            }
            throw new IllegalArgumentException();
        }
    }
}