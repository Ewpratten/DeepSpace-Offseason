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
        kManual,
        kLeft,
        kCentre,
        kRight
    }

    private static Slider instance = null;
    private final RobotLogger logger = RobotLogger.getInstance();

    private WantedState mWantedState;

    private WPI_TalonSRX slider;
    private DigitalInput left_hall, centre_hall, right_hall;

    protected double speed = 0.0;

    public Slider(){
        logger.log("[Slider] Constructing WPI_TalonSRX for slider", Level.kRobot);
        this.slider = new WPI_TalonSRX(Constants.slider_id);

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
    public void periodic(double timestamp) {
        // Follow appropriate action based on wanted state
        switch (mWantedState){
        case kManual:
            slider.set(speed);
        }
    }

    private void setManualSpeed(double speed) {
        
    }

    /**
     * Sends Subsystem telemetry data to SmartDashboard
     */
    public void outputTelemetry() {
        SmartDashboard.putNumber("Slider speed", speed);
    }
    
    public void reset() {
        
    }
}
