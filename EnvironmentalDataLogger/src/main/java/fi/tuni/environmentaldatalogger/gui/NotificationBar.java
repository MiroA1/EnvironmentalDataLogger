package fi.tuni.environmentaldatalogger.gui;

import javafx.animation.PauseTransition;
import javafx.animation.SequentialTransition;
import javafx.scene.control.Label;
import javafx.animation.TranslateTransition;
import javafx.util.Duration;

/**
 *  A simple notification bar class for nicer user experience. Ideally
 *  used to handle exceptions that occur during the program execution.
 */
public class NotificationBar {

    /**
     * Constructor for notification bar. Contains Label node for
     * informing user of possible error states in program. Additionally, introduces
     * transition animation for smooth user experience
     * @param notificationLabel Label element for the notification (from fxml template)
     */
    public NotificationBar(Label notificationLabel) {
        _notificationLabel = notificationLabel;
        initTransitions();
    }

    /**
     * Simple setter for applying CSS to the label
     * @param colorCSS CSS for changing the notification label style
     */
    private void setLabelStyle(String colorCSS){
        _notificationLabel.setStyle(colorCSS);
    }

    /**
     * Initializes the used transition animations.
     */
    private void initTransitions() {
        TranslateTransition transitionUp = new TranslateTransition();
        transitionUp.setByY(-DEFAULT_OFFSET);
        transitionUp.setDuration(Duration.millis(PUSH_TRANSITION_DURATION));
        transitionUp.setCycleCount(1);

        PauseTransition pause = new PauseTransition(Duration.millis(MESSAGE_SHOWN_DURATION));

        TranslateTransition transitionDown = new TranslateTransition();
        transitionDown.setByY(DEFAULT_OFFSET);
        transitionDown.setDuration(Duration.millis(PUSH_TRANSITION_DURATION));
        transitionDown.setCycleCount(1);

        _transition = new SequentialTransition(transitionUp, pause, transitionDown);
        _transition.setNode(_notificationLabel);
    }

    /**
     * Function to push Alert notification. Label for alerts is orange by default.
     * @param message The message conveying information about the program state.
     */
    public void pushAlertNotification(String message){
        setLabelStyle("-fx-background-color: orange");
        _notificationLabel.setText(message);
        _transition.play();
    }

    // TODO: We could color-code more error messages here
    //  - Green for success
    //  - Orange for alert
    //  - Red for serious error.

    private static final int PUSH_TRANSITION_DURATION = 1000;
    private static final int MESSAGE_SHOWN_DURATION = 8000;
    private static final int DEFAULT_OFFSET = 30;
    private final Label _notificationLabel;
    private SequentialTransition _transition;

}
