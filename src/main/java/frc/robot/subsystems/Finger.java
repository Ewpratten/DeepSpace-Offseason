package frc.robot.subsystems;

import frc.common.utils.RobotLogger;
import frc.common.utils.RobotLogger.Level;

import edu.wpi.first.wpilibj.command.Subsystem;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj.Solenoid;

import frc.robot.Constants;

/**
 * The Subsystem in control of the robot's finger
 * 
 * Usage:
 *  To control the Finger, a state must be requested via setWantedState();
 *  this will be completed via the update() function that is called in the robot's periodic loop
 */
public class Finger extends Subsystem {
    public enum WantedState {
        kLowered,
        kRaised
    }

    private static Finger instance = null;
    private final RobotLogger logger = RobotLogger.getInstance();

    private WantedState mWantedState;

    private Solenoid led;

    protected boolean isLowered = false;

    public Finger(){
        logger.log("[Finger] Constructing solenoid", Level.kRobot);
        this.led = new Solenoid(Constants.PCM.finger);
    }

    @Override
    /**
     * Called by WPIlib's Scheduler during initalization
     */
    public void initDefaultCommand() {
        // setDefaultCommand(new TriggerDrive());
    }

    public static Finger getInstance() {
        if (instance == null) {
            instance = new Finger();
        }

        return instance;
    }

    public void setWantedState(WantedState state) {
        mWantedState = state;
        logger.log("[Finger] Wanted state set to: " + state);
    }

    /**
     * When called, will update to display the wanted state. 
     * The reason this subsystem is updated, and not directly 
     * controlled, is that it is still used while the robot is disabled.
     */
    public void update() {
        switch (mWantedState) {
        case kLowered:
            led.set(false);
            break;
        default:
            led.set(true);
            break;
        }
    }

    /**
     * Sends Subsystem telemetry data to SmartDashboard
     */
    public void outputTelemetry() {
        SmartDashboard.putBoolean("Finger lowered", isLowered);
    }
    
    public void reset() {
        led.set(true);
    }
}
