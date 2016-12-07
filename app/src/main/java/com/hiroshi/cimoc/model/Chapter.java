package com.hiroshi.cimoc.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Hiroshi on 2016/7/2.
 */
public class Chapter implements Parcelable {

    private String title;
    private String path;
    private int count;
    private boolean download;

    public Chapter(String title, String path, int count, boolean download) {
        this.title = title;
        this.path = path;
        this.count = count;
        this.download = download;
    }

    public Chapter(String title, String path) {
        this(title, path, 0, false);
    }

    public Chapter(Parcel source) {
        this(source.readString(), source.readString(), source.readInt(), source.readByte() == 1);
    }

    public String getTitle() {
        return title;
    }

    public String getPath() {
        return path;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public boolean isDownload() {
        return download;
    }

    public void setDownload(boolean download) {
        this.download = download;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof Chapter && ((Chapter) o).path.equals(path);
    }

    @Override
    public int hashCode() {
        return path.hashCode();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(title);
        dest.writeString(path);
        dest.writeInt(count);
        dest.writeByte((byte) (download ? 1 : 0));
    }

    public final static Parcelable.Creator<Chapter> CREATOR = new Parcelable.Creator<Chapter>() {
        @Override
        public Chapter createFromParcel(Parcel source) {
            return new Chapter(source);
        }

        @Override
        public Chapter[] newArray(int size) {
            return new Chapter[size];
        }
    };

}
