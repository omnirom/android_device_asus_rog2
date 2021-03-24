package android.os;

import android.os.Parcelable;

public class VibrateMessage implements Parcelable {
    public static final Parcelable.Creator<VibrateMessage> CREATOR = new Parcelable.Creator<VibrateMessage>() {
        @Override
        public VibrateMessage createFromParcel(Parcel source) {
            return new VibrateMessage(source);
        }

        @Override
        public VibrateMessage[] newArray(int size) {
            return new VibrateMessage[size];
        }
    };
    public static int TYPE_DURATION = 0;
    public static int TYPE_EFFECT = 1;
    private int mAmplitude = 0;
    private int mDuration = 0;
    private int mEffectId = 0;
    private int mRepeat = 0;
    private int mType = 0;
    private int mUid = 0;

    public VibrateMessage() {
    }

    protected VibrateMessage(Parcel in) {
        this.mType = in.readInt();
        this.mDuration = in.readInt();
        this.mEffectId = in.readInt();
        this.mRepeat = in.readInt();
        this.mAmplitude = in.readInt();
        this.mUid = in.readInt();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.mType);
        dest.writeInt(this.mDuration);
        dest.writeInt(this.mEffectId);
        dest.writeInt(this.mRepeat);
        dest.writeInt(this.mAmplitude);
        dest.writeInt(this.mUid);
    }

    public int getType() {
        return this.mType;
    }

    public void setType(int type) {
        this.mType = type;
    }

    public int getDuration() {
        return this.mDuration;
    }

    public void setDuration(int millisecond) {
        this.mDuration = millisecond;
    }

    public int getEffectId() {
        return this.mEffectId;
    }

    public void setEffectId(int effectId) {
        this.mEffectId = effectId;
    }

    public int getRepeat() {
        return this.mRepeat;
    }

    public void setRepeat(int repeat) {
        this.mRepeat = repeat;
    }

    public int getAmptitude() {
        return this.mAmplitude;
    }

    public void setAmptitude(int amplitude) {
        this.mAmplitude = amplitude;
    }

    public int getUid() {
        return this.mUid;
    }

    public void setUid(int uid) {
        this.mUid = uid;
    }
}
