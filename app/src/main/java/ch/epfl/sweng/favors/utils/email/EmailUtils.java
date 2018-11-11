package ch.epfl.sweng.favors.utils.email;

import android.content.Context;
import android.support.annotation.NonNull;

/**
 * Provides the utilities that allow for the sending of emails.
 */
public class EmailUtils {
    /**
     * Sends an email to `to` originating from `from`.
     *
     * Testing details implementation:
     * If ExecutionMode.getInstance().setInvalidAuthTest(true) then the email will fail to be send and toast for a failed message will be shown
     *
     * @param from - the originating source of the email
     * @param to - whom the email will be send
     * @param subject - subject/header of the email
     * @param message - the main body of the email
     * @param context - the current context that is using this method
     * @param successMsg - text that will be displayed as a toast if the email is successfully send
     * @param failureMsg - text that will be displayed as a toast if the email fails to be send
     */
    public static void sendEmail(@NonNull String from, @NonNull String to, String subject, String message, @NonNull Context context, @NonNull String successMsg, @NonNull String failureMsg){

        RetrofitDispatcher.getInstance()
                .getApi()
                .sendEmail(from, to, subject, message)
                .enqueue(RetrofitDispatcher.getInstance().getCallback(context, successMsg, failureMsg));
    }
}
