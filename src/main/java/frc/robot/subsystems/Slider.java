package frc.robot.subsystems;

import frc.common.utils.RobotLogger;
import frc.common.utils.RobotLogger.Level;

import edu.wpi.first.wpilibj.command.Subsystem;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import com.ctre.phoenix.motorcontrol.can.WPI_TalonSRX;
import com.ctre.phoenix.motorcontrol.NeutralMode;
import edu.wpi.first.wpilibj.DigitalInput;

import frc.robot.Constants;

/**
 * The Subsystem in control of the robot's slider
 */
public class Slider extends Subsystem {
    public enum WantedState {
        kManual, kLeft, kCentre, kRight
    }

    public enum RelPosition {
        kUnknown, kLeft, kRight, kCentre
    }

    private static Slider instance = null;
    private final RobotLogger logger = RobotLogger.getInstance();

    private WantedState mWantedState;
    private RelPosition mCurrentPosition = RelPosition.kUnknown;

    private WPI_TalonSRX slider;
    private DigitalInput left_hall, centre_hall, right_hall;

    protected double speed = 0.0;

    public Slider() {
        logger.log("[Slider] Constructing WPI_TalonSRX for slider", Level.kRobot);
        this.slider = new WPI_TalonSRX(Constants.slider_id);
        slider.setNeutralMode(NeutralMode.Brake);

        logger.log("[Slider] Constructing Hall effect sensors for slider", Level.kRobot);
        this.left_hall = new DigitalInput(Constants.DIO.slider_left_limit);
        this.centre_hall = new DigitalInput(Constants.DIO.slider_centre_limit);
        this.right_hall = new DigitalInput(Constants.DIO.slider_right_limit);
    }

    @Override
    /**
     * Called by WPIlib's Scheduler during initalization
     */
    public void initDefaultCommand() {
        // setDefaultCommand(new TriggerDrive());
    }

    /**
     * Restricts the slider from moving past it's limits
     * 
     * Note: sensors are wired backwards on both bots
     */
    private double boundOutputSpeed(double speed) {
        double output = speed;

        // Check left limit
        if (output < 0 && !left_hall.get()) {
            output = 0.0;
        }

        // Check right limit
        if (output > 0 && !right_hall.get()) {
            output = 0.0;
        }

        return output;
    }

    /**
     * Get the slider instance
     */
    public static Slider getInstance() {
        if (instance == null) {
            instance = new Slider();
        }

        return instance;
    }

    public void setWantedState(WantedState state) {
        mWantedState = state;
        logger.log("[Slider] Wanted state set to: " + state);
    }

    /**
     * This is called periodically by the robot class.
     * 
     * @param timestamp
     */
    public void update(double timestamp) {
        // Set an output speed to be modified
        double output = 0.0;

        // Follow appropriate action based on wanted state
        switch (mWantedState) {

        // Handle manual control
        case kManual:
            output = boundOutputSpeed(speed);
            break;

        // Handle centering
        case kCentre:

            // Handle movement from each source
            switch (mCurrentPosition) {

            // If unknown, move left until a sensor trips to provide location info
            case kUnknown:
                output = -1;
                break;

            // If left, move right
            case kLeft:
                output = 1;
                break;

            // If right, move left
            case kRight:
                output = -1;
                break;

            // If centre, we are successfull! set the speed to 0, and switch to manual mode
            case kCentre:
                output = 0;
                mWantedState = WantedState.kManual;
                break;
            }

            // Filter the output
            output = boundOutputSpeed(output);
            break;

        // Handle left align
        case kLeft:

            // Handle movement from each source
            switch (mCurrentPosition) {

            // If left, we are successfull! set the speed to 0, and switch to manual mode
            case kLeft:
                output = 0;
                mWantedState = WantedState.kManual;
                break;

            // Everything else should move the slider to the left
            default:
                output = -1;
                break;
            }

            // filter the output
            output = boundOutputSpeed(output);
            break;

        // Handle right align
        case kRight:

            // Handle movement from each source
            switch (mCurrentPosition) {

            // If right, we are successfull! set the speed to 0, and switch to manual mode
            case kRight:
                output = 0;
                mWantedState = WantedState.kManual;
                break;

            // Everything else should move the slider to the right
            default:
                output = 1;
                break;
            }

            // filter the output
            output = boundOutputSpeed(output);
            break;

        }

        // Output to the motor and store speed in variable
        slider.set(output);
        this.speed = output;

        // Read from sensors and determine slider location. note: sensor is backwards
        if (!centre_hall.get()) {
            if (speed > 0) {
                this.mCurrentPosition = RelPosition.kRight;
            } else if (speed < 0) {
                this.mCurrentPosition = RelPosition.kLeft;
            } else {
                this.mCurrentPosition = RelPosition.kCentre;
            }
        }
    }

    /**
     * Set the input speed for the slider
     */
    public void setManualSpeed(double speed) {
        this.speed = speed;
    }

    public boolean isSliderCentred() {
        return !centre_hall.get();
    }

    /**
     * Sends Subsystem telemetry data to SmartDashboard
     */
    public void outputTelemetry() {
        SmartDashboard.putNumber("Slider speed", speed);
    }

    public void reset() {
        setWantedState(WantedState.kCentre);
    }
}
