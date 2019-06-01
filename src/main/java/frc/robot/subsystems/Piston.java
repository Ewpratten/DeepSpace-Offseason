package frc.robot.subsystems;

import frc.common.utils.RobotLogger;
import frc.common.utils.RobotLogger.Level;

import edu.wpi.first.wpilibj.command.Subsystem;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj.Solenoid;

import frc.robot.Constants;

/**
 * The Subsystem in control of the robot's Pison
 * 
 * Usage: To control the Pison, a state must be requested via setWantedState();
 * this will be completed via the update() function that is called in the
 * robot's periodic loop
 */
public class Piston extends Subsystem {
    public enum WantedState {
        kExtended, kRetracted
    }

    private static Piston instance = null;
    private final RobotLogger logger = RobotLogger.getInstance();

    private WantedState mWantedState;

    private Solenoid pison;

    protected boolean isExtended = false;

    public Piston() {
        logger.log("[Piston] Constructing solenoid", Level.kRobot);
        this.pison = new Solenoid(Constants.PCM.piston);
    }

    @Override
    /**
     * Called by WPIlib's Scheduler during initalization
     */
    public void initDefaultCommand() {
        // setDefaultCommand(new TriggerDrive());
    }

    public static Piston getInstance() {
        if (instance == null) {
            instance = new Piston();
        }

        return instance;
    }

    public void setWantedState(WantedState state) {
        mWantedState = state;
        logger.log("[Piston] Wanted state set to: " + state);
    }

    /**
     * When called, will update to display the wanted state. The reason this
     * subsystem is updated, and not directly controlled, is that it is still used
     * while the robot is disabled.
     */
    public void update() {
        switch (mWantedState) {
        case kExtended:
            pison.set(true);
            isExtended = true;
            break;
        default:
            pison.set(false);
            isExtended = false;
            break;
        }
    }

    /**
     * Sends Subsystem telemetry data to SmartDashboard
     */
    public void outputTelemetry() {
        SmartDashboard.putBoolean("Piston extended", isExtended);
    }

    public void reset() {
        pison.set(false);
    }
}
