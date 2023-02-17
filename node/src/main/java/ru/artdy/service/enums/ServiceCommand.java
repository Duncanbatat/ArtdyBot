package ru.artdy.service.enums;

public enum ServiceCommand {
    HELP("/help"),
    REGISTRATION("/registration"),
    CANCEL("/cancel"),
    START("/start");

    private final String value;

    ServiceCommand(String value) {
        this.value = value;
    }
    
    @Override
    public String toString() {
        return value;
    }

    public static ServiceCommand fromValues(String textMessage) {
        for (ServiceCommand serviceCommand : ServiceCommand.values()) {
            if (serviceCommand.value.equals(textMessage)) {
                return serviceCommand;
            }
        }
        return null;
    }
}
