package spotify.exceptions;

public class AccountCreationWentWrong extends Throwable {
    public AccountCreationWentWrong() {
        super("Account creation went wrong");
    }

    public AccountCreationWentWrong(String message) {
        super(message);
    }
}
