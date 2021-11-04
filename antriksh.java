package antriksh;

import robocode.*;

import java.awt.*;

import static robocode.util.Utils.normalRelativeAngleDegrees;

/*
 * antriksh 2.0
 *
 * By Antriksh Arora
 */
public class antriksh extends AdvancedRobot {

    double fieldWidth; // battlefield width
    double fieldHeight; // battlefield height
    boolean forwardMovement; // is true when robot is moving forward - when ahead is called, is false when back is called
    boolean closeToWall; // is true when robot is closer than 50px to any wall
    int others; // saves the number enemies at beginning of the fight
    int missedBullets = 0; // counts the streak in which bullets don't hit any enemy
    int counter = 0; // counts whether bullet should be shot or not after a long miss streak

    public void run() {

        // set colors
        setAllColors(Color.pink);

        // get field width and height
        fieldWidth = getBattleFieldWidth();
        fieldHeight= getBattleFieldHeight();
        others = getOthers();

        // allow every part of the robot move independent of the other parts
        setAdjustGunForRobotTurn(true);
        setAdjustRadarForGunTurn(true);
        setAdjustRadarForRobotTurn(true);

        // check if the robot os closer than 50px to any wall
        if (getX() <= 50 || getY() <= 50 || fieldWidth-getX() <= 50 || fieldHeight-getY() <= 50) {
            closeToWall = true;
        } else {
            closeToWall = false;
        }

        setAhead(100000); // lage number to make it move infinity unit it gets interrupted
        forwardMovement = true;
        setTurnRadarRight(360); // scans around himself to spot enemies

        while (true) {

            // if the robot was close a wall, check if it has moved away yet and set the variable to false
            if (getX() > 50 && getY() > 50 && getBattleFieldWidth() - getX() > 50 && getBattleFieldHeight() - getY() > 50 && closeToWall) {
                closeToWall = false;
            }

            // check if robot is close to any wall
            if (getX() <= 50 || getY() <= 50 || fieldWidth - getX() <= 50 || fieldHeight - getY() <= 50 ) {
                // if hasn't noticed yet, then move robot away and change the value of the variable to true
                // if has noticed yet, then do nothing
                if (!closeToWall){
                    bounce();
                    closeToWall = true;
                }
            }

            // in case the radar stopped turning, scan around and spot a new enemy
            if (getRadarTurnRemaining() == 0.0){
                setTurnRadarRight(360);
            }

            execute();
        }
    }

    // when robot aims at an enemy
    public void onScannedRobot(ScannedRobotEvent event) {

        // get location of the enemy
        double absoluteBearing = getHeading() + event.getBearing();
        double bearingFromGun = normalRelativeAngleDegrees(absoluteBearing - getGunHeading());
        double bearingFromRadar = normalRelativeAngleDegrees(absoluteBearing - getRadarHeading());

        // movement: move around enemy in circle
        if (forwardMovement){
            setTurnRight(normalRelativeAngleDegrees(event.getBearing() + 80));
        }
        // if is moving backwards, add 100 - 80 and 100 causes that robot moves a bit closer after hitting wall
        else {
            setTurnRight(normalRelativeAngleDegrees(event.getBearing() + 100));
        }

        setTurnGunRight(bearingFromGun); // turns gun to keep aim at enemy
        setTurnRadarRight(bearingFromRadar); // turns the radar to keeps track of the enemy

        // if the missed bullet steak is lower than 10
        if (missedBullets <= 10) {

            // until 6 out of 8 enemy are alive fire with max power bullets
            // check gun heat and energy to not kill yourself
            if (getOthers() / (double) others > 0.75 && getGunHeat() == 0 && getEnergy() > 15) {
                fire(3);
            }
            // until 5 out of 8 enemy are alive fire with power of 2.5
            else if (getOthers() / (double) others > 0.625 && getGunHeat() == 0 && getEnergy() > 15) {
                fire(2.5);
            }
            // until 4 out of 8 enemy are alive fire with power of 2.25
            else if (getOthers() / (double) others > 0.5 && getGunHeat() == 0 && getEnergy() > 15) {
                fire(2.25);

            }
            // setting conditions to select the power of the bullet
            else if (event.getDistance() > 200 && getGunHeat() == 0 && getEnergy() > 5) {
                fire(1);

            } else if (event.getDistance() > 150 && getGunHeat() == 0 && getEnergy() > 15) {
                fire(1.5);

            } else if (event.getDistance() > 100 && getGunHeat() == 0 && getEnergy() > 15) {
                fire(2);

            } else if (event.getDistance() > 50 && getGunHeat() == 0 && getEnergy() > 10) {
                fire(2.5);

            } else if (event.getDistance() < 50 && getGunHeat() == 0) {
                if (event.getEnergy() < 3.1) {
                    fire(1);
                } else {
                    fire(3);
                }
            } else {
                // else just keep track the enemy
                setTurnGunRight(bearingFromGun);
                setTurnRadarRight(bearingFromRadar);
            }
        }
        // when the missed bulled steak is above 10, then fire only every third bullet
        else if (counter %3 == 0) {
            fire(1);
            counter++;
        } else {
            // else just keep track the enemy and raise the counter
            setTurnGunRight(bearingFromGun);
            setTurnRadarRight(bearingFromRadar);
            counter++;
        }
    }

    // in case the robot still touches the wall
    public void onHitWall(HitWallEvent event) {
        bounce();
    }

    public void bounce() {
        if (forwardMovement) {
            setBack(100000);
            forwardMovement = false;
        } else {
            setAhead(100000);
            forwardMovement = true;
        }
    }

    public void onHitByBullet(HitByBulletEvent event) {
        // if robot gets hit it does sharper turns
        if (forwardMovement){
            setTurnRight(normalRelativeAngleDegrees(event.getBearing() -20));
        }
        else {
            setTurnRight(normalRelativeAngleDegrees(event.getBearing() + 20));
        }
    }


    public void onHitRobot(HitRobotEvent event) {
        // if we drive into another robot, then bounce - when another robot drives into us, do nothing
        if (event.isMyFault()) {
            bounce();
        }
    }


    public void onBulletHit(BulletHitEvent event) {
        // reset the missed bullets streak
        missedBullets = 0;
        counter = 0;
    }


    public void onBulletMissed(BulletMissedEvent event) {
        // add one to the missed bullets streak
        missedBullets++;
    }


    public void onWin(WinEvent event) {
        // do a win dance
        for (int i = 0; i < 50; i++) {
            turnRight(30);
            turnLeft(30);
        }
    }


    // TODO: predict the enemies movement to get better aim and miss lesser bullets
}
