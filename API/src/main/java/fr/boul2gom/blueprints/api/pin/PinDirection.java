package fr.boul2gom.blueprints.api.pin;

public enum PinDirection {

    INPUT("Input"),
    OUTPUT("Output");

    private final String name;

    PinDirection(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
