package frc.robot.subsystems;

import edu.wpi.first.wpilibj.Timer;
import frc.common.utils.RobotLogger;
import frc.robot.Constants;

/**
 * Any information about the robot's current status should be accessed through
 * the superstructure. This class can be thought of as an interface for getting
 * high-level data about the robot as well as high-level methods for controlling
 * components of the robot that interact with eachother.
 * 
 * ex. An elevator and arm could be both controlled with a
 * setIntakeKinematicPosition(x,y); this function would then follow the correct
 * set of states to get the job done. To move the elevator, it might call a
 * setSetpoint(), then a feed() untill the error is within the threshold. This
 * could be checked with a isAtThreshold()
 * 
 * This file is in the subsystem packge so that it can access protected
 * information from each subsystem.
 * 
 * This is a state machine
 */
public class Superstructure {
    // State that the user wants
    public enum WantedState {
        kIdle, kIntake, kOuttake, kClimb
    }

    // Internal state of robot
    public enum SystemState {
        kIdle, kLowerFinger, kRaiseFinger, kAcceptSliderInput, kEnableLedring, kDisableLedring, kExtendPiston,
        kRetractPison
    }

    // Wanted method of controlling the drivetrain
    public enum WantedDriveMethod {
        kDefault, kArcade, kCurvature
    }

    // Static var for holding the current instance
    private static Superstructure instance = null;
    private RobotLogger logger = RobotLogger.getInstance();

    // States
    public WantedState mWantedState;
    public SystemState mSystemState;

    // Other wants
    public WantedDriveMethod mDriveMethod;

    // Data about state
    private boolean mStateChanged = true;

    // All subsystems involved in, or accessed by the superstructure
    private DriveTrain mDriveTrain = DriveTrain.getInstance();
    private Ledring mLedring = Ledring.getInstance();
    private Slider mSlider = Slider.getInstance();
    private Finger mFinger = Finger.getInstance();
    private Piston mPiston = Piston.getInstance();

    // Buffered data
    private double buffered_slider_speed = 0.0;
    private Slider.WantedState buffered_slider_wanted_state;

    // Ticker to be shared by all states
    private int ticker = 0;

    public static Superstructure getInstance() {
        if (instance == null) {
            instance = new Superstructure();
        }

        return instance;
    }

    /**
     * This is run periodically while the robot is running
     * 
     * This is not to be confused with the conventional subsystem interface. For
     * simple tasks, the usual interface must be used. But for any task that can be
     * controlled by more than one command, or requires multiple subsystems to work
     * together, that code should be spun off here.
     */
    public void periodic(double timestamp) {
        synchronized (this) {
            SystemState newState = mSystemState;

            // Run the correct handler for new state
            switch (newState) {

            // Superstructure idle
            case kIdle:
                newState = handleIdle(mStateChanged);

                // Lowering finger to accept a hatch
            case kLowerFinger:
                newState = handleLowerFinger(mStateChanged);

                // Raising finger once a hatch is grabbed
            case kRaiseFinger:
                newState = handleRaiseFinger(mStateChanged);

                // Enable light
            case kEnableLedring:
                newState = handleEnableLedring(mStateChanged);

                // Disable light
            case kDisableLedring:
                newState = handleDisableLedring(mStateChanged);

                // Accept input from user
            case kAcceptSliderInput:
                newState = handleSliderInput(mStateChanged);

            case kExtendPiston:
                newState = handlePistonExtend(mStateChanged);

            case kRetractPison:
                newState = handlePistonRetract(mStateChanged);
            }

            // Deal with a state change
            if (newState != mSystemState) {
                logger.log("Superstructure state " + mSystemState + " to " + newState + " Timestamp: "
                        + Timer.getFPGATimestamp());
                mSystemState = newState;
                mStateChanged = true;
            } else {
                mStateChanged = false;
            }

            // Increment the ticker
            ticker += 1;

        }
    }

    /* States */

    /**
     * The Idle state
     * 
     * This state runs whenever nothing else on the bot is being used.
     */
    private SystemState handleIdle(boolean stateChanged) {
        // Do required work for this state
        if (stateChanged) {
            // Disable the Ledring, but only on the first time this is called.
            // This way, some other part of the code can override the Ledring state
            mLedring.setWantedState(Ledring.WantedState.kOff);

            // Recentre the slider
            mSlider.setWantedState(Slider.WantedState.kCentre);

            // Stow / raise the finger
            mFinger.setWantedState(Finger.WantedState.kRaised);

            // Reset the WantedState
            mWantedState = WantedState.kIdle;
        }

        /**
         * If the user has requested a new state for the robot, set the next SystemState
         * required to get the job done.
         * 
         * The default state is nothing
         */
        switch (mWantedState) {

        // Intake
        case kIntake:
            return SystemState.kLowerFinger;

        // Outtake
        case kOuttake:
            return SystemState.kLowerFinger;

        // Anything else
        default:
            return SystemState.kIdle;
        }

    }

    /**
     * State handler for lowering the finger and pausing the appropriate amount of
     * time
     */
    private SystemState handleLowerFinger(boolean stateChanged) {
        // Do required work for this state
        if (stateChanged) {
            // Release the solenoid
            mFinger.setWantedState(Finger.WantedState.kLowered);
        }

        // Null loop until ticker hits desired height
        if (!(ticker == Constants.TickerTiming.finger_movement_time)) {
            return SystemState.kLowerFinger;
        }

        // Reset the ticker
        ticker = 0;

        // Move to the next required state for the user input
        switch (mWantedState) {

        // Wants to intake
        case kIntake:
            return SystemState.kEnableLedring;

        case kOuttake:
            return SystemState.kExtendPiston;

        // Wants to do something else. Reset finger first
        default:
            return SystemState.kRaiseFinger;
        }
    }

    private SystemState handleRaiseFinger(boolean stateChanged) {
        // Do required work for this state
        if (stateChanged) {
            // Release the solenoid
            mFinger.setWantedState(Finger.WantedState.kRaised);
        }

        // Move to the next required state for the user input
        switch (mWantedState) {

        // Ideling after an intake
        case kIdle:
            return SystemState.kIdle;

        case kOuttake:
            return SystemState.kIdle;

        // Nothing else needs to be reset. Become idle
        default:
            return SystemState.kIdle;
        }
    }

    private SystemState handleEnableLedring(boolean stateChanged) {
        // Do required work for this state
        if (stateChanged) {
            // Release the solenoid
            mLedring.setWantedState(Ledring.WantedState.kSolid);
        }

        // Move to the next required state for the user input
        switch (mWantedState) {

        // Allow the user to control the slider
        case kIntake:
            return SystemState.kAcceptSliderInput;

        // Nothing else needs to be reset. Become idle
        default:
            return SystemState.kIdle;
        }
    }

    private SystemState handleDisableLedring(boolean stateChanged) {
        // Do required work for this state
        if (stateChanged) {
            // Release the solenoid
            mLedring.setWantedState(Ledring.WantedState.kOff);
        }

        // Move to the next required state for the user input
        switch (mWantedState) {

        // Ideling after an intake
        case kIdle:
            return SystemState.kRaiseFinger;

        // Nothing else needs to be reset. Become idle
        default:
            return SystemState.kIdle;
        }
    }

    private SystemState handleSliderInput(boolean stateChanged) {
        // Do required work for this state

        // Push buffers to slider subsystem
        mSlider.setWantedState(buffered_slider_wanted_state);
        mSlider.setManualSpeed(buffered_slider_speed);

        // Move to the next required state for the user input
        switch (mWantedState) {

        // Ideling after an intake
        case kIdle:
            return SystemState.kDisableLedring;

        // Continue to intake if want does not change
        case kIntake:
            return SystemState.kAcceptSliderInput;

        // Nothing else needs to be reset. Become idle
        default:
            return SystemState.kIdle;
        }
    }

    private SystemState handlePistonExtend(boolean stateChanged) {
        // Do required work for this state
        if (stateChanged) {
            // Release the solenoid
            mPiston.setWantedState(Piston.WantedState.kExtended);
        }

        // Null loop until ticker hits desired height
        if (!(ticker == Constants.TickerTiming.finger_movement_time)) {
            return SystemState.kExtendPiston;
        }

        // Reset the ticker
        ticker = 0;

        // Move to the next required state for the user input
        switch (mWantedState) {

        case kOuttake:
            return SystemState.kRetractPison;

        // Wants to do something else. Reset finger first
        default:
            return SystemState.kRaiseFinger;
        }
    }

    private SystemState handlePistonRetract(boolean stateChanged) {
        // Do required work for this state
        if (stateChanged) {
            // Release the solenoid
            mPiston.setWantedState(Piston.WantedState.kRetracted);
        }

        // Move to the next required state for the user input
        switch (mWantedState) {

        case kOuttake:
            return SystemState.kRaiseFinger;

        // Wants to do something else. Reset finger first
        default:
            return SystemState.kRaiseFinger;
        }
    }

    /* Getters */

    /**
     * Check if the robot is currently driving
     * 
     * @return Is the robot driving
     */
    public boolean isDriving() {
        return (mDriveTrain.is_moving || mDriveTrain.is_turning);
    }

    /* State-independant control */

    /**
     * Control the robot's drivetrain
     */
    public void drive(double throttle, double rotation, boolean mode) {
        // Call the appropriate function for wanted drive method
        switch (mDriveMethod) {
        case kArcade:
            mDriveTrain.arcadeDrive(throttle, rotation, mode);
            break;
        case kCurvature:
            mDriveTrain.cheesyDrive(throttle, rotation, mode);
            break;
        default:
            mDriveTrain.raiderDrive(throttle, rotation);
            break;
        }
    }

    /**
     * Set the buffered speed for the slider. This can be called at any time, but
     * will only actually control the slider when input is needed.
     */
    public void slide(double speed) {
        this.buffered_slider_speed = speed;
    }

    /**
     * Set the buffered WantedState for the slider. This can be called at any time,
     * but will only actually control the slider when input is needed.
     * 
     * Passing a kManual state will allow the input of slide(double) to control the
     * slider
     */
    public void slide(Slider.WantedState state) {
        this.buffered_slider_wanted_state = state;
    }

    public void setWantedState(WantedState state) {
        mWantedState = state;
    }
}