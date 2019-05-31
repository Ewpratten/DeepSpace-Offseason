package frc.robot.commands;

import edu.wpi.first.wpilibj.command.Command;
import java.lang.Math;

import frc.common.control.CubicDeadband;

import frc.robot.Constants;
import frc.robot.Robot;
import frc.robot.subsystems.Superstructure;
import frc.robot.OI;

public class ControlExtras extends Command {
    Superstructure superstructure;
    OI oi;

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

        // Get intake toggle

        // Get outtake button

        // Set appropriate want for superstructure

        // Feed the superstructure with data
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
