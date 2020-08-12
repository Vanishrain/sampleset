package cn.iecas.sampleset.pojo.enums;

public enum  SampleSetStatus {
    CREATING(0), TOUPLOAD(1), FINISH(2);

    private int value;

    SampleSetStatus(int value){
        this.value = value;
    }

    public int getValue(){
        return this.value;
    }

    public void setValue(int value){
        this.value = value;
    }

}
