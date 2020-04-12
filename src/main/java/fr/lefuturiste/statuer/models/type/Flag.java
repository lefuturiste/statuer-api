package fr.lefuturiste.statuer.models.type;

public class Flag {

    private boolean value;

    private boolean isNull;

    public void setValue(Boolean value) {
        this.isNull = value == null;
        if (!this.isNull) this.value = value;
    }

    public String toString() {
        return isNull ? null : value ? "TRUE" : "FALSE";
    }
}
