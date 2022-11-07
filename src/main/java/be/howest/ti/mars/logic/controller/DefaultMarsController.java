package be.howest.ti.mars.logic.controller;

/**
 * DefaultMarsController is the default implementation for the MarsController interface.
 * The controller shouldn't even know that it is used in an API context..
 *
 * This class and all other classes in the logic-package (or future sub-packages)
 * should use 100% plain old Java Objects (POJOs). The use of Json, JsonObject or
 * Strings that contain encoded/json data should be avoided here.
 * Keep libraries and frameworks out of the logic packages as much as possible.
 * Do not be afraid to create your own Java classes if needed.
 */
public class DefaultMarsController implements MarsController {
    private static final String MSG_QUOTE_ID_UNKNOWN = "No quote with id: %d";

    // TODO: Test repository
//    @Override
//    public Quote getQuote(int quoteId) {
//        Quote quote = Repositories.getH2Repo().getQuote(quoteId);
//        if (null == quote)
//            throw new NoSuchElementException(String.format(MSG_QUOTE_ID_UNKNOWN, quoteId));
//
//        return quote;
//    }
}