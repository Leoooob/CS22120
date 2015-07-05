package uk.ac.aber.dcs.cs22120.g9.rpsrrec.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Data structure containing a species as described by the Botanical Society of Britain and Ireland
 * as "BSBI List 2007". If the species is not on that list, the option to provide a new should exist.
 */
public class Species implements Parcelable {

    /** Class loader used when de-serialising from a {@link Parcel}. */
    public static final Parcelable.Creator<Species> CREATOR = new Parcelable.Creator<Species>() {
        @Override
        public Species createFromParcel(Parcel source) {
            return new Species(source);
        }

        @Override
        public Species[] newArray(int size) {
            return new Species[size];
        }
    };

    /** Species database ID. */
    public long id;
    /** Species common name. */
    public String nameCommon;
    /** Species latin name. */
    public String nameSpecies;
    /** Species authority. */
    public String authority;

    /** Default constructor. */
    public Species() {
    }

    /** Create a new Species by de-serialising it from a {@link Parcel}. */
    public Species(Parcel in) {
        this.id = in.readLong();
        this.nameCommon = in.readString();
        this.nameSpecies = in.readString();
        this.authority = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(id);
        dest.writeString(nameCommon);
        dest.writeString(nameSpecies);
        dest.writeString(authority);
    }
}
