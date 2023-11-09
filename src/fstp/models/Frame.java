package fstp.models;

public class Frame {
    private final int tag;
    private byte[] data;

    public Frame(int tag, byte[] data) {
        this.tag = tag;
        this.data = data;
    }

    public int getTag() {
        return this.tag;
    }

    public byte[] getData() {
        return this.data;
    }
}
