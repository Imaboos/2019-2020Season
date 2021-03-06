/*----------------------------------------------------------------------------*/
/* Copyright (c) 2019 FIRST. All Rights Reserved.                             */
/* Open Source Software - may be modified and shared by FRC teams. The code   */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project.                                                               */
/*----------------------------------------------------------------------------*/

package frc.robot.subsystems;

import edu.wpi.first.wpilibj.AnalogInput;
import edu.wpi.first.wpilibj.controller.PIDController;
import edu.wpi.first.wpilibj.controller.ProfiledPIDController;
import edu.wpi.first.wpilibj.geometry.Rotation2d;
import edu.wpi.first.wpilibj.kinematics.SwerveModuleState;
import edu.wpi.first.wpilibj.trajectory.TrapezoidProfile;
import com.revrobotics.CANSparkMax;
import com.revrobotics.CANSparkMaxLowLevel.MotorType;

import frc.robot.Constants.ModuleConstants;

public class SwerveModule {
  private final CANSparkMax m_driveMotor;
  private final CANSparkMax m_turningMotor;

  private final CANSparkMax m_driveEncoder;
  private final AnalogInput m_turningEncoder;
  private double EncConversionFactor = 1;

  private final PIDController m_drivePIDController =
      new PIDController(ModuleConstants.kPModuleDriveController, 0, 0);

  //Using a TrapezoidProfile PIDController to allow for smooth turning
  private final ProfiledPIDController m_turningPIDController
      = new ProfiledPIDController(
          ModuleConstants.kPModuleTurningController, 0, 0,
          new TrapezoidProfile.Constraints(
              ModuleConstants.kMaxModuleAngularSpeedRadiansPerSecond,
              ModuleConstants.kMaxModuleAngularAccelerationRadiansPerSecondSquared));

  /**
   * Constructs a SwerveModule.
   *
   * @param driveMotorChannel   ID for the drive motor.
   * @param turningMotorChannel ID for the turning motor.
   */
  
  public SwerveModule(int driveMotorChannel,
                      int turningMotorChannel,
                      int[] driveEncoderPorts,
                      int[] turningEncoderPorts,
                      boolean driveEncoderReversed,
                      boolean turningEncoderReversed,
                      double EncOffset) {
                
                
   
    m_driveMotor = new CANSparkMax(driveMotorChannel,MotorType.kBrushless);
    m_turningMotor = new CANSparkMax(turningMotorChannel,MotorType.kBrushless);

    this.m_driveEncoder = new CANSparkMax(driveMotorChannel,MotorType.kBrushless);
    System.out.println(turningEncoderPorts[0]);
    this.m_turningEncoder = new AnalogInput(turningEncoderPorts[0]);
    
    
    // Set the distance per pulse for the drive encoder. We can simply use the
    // distance traveled for one rotation of the wheel divided by the encoder
    // resolution.
    //no silly distance for pulse nonsense with our precious Analog Encoders

    //Set whether drive encoder should be reversed or not

    //m_driveEncoder.setReverseDirection(driveEncoderReversed); 
    //I do not believe we have to reverse the cute little analog encoders 

    // Set the distance (in this case, angle) per pulse for the turning encoder.
    // This is the the angle through an entire rotation (2 * wpi::math::pi)
    // divided by the encoder resolution.

    //m_turningEncoder.setDistancePerPulse(ModuleConstants.kTurningEncoderDistancePerPulse);

    //Set whether turning encoder should be reversed or not

   // m_turningEncoder.setReverseDirection(turningEncoderReversed);

    // Limit the PID Controller's input range between -pi and pi and set the input
    // to be continuous.
    m_turningPIDController.enableContinuousInput(-Math.PI, Math.PI);
  }

  /**
   * Returns the current state of the module.
   *
   * @return The current state of the module.
   */
  public SwerveModuleState getState() {
    return new SwerveModuleState(m_driveEncoder.getEncoder().getVelocity(), new Rotation2d(m_turningEncoder.getValue()*EncConversionFactor));
  }

  /**
   * Sets the desired state for the module.
   *
   * @param state Desired state with speed and angle.
   */
  public void setDesiredState(SwerveModuleState state,double[] EncoderOffset) {
    // Calculate the drive output from the drive PID controller.
    final var driveOutput = m_drivePIDController.calculate(
        m_driveEncoder.getEncoder().getVelocity(), state.speedMetersPerSecond);

    // Calculate the turning motor output from the turning PID controller.
    final var turnOutput = m_turningPIDController.calculate(
        (m_turningEncoder.getValue()*EncConversionFactor) +EncoderOffset[0], state.angle.getRadians()
    );

    System.out.println("Motor number "+ EncoderOffset[1] );
    System.out.println(m_turningEncoder.getValue()*EncConversionFactor);
    // Calculate the turning motor output from the turning PID controller.
    m_driveMotor.set(driveOutput);
    m_turningMotor.set(turnOutput);
  }

  /**
   * Zeros all the SwerveModule encoders.
   */

  public void resetEncoders() {
    m_driveEncoder.getEncoder().setPosition(0);
    //used to reset encoder here
  }
}
