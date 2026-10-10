package microarch.delivery.core.application.commands;

public final class AssignOrderCommand {

    private AssignOrderCommand() {
    }

    public static AssignOrderCommand create() {
        return new AssignOrderCommand();
    }
}
