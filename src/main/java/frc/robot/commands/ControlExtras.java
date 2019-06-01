package frc.robot.commands;

import edu.wpi.first.wpilibj.command.Command;
import java.lang.Math;

import frc.common.control.CubicDeadband;

import frc.robot.Constants;
import frc.robot.Robot;
import frc.robot.subsystems.Slider;
import frc.robot.subsystems.Superstructure;
import frc.robot.OI;

public class ControlExtras extends Command {
    Superstructure superstructure;
    OI oi;

    boolean idle_lock = false;

    public ControlExtras() {
        // Not sure if this is needed when using the superstructure
        requires(Robot.mSlider);

        this.oi = OI.getInstance();
        this.superstructure = Superstructure.getInstance();
    }

    // Called just before this Command runs the first time
    @Override
    protected void initialize() {
    }

    // Called repeatedly when this Command is scheduled to run
    @Override
    protected void execute() {
        // Get slider override and presets
        double slider_override = oi.getManualSliderSpeed();
        int slider_position = oi.getSliderPosition();

        // Convert POV to position
        Slider.WantedState slider_state = Slider.WantedState.kManual;
        switch (slider_position) {
        case 270:
            slider_state = Slider.WantedState.kLeft;
            break;
        case 0:
            slider_state = Slider.WantedState.kCentre;
            break;
        case 90:
            slider_state = Slider.WantedState.kRight;
            break;
        }

        // Get intake toggle
        boolean intake = oi.intake();

        // Get outtake button
        boolean outtake = oi.outtake();

        // Set appropriate want for superstructure
        if (intake) {
            superstructure.setWantedState(Superstructure.WantedState.kIntake);
            idle_lock = false;

        } else if (!intake && !outtake && !idle_lock) {
            superstructure.setWantedState(Superstructure.WantedState.kIdle);

            // This lock ensures that this can only be called once, and not override other
            // requests
            idle_lock = true;

        } else if (outtake) {
            superstructure.setWantedState(Superstructure.WantedState.kOuttake);
        }

        // Feed the superstructure with data
        superstructure.slide(slider_override);
        superstructure.slide(slider_state);
    }

    // Make this return true when this Command no longer needs to run execute()
    @Override
    protected boolean isFinished() {
        return false;
    }

    // Called once after isFinished returns true
    @Override
    protected void end() {
    }

    // Called when another command which requires one or more of the same
    // subsystems is scheduled to run
    @Override
    protected void interrupted() {
    }
}
